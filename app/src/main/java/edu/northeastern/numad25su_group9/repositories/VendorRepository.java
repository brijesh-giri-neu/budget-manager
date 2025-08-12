package edu.northeastern.numad25su_group9.repositories;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.Map;

import edu.northeastern.numad25su_group9.models.Vendor;

public class VendorRepository extends BaseRepository {
    public VendorRepository(String userId) {
        super(FirebaseDatabase.getInstance().getReference("userVendors").child(userId));
    }

    public Task<Void> addVendor(String vendorId, Vendor vendor) {
        Map<String, Object> map = vendor.toMap();
        map.put("createdAt", ServerValue.TIMESTAMP);
        return set(vendorId, map);
    }
    public Task<Void> deleteVendor(String vendorId) { return delete(vendorId); }
    public Task<DataSnapshot> getVendorById(String vendorId) { return get(vendorId); }
    public Task<DataSnapshot> getAllVendors() { return getAll(); }

    public Task<DataSnapshot> getVendorsByCategoryId(String categoryId) {
        return ref.orderByChild("categoryId").equalTo(categoryId).get();
    }
}
