package edu.northeastern.numad25su_group9.repositories;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.Map;

import edu.northeastern.numad25su_group9.models.VendorLocation;

public class VendorLocationRepository extends BaseRepository {
    public VendorLocationRepository(String userId, String vendorId) {
        super(FirebaseDatabase.getInstance()
                .getReference("vendorLocations")
                .child(userId)
                .child(vendorId));
    }

    public Task<Void> addOrUpdateLocation(String locationId, VendorLocation location) {
        Map<String, Object> map = location.toMap();
        map.put("createdAt", ServerValue.TIMESTAMP);
        return set(locationId, map);
    }

    public Task<Void> deleteLocation(String locationId) { return delete(locationId); }
    public Task<DataSnapshot> getLocationById(String locationId) { return get(locationId); }
    public Task<DataSnapshot> getAllLocationsForVendor() { return getAll(); }
}
