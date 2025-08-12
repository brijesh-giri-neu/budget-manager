package edu.northeastern.numad25su_group9.services;

import com.google.firebase.database.DataSnapshot;
import java.util.ArrayList;
import java.util.List;

import edu.northeastern.numad25su_group9.models.Vendor;
import edu.northeastern.numad25su_group9.repositories.VendorRepository;

public class VendorService {
    public interface OperationCallback { void onSuccess(); void onError(Exception e); }
    public interface VendorCallback { void onSuccess(Vendor v); void onError(Exception e); }
    public interface VendorsCallback { void onSuccess(List<Vendor> list); void onError(Exception e); }

    private final VendorRepository repo;

    public VendorService() {
        String userId = new AuthService().getCurrentUserId();
        this.repo = new VendorRepository(userId);
    }

    public void addVendor(String vendorId, Vendor v, OperationCallback cb) {
        repo.addVendor(vendorId, v)
                .addOnSuccessListener(u -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }

    public void deleteVendor(String vendorId, OperationCallback cb) {
        repo.deleteVendor(vendorId)
                .addOnSuccessListener(u -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }

    public void getVendorById(String vendorId, VendorCallback cb) {
        repo.getVendorById(vendorId)
                .addOnSuccessListener(snap -> {
                    if (!snap.exists()) {
                        cb.onError(new Exception("Vendor not found"));
                        return;
                    }
                    Vendor v = snap.getValue(Vendor.class);
                    if (v != null) cb.onSuccess(v); else cb.onError(new Exception("Vendor data invalid"));
                })
                .addOnFailureListener(cb::onError);
    }

    public void getAllVendors(VendorsCallback cb) {
        repo.getAllVendors()
                .addOnSuccessListener(snap -> {
                    if (!snap.exists()) {
                        cb.onSuccess(new ArrayList<>());
                        return;
                    }
                    List<Vendor> out = new ArrayList<>();
                    for (DataSnapshot child : snap.getChildren()) {
                        Vendor v = child.getValue(Vendor.class);
                        if (v != null) out.add(v);
                    }
                    cb.onSuccess(out);
                })
                .addOnFailureListener(cb::onError);
    }

    public void getVendorsByCategoryId(String categoryId, VendorsCallback cb) {
        repo.getVendorsByCategoryId(categoryId)
                .addOnSuccessListener(snap -> {
                    if (!snap.exists()) {
                        cb.onSuccess(new ArrayList<>());
                        return;
                    }
                    List<Vendor> out = new ArrayList<>();
                    for (DataSnapshot child : snap.getChildren()) {
                        Vendor v = child.getValue(Vendor.class);
                        if (v != null) out.add(v);
                    }
                    cb.onSuccess(out);
                })
                .addOnFailureListener(cb::onError);
    }
}
