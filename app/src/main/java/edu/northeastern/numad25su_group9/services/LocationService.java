package edu.northeastern.numad25su_group9.services;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.northeastern.numad25su_group9.R;
import edu.northeastern.numad25su_group9.models.Transaction;
import edu.northeastern.numad25su_group9.utils.LocationUtil;

/**
 * Expose location information.
 */
public class LocationService {
    /**
     * Singleton instance.
     */
    private static LocationService instance;
    private static final String CHANNEL_ID = "location_notification_channel";
    public static final int LOCATION_REQUEST_CODE = 1000;
    private final Context applicationContext;
    private final FusedLocationProviderClient fusedLocationClient;
    private final TransactionService transactionService;
    private final Set<String> notifiedLocations;

    private LocationService(Context context) {
        this.applicationContext = context.getApplicationContext();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.applicationContext);
        transactionService = new TransactionService();
        notifiedLocations = new HashSet<>();
        createNotificationChannel();
    }

    /**
     * Provides the singleton instance of LocationService.
     * @param context Any Context (Activity or Application). The Application Context is extracted and used internally.
     */
    public static synchronized LocationService getInstance(Context context) {
        if (instance == null) {
            instance = new LocationService(context.getApplicationContext());
        }
        return instance;
    }

    /** Callback for returning the current device location. */
    public interface CurrentLocationCallback {
        void onLocationAvailable(Location location);
        void onError(Exception e);
    }

    /**
     * Fetches the device's last known location once.
     * Requires ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION permission.
     * This method assumes necessary permissions are already granted by the calling Activity.
     * If permissions are not granted, it invoke callback with Security exception.
     * @param callback The callback to deliver the location result.
     */
    public void getCurrentLocation(CurrentLocationCallback callback) {
        if (ActivityCompat.checkSelfPermission(applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(applicationContext,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w("LocationService", "Location permissions not granted. Cannot get current location.");
            callback.onError(new SecurityException("Location permissions not granted."));
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        callback.onLocationAvailable(location);
                    } else {
                        callback.onError(new Exception("No last known location available"));
                    }
                })
                .addOnFailureListener(callback::onError);
    }

    /**
     * Start comparing current location updates with locations of previous transactions
     * to send alerts for user visiting known transaction location.
     * HINT: Useful when user logs transaction at a store multiple times.
     * This method assumes necessary permissions are already granted by the calling Activity.
     * If not the case it logs error.
     */
    public void startLocationUpdates() {
        Log.d("Location Service", "In startLocationUpdates");
        if (ActivityCompat.checkSelfPermission(applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(applicationContext,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w("LocationService", "Location permissions not granted. Cannot start location updates.");
            return;
        }

        LocationRequest locationRequest = new LocationRequest
                .Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setMinUpdateIntervalMillis(5000)
                .build();

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    /**
     * Stop receiving location updates.
     */
    public void stopLocationUpdates() {
        Log.d("Location Service", "Stopping location updates");
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                return;
            }
            for (Location location : locationResult.getLocations()) {
                double currentLatitude = location.getLatitude();
                double currentLongitude = location.getLongitude();
                checkProximity(currentLatitude, currentLongitude);
            }
        }
    };

    /** Callback to return locations list asynchronously. */
    private interface LocationsCallback {
        void onReady(List<Location> locations);
        void onError(Exception e);
    }

    // Check proximity of frequent transaction locations with current location data.
    private void checkProximity(double currentLat, double currentLon) {
        fetchFrequentLocations(new LocationsCallback() {
            @Override public void onReady(List<Location> locations) {
                for (Location loc : locations) {
                    float[] results = new float[1];
                    // Correct order: startLat, startLon, endLat, endLon
                    Location.distanceBetween(currentLat, currentLon, loc.getLatitude(), loc.getLongitude(), results);
                    float distanceInMeters = results[0];

                    Log.d("LocationService", "Current: (" + currentLat + ", " + currentLon + ")");
                    Log.d("LocationService", "Saved  : (" + loc.getLatitude() + ", " + loc.getLongitude() + ")");
                    Log.d("LocationService", "Distance: " + distanceInMeters + " m");

                    // Round-off to 4 decimal places.
                    String key = LocationUtil.roundLocToDecimals(loc.getLatitude(), loc.getLongitude());

                    // Notify once per spot per app run (until you clear the set)
                    if (distanceInMeters < 100f && notifiedLocations.add(key)) {
                        sendNotification("You’re near a previous transaction. Add a new one?");
                        Log.d("LocationService", "Notified for key=" + key);
                        break; // only one notification per check
                    }
                }
            }

            @Override public void onError(Exception e) {
                Log.e("LocationService", "Failed to fetch locations", e);
            }
        });
    }

    /** Async loader to dedupe previous transaction coordinates into Location objects. */
    private void fetchFrequentLocations(LocationsCallback cb) {
        transactionService.getAllTransactions(new TransactionService.TransactionsCallback() {
            @Override public void onSuccess(List<Transaction> transactions) {
                Map<String, Location> unique = new HashMap<>();
                for (Transaction t : transactions) {
                    // Skip missing coordinates
                    if (t == null || t.isIgnore() || t.getLatitude() == null || t.getLongitude() == null) continue;

                    // Round to 4 decimals to cluster nearby points
                    String key = LocationUtil.roundLocToDecimals(t.getLatitude(), t.getLongitude());
                    if (!unique.containsKey(key)) {
                        Location loc = new Location("txn");
                        loc.setLatitude(t.getLatitude());
                        loc.setLongitude(t.getLongitude());
                        unique.put(key, loc);
                    }
                }
                cb.onReady(new ArrayList<>(unique.values()));
            }

            @Override public void onError(Exception e) {
                cb.onError(e);
            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Location Notification Channel";
            String description = "Channel for location proximity alerts";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = (NotificationManager)
                    this.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Sends a notification if notification permissions are granted.
     * This method does NOT request permissions or show dialogs.
     * Permissions must be handled by the calling Activity.
     */
    private void sendNotification(String message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Check notification permission (Android 13+)
            if (ActivityCompat.checkSelfPermission(applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.w("LocationService", "Notification permission not granted. Cannot send notification.");
                return;
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_active)
                .setContentTitle("Location-Based Transaction Reminder")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(applicationContext);
        notificationManager.notify(0, builder.build());
    }
}