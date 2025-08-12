package edu.northeastern.numad25su_group9.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import edu.northeastern.numad25su_group9.R;
import edu.northeastern.numad25su_group9.fragments.budget.BudgetFragment;
import edu.northeastern.numad25su_group9.fragments.profile.ProfileFragment;
import edu.northeastern.numad25su_group9.fragments.spending.SpendingFragment;
import edu.northeastern.numad25su_group9.fragments.transactions.TransactionsFragment;
import edu.northeastern.numad25su_group9.databinding.ActivityHomeBinding;
import edu.northeastern.numad25su_group9.models.User;
import edu.northeastern.numad25su_group9.services.LocationService;
import edu.northeastern.numad25su_group9.services.UserService;

public class HomeActivity extends AppCompatActivity {
    private ActivityHomeBinding binding;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final int REQUEST_NOTIFICATION_PERMISSION = 2;
    private final UserService userService = new UserService();
    /**
     * Instantiate inside constructor or will lead to NullPointerException due to dependency on this.
     */
    private LocationService locationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot ());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Bind the menu item selected listener for the bottom navigation menu.
        bindMenuItemSelectedListener();

        // Set the default selected item
        if (savedInstanceState == null) {
            binding.bottomNavigation.setSelectedItemId(R.id.navigation_spending);
        }

        // Set location service
        locationService = LocationService.getInstance(this);

        // Check and request permissions
        checkAndRequestPermissions();
    }

    private void bindMenuItemSelectedListener() {
        /*
         TODO:
          - We could use a ViewModel as a cache to store the data to be displayed
            in each fragment.
        */
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_spending) {
                selectedFragment = new SpendingFragment();
            } else if (itemId == R.id.navigation_budget) {
                selectedFragment = BudgetFragment.newInstance();
            } else if (itemId == R.id.navigation_transactions) {
                selectedFragment = new TransactionsFragment();
            } else if (itemId == R.id.navigation_profile) {
                selectedFragment = ProfileFragment.newInstance();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment_container, selectedFragment).commit();
            }
            return true;
        });
    }

    /**
     * Centralized method to check and request all necessary permissions for the app.
     * This method recursively calls itself to check for the next permission if one is granted.
     * After permissions are confirmed, it proceeds to check user preferences for location updates.
     */
    private void checkAndRequestPermissions() {
        // Check for ACCESS_FINE_LOCATION (or COARSE)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("HomeActivity", "Requesting ACCESS_FINE_LOCATION permission.");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            return; // Exit, result handled in onRequestPermissionsResult
        }

        // Check for POST_NOTIFICATIONS (for Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.d("HomeActivity", "Requesting POST_NOTIFICATIONS permission (Android 13+).");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATION_PERMISSION);
                return; // Exit, result handled in onRequestPermissionsResult
            }
        }

        // If we reach here, all necessary permissions for this Android version are granted.
        Log.d("HomeActivity", "All necessary permissions granted or not applicable for this device API level.");
        // Now, check user preference to decide if location updates should start.
        loadAndApplyLocationPreference();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("HomeActivity", "Location permission granted by user.");
                // Location granted, now recursively call to check for next required permission (e.g., notifications)
                checkAndRequestPermissions();
            } else {
                Log.d("HomeActivity", "Location permission denied by user.");
                // Location permission denied, show dialog to guide user to settings
                // Defer showing dialog to ensure window token is ready
                if (binding != null && binding.getRoot() != null) {
                    binding.getRoot().post(() ->
                            showPermissionDialog("Location permission is required for this app's location-based features. Please enable it in settings.", REQUEST_LOCATION_PERMISSION)
                    );
                }
                // Important: If location permission is denied, location updates cannot start, regardless of user preference.
                locationService.stopLocationUpdates(); // Ensure stopped if denied
            }
        } else if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("HomeActivity", "Notification permission granted by user.");
                // Notification granted. Now, check user preference to decide if location updates should start.
                loadAndApplyLocationPreference();
            } else {
                Log.d("HomeActivity", "Notification permission denied by user.");
                // Notification permission denied, show dialog to guide user to settings
                // Defer showing dialog to ensure window token is ready
                if (binding != null && binding.getRoot() != null) {
                    binding.getRoot().post(() ->
                            showPermissionDialog("Notification permission is required for location-based and spending history alerts. Please enable it in settings.", REQUEST_NOTIFICATION_PERMISSION)
                    );
                }
            }
        }
    }

    /**
     * Loads the user's preference for location-based alerts and starts/stops
     * location updates accordingly, assuming all necessary permissions are granted.
     */
    private void loadAndApplyLocationPreference() {
        Log.d("HomeActivity", "Loading user preference for location alerts.");
        userService.getUser(new UserService.UserCallback() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    // Assuming User model has a method isEnableLocationAlerts() and default is false
                    boolean enableLocationAlerts = user.isLocationAlerts();
                    Log.d("HomeActivity", "User preference for location alerts: " + enableLocationAlerts);

                    // Check both permissions and user preference
                    boolean locationPermissionGranted = ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
                    boolean notificationPermissionGranted = true; // Assume true for older Android, check for Tiramisu
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionGranted = ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
                    }

                    if (enableLocationAlerts && locationPermissionGranted && notificationPermissionGranted) {
                        Log.d("HomeActivity", "Starting location updates based on user preference and granted permissions.");
                        locationService.startLocationUpdates();
                    } else {
                        Log.d("HomeActivity", "Not starting location updates: preference=" + enableLocationAlerts + ", loc_perm=" + locationPermissionGranted + ", notif_perm=" + notificationPermissionGranted);
                        locationService.stopLocationUpdates();
                    }
                } else {
                    Log.w("HomeActivity", "User object is null, cannot apply location preference.");
                    locationService.stopLocationUpdates();
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("HomeActivity", "Error fetching user for location preference: " + e.getMessage());
                locationService.stopLocationUpdates();
            }
        });
    }

    private void showPermissionDialog(String message, int requestCode) {
        // Ensure Activity is not finishing before showing dialog
        if (isFinishing() || isDestroyed()) {
            Log.w("HomeActivity", "Activity is finishing or destroyed, cannot show permission dialog.");
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage(message)
                .setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(android.net.Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    /**
     * Public method for fragments (like ProfileFragment) to request location
     * and notification permissions. This redirects to the centralized permission
     * handling in HomeActivity.
     */
    public void requestLocationNotificationPermissions() {
        Log.d("HomeActivity", "Fragment requested permission flow initiation.");
        checkAndRequestPermissions();
    }
}
