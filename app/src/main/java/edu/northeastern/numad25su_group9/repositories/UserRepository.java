package edu.northeastern.numad25su_group9.repositories;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.Map;

import edu.northeastern.numad25su_group9.models.User;

public class UserRepository extends BaseRepository {
    public UserRepository(String userId) {
        super(FirebaseDatabase.getInstance().getReference("users").child(userId));
    }

    public Task<Void> upsertUser(User user) {
        Map<String, Object> map = user.toMap();
        map.put("modifiedAt", ServerValue.TIMESTAMP);
        return set("", map);
    }

    public Task<DataSnapshot> getUser() { return get(""); }
}
