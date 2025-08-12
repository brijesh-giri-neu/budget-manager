package edu.northeastern.numad25su_group9.services;

import com.google.firebase.database.DataSnapshot;
import java.util.ArrayList;
import java.util.List;

import edu.northeastern.numad25su_group9.models.VendorLocation;
import edu.northeastern.numad25su_group9.repositories.VendorLocationRepository;

/** Service to interact with VendorLocation collection */
public class VendorLocationService {
    public interface OperationCallback { void onSuccess(); void onError(Exception e); }
    public interface LocationCallback { void onSuccess(VendorLocation loc); void onError(Exception e); }
    public interface LocationsCallback { void onSuccess(List<VendorLocation> list); void onError(Exception e); }

    private final VendorLocationRepository repo;

    public VendorLocationService(String vendorId) {
        String userId = new AuthService().getCurrentUserId();
        this.repo = new VendorLocationRepository(userId, vendorId);
    }

    public void addOrUpdateLocation(String locationId, VendorLocation loc, OperationCallback cb) {
        repo.addOrUpdateLocation(locationId, loc)
                .addOnSuccessListener(u -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }

    public void deleteLocation(String locationId, OperationCallback cb) {
        repo.deleteLocation(locationId)
                .addOnSuccessListener(u -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }

    public void getLocationById(String locationId, LocationCallback cb) {
        repo.getLocationById(locationId)
                .addOnSuccessListener(snap -> {
                    if (!snap.exists()) {
                        cb.onError(new Exception("Location data does not exist"));
                        return;
                    }
                    VendorLocation v = snap.getValue(VendorLocation.class);
                    if (v != null) cb.onSuccess(v); else cb.onError(new Exception("Location data invalid"));
                })
                .addOnFailureListener(cb::onError);
    }

    public void getAllLocationsForVendor(LocationsCallback cb) {
        repo.getAllLocationsForVendor()
                .addOnSuccessListener(snap -> {
                    if (!snap.exists()) {
                        cb.onSuccess(new ArrayList<>());
                        return;
                    }
                    List<VendorLocation> out = new ArrayList<>();
                    for (DataSnapshot child : snap.getChildren()) {
                        VendorLocation v = child.getValue(VendorLocation.class);
                        if (v != null) out.add(v);
                    }
                    cb.onSuccess(out);
                })
                .addOnFailureListener(cb::onError);
    }
}
