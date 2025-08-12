package edu.northeastern.numad25su_group9.repositories;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.Map;

import edu.northeastern.numad25su_group9.models.Category;

public class CategoryRepository extends BaseRepository {
    public CategoryRepository(String userId) {
        super(FirebaseDatabase.getInstance().getReference("userCategories").child(userId));
    }

    public Task<Void> addCategory(Category category) {
        Map<String, Object> map = category.toMap();
        map.put("createdAt", ServerValue.TIMESTAMP);

        String categoryId = category.getName().toLowerCase();
        return set(categoryId, map);
    }

    public Task<Void> deleteCategory(String categoryName) {
        String categoryId = categoryName.toLowerCase();
        return delete(categoryId);
    }

    public Task<DataSnapshot> getAllCategories() { return getAll(); }
}
