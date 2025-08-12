package edu.northeastern.numad25su_group9.services;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import edu.northeastern.numad25su_group9.models.Budget;
import edu.northeastern.numad25su_group9.models.Category;
import edu.northeastern.numad25su_group9.models.Types.Theme;
import edu.northeastern.numad25su_group9.models.User;

/**
 * Uses Firebase Authentication to authenticate users and maintain server-side sessions.
 *
 * <p>
 *     NOTE: Firebase Auth caches the logged-in user locally on the device using an ID token and
 *     a corresponding expiry token. ID token gets automatically refreshed in the background upon
 *     refresh token expiry.
 * </p>
 */
public class AuthService {
    private static final String TAG = "AuthService";
    private final FirebaseAuth mAuth;

    public AuthService() {
        mAuth = FirebaseAuth.getInstance();
    }

    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onFailure(Exception e);
    }

    public void login(String email, String password, AuthCallback callback) {
        Log.d(TAG, "Attempting login for: " + email);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Login successful.");
                        callback.onSuccess(mAuth.getCurrentUser());
                    } else {
                        Log.e(TAG, "Login failed: ", task.getException());
                        callback.onFailure(task.getException());
                    }
                });
    }

    public void signInAnonymously(AuthCallback callback) {
        mAuth.signInAnonymously()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(mAuth.getCurrentUser());
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    public void signup(String email, String displayName, String password, AuthCallback callback) {
        Log.d(TAG, "Attempting signup for: " + email);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Signup successful.");
                        // Push data to Users table.
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser == null) {
                            callback.onFailure(new IllegalStateException("Firebase user is null after signup"));
                            return;
                        }

                        // Build domain user
                        firebaseUser.updateProfile(new UserProfileChangeRequest.Builder()
                            .setDisplayName(displayName).build());
                        User user = new User();
                        user.setEmail(firebaseUser.getEmail());
                        user.setDisplayName(displayName);
                        user.setTheme(String.valueOf(Theme.LIGHT));
                        // False for new user
                        user.setLocationAlerts(false);
                        user.setAnonymous(false);

                        UserService userService = new UserService(firebaseUser.getUid());
                        userService.upsertUser(user, new UserService.OperationCallback() {
                            @Override public void onSuccess() {
                                // 2. Seed default categories
                                seedDefaultCategories(firebaseUser.getUid(), new UserService.OperationCallback() {
                                    @Override public void onSuccess() {
                                        Log.d(TAG, "Default categories seeded successfully. Seeding default budget.");
                                        // 3. Seed default budget
                                        seedDefaultBudget(firebaseUser.getUid(), new UserService.OperationCallback() {
                                            @Override public void onSuccess() {
                                                Log.d(TAG, "Default budget seeded successfully. Signup complete.");
                                                // Only report auth success once ALL DB writes have succeeded
                                                callback.onSuccess(firebaseUser);
                                            }

                                            @Override public void onError(Exception e) {
                                                Log.e(TAG, "Seeding default budget failed", e);
                                                callback.onFailure(e);
                                            }
                                        });
                                    }

                                    @Override public void onError(Exception e) {
                                        Log.e(TAG, "Seeding default categories failed", e);
                                        callback.onFailure(e);
                                    }
                                });
                            }

                            @Override public void onError(Exception e) {
                                Log.e(TAG, "Upsert user failed", e);
                                callback.onFailure(e);
                            }
                        });
                    } else {
                        Log.e(TAG, "Signup failed: ", task.getException());
                        callback.onFailure(task.getException());
                    }
                });
    }

    /**
     * Seeds default categories for a new user.
     * @param userId The UID of the new user.
     * @param callback Callback for operation success/failure.
     */
    private void seedDefaultCategories(String userId, UserService.OperationCallback callback) {
        CategoryService categoryService = new CategoryService();

        // Define common default categories
        List<String> defaultCategoryNames = Arrays.asList(
                "Food", "Transportation", "Shopping", "Entertainment", "Utilities", "Rent"
        );

        // Chain additions or use a counter for multiple async ops if needed.
        // For simplicity, we'll iterate and use a simple callback for each.
        // A more robust solution might track completion of all additions.
        final int[] categoriesAdded = {0};
        final int totalCategories = defaultCategoryNames.size();

        for (String name : defaultCategoryNames) {
            Category category = new Category();
            category.setName(name);
            // Assuming CategoryService supports adding for a specific user ID
            categoryService.addCategory(category, new CategoryService.OperationCallback() {
                @Override
                public void onSuccess() {
                    categoriesAdded[0]++;
                    if (categoriesAdded[0] == totalCategories) {
                        callback.onSuccess();
                    }
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "Failed to add default category " + name + ": " + e.getMessage(), e);
                    // Decide how to handle individual category failures: fail whole operation or continue
                    callback.onError(e); // Propagate first error
                }
            });
        }
    }


    /**
     * Seeds a default budget for a new user for the current month.
     * @param userId The UID of the new user.
     * @param callback Callback for operation success/failure.
     */
    private void seedDefaultBudget(String userId, UserService.OperationCallback callback) {
        BudgetService budgetService = new BudgetService();

        double defaultBudgetAmount = 0.00;

        Budget defaultBudget = Budget.builder()
                .setAmount(defaultBudgetAmount)
                .setMonthDateLocal(LocalDateTime.now()) // Set the current date/time for local reference
                .build();

        budgetService.updateBudget(defaultBudget, new BudgetService.OperationCallback() {
            @Override
            public void onSuccess() {
                callback.onSuccess();
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Failed to seed default budget: " + e.getMessage(), e);
                callback.onError(e);
            }
        });
    }

    /**
     * Returns the currently logged-in Firebase user.
     *
     * <p><strong>Note:</strong> This method does not trigger a network request.</p>
     *
     * <p>
     * Firebase Authentication caches the logged-in user locally on the device using an ID token
     * and a refresh token. These tokens are automatically refreshed in the background upon expiry.
     * As a result, calling this method will not perform a network operation and is efficient for
     * frequent use when session information is needed.
     * </p>
     *
     * <p>
     * <strong>Tip:</strong> The only practical reason to cache the currently logged-in user in a
     *  singleton is if other additional data is already being managed there. So that we have a
     *  single source of truth for cached, application-level user data.
     *
     * @return the currently logged-in {@link FirebaseUser}, or {@code null} if no user is logged in.
     */
    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    /**
     * Returns firebase UUID of currently logged in user or null if no user is logged in.
     * @return firebase UUID of currently logged in user.
     */
    public String getCurrentUserId() {
        if (mAuth.getCurrentUser() == null) {
            return null;
        }
        return mAuth.getCurrentUser().getUid();
    }

    public void signOut() {
        Log.d(TAG, "Signing out user");
        mAuth.signOut();
    }
}
