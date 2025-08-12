package edu.northeastern.numad25su_group9.services;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.numad25su_group9.models.Category;
import edu.northeastern.numad25su_group9.repositories.CategoryRepository;

public class CategoryService {
    private final CategoryRepository repo;

    public CategoryService() {
        String userId = new AuthService().getCurrentUserId();
        this.repo = new CategoryRepository(userId);
    }

    // Callbacks
    public interface OperationCallback { void onSuccess(); void onError(Exception e); }
    public interface CategoriesCallback { void onSuccess(List<Category> categories); void onError(Exception e); }

    // Methods
    public void addCategory(Category c, OperationCallback cb) {
        repo.addCategory(c)
                .addOnSuccessListener(u -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }

    public void deleteCategory(String categoryName, OperationCallback cb) {
        repo.deleteCategory(categoryName)
                .addOnSuccessListener(u -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }

    public void getAllCategories(CategoriesCallback cb) {
        repo.getAllCategories()
                .addOnSuccessListener(snap -> {
                    // Return default value
                    if (!snap.exists()) {
                        cb.onSuccess(new ArrayList<>());
                        return;
                    }
                    List<Category> out = new ArrayList<>();
                    for (DataSnapshot child : snap.getChildren()) {
                        Category c = child.getValue(Category.class);
                        if (c != null) {
                            out.add(c);
                        }
                    }
                    cb.onSuccess(out);
                })
                .addOnFailureListener(cb::onError);
    }

    public static String getCategoryIdFromName(String categoryName) {
        return categoryName.toLowerCase();
    }
}
