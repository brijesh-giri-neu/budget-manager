package edu.northeastern.numad25su_group9.services;

import edu.northeastern.numad25su_group9.models.User;
import edu.northeastern.numad25su_group9.repositories.UserRepository;

public class UserService {
    public interface OperationCallback { void onSuccess(); void onError(Exception e); }
    public interface UserCallback { void onSuccess(User user); void onError(Exception e); }

    private final UserRepository repo;

    /** Uses current authenticated user */
    public UserService() {
        String userId = new AuthService().getCurrentUserId();
        this.repo = new UserRepository(userId);
    }

    /** Overload: Accepts explicit userId */
    public UserService(String userId) {
        this.repo = new UserRepository(userId);
    }

    public void upsertUser(User user, OperationCallback cb) {
        repo.upsertUser(user)
                .addOnSuccessListener(v -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }

    public void getUser(UserCallback cb) {
        repo.getUser()
                .addOnSuccessListener(snap -> {
                    if (!snap.exists()) {
                        cb.onError(new Exception("User does not exist"));
                        return;
                    }
                    User u = snap.getValue(User.class);
                    if (u != null) cb.onSuccess(u); else cb.onError(new Exception("User data invalid"));
                })
                .addOnFailureListener(cb::onError);
    }
}
