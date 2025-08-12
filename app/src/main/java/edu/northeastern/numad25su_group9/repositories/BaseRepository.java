package edu.northeastern.numad25su_group9.repositories;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.util.Map;

/**
 * Provides base functionality for all repositories.
 */
public abstract class BaseRepository {
    // TODO: Migrate to Firebase Firestore Database to enable advanced query capabilities.
    protected final DatabaseReference ref;

    public BaseRepository(DatabaseReference ref) {
        this.ref = ref;
    }

    protected Task<Void> set(String key, Map<String, Object> value) {
        return ref.child(key).setValue(value);
    }

    protected Task<Void> update(String key, Map<String, Object> updates) {
        return ref.child(key).updateChildren(updates);
    }

    protected Task<Void> delete(String key) {
        return ref.child(key).removeValue();
    }

    protected Task<DataSnapshot> get(String key) {
        return ref.child(key).get();
    }

    protected Task<DataSnapshot> getAll() {
        return ref.get();
    }
}
