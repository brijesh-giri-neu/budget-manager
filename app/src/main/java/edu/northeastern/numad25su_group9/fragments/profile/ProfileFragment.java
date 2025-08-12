package edu.northeastern.numad25su_group9.fragments.profile;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import edu.northeastern.numad25su_group9.activities.HomeActivity;
import edu.northeastern.numad25su_group9.activities.LoginActivity;
import edu.northeastern.numad25su_group9.databinding.FragmentProfileBinding;
import edu.northeastern.numad25su_group9.models.Category;
import edu.northeastern.numad25su_group9.models.User;
import edu.northeastern.numad25su_group9.services.AuthService;
import edu.northeastern.numad25su_group9.services.CategoryService;
import edu.northeastern.numad25su_group9.services.LocationService;
import edu.northeastern.numad25su_group9.services.UserService;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";
    // View objects
    private FragmentProfileBinding binding;
    // Services
    private final UserService profileService = new UserService();
    private final CategoryService categoryService = new CategoryService();
    private final AuthService authService = new AuthService();
    /**
     * Instantiate inside constructor or will lead to NullPointerException due to dependency on this.
     */
    private LocationService locationService;

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        // Load initial data from service
        loadPersonalDetails();
        loadCategories();

        // Set Location Service
        locationService = LocationService.getInstance(this.getContext());

        // Listeners
        binding.buttonAddCategory.setOnClickListener(v -> addNewCategory());
        binding.buttonSignOut.setOnClickListener(v -> signOut());

        return binding.getRoot();
    }

    private void switchLocationAlerts(boolean isChecked) {
        if (!isAdded()) return;

        if (locationService == null) {
            toast("Location service not available!");
            Log.e(TAG, "LocationService instance is null.");
            if (binding != null) {
                binding.switchLocationAlerts.setChecked(false); // Reset switch if service unavailable
            }
            return;
        }

        if (isChecked) {
            boolean locationPermissionGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            boolean notificationPermissionGranted = true; // Assume true for older Android
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationPermissionGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
            }

            if (locationPermissionGranted && notificationPermissionGranted) {
                // All permissions are granted, proceed to start updates
                toast("Location-based alerts enabled!");
                Log.d(TAG, "Location-based alerts ENABLED");
                locationService.startLocationUpdates();
            } else {
                // Permissions are missing, ask HomeActivity to request them
                toast("Permissions needed for location alerts.");
                Log.d(TAG, "Permissions missing for location alerts. Requesting via HomeActivity.");
                if (getActivity() instanceof HomeActivity) {
                    ((HomeActivity) getActivity()).requestLocationNotificationPermissions();
                }
                // Important: Uncheck the switch immediately, as alerts can't be enabled without permissions yet.
                // It will be re-checked by loadLocationAlertPreference if permissions are granted AND user preference is true.
                if (binding != null) {
                    binding.switchLocationAlerts.setChecked(false);
                }
            }
        } else {
            // User wants to disable location alerts
            toast("Location-based alerts disabled!");
            Log.d(TAG, "Location-based alerts DISABLED");
            locationService.stopLocationUpdates();
        }
        // TODO: Update user's preference in the database.
        profileService.getUser(new UserService.UserCallback() {
            @Override
            public void onSuccess(User user) {
                user.setLocationAlerts(isChecked);
                profileService.upsertUser(user, new UserService.OperationCallback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError(Exception e) {
                        if (!isAdded()) return;
                        toast("Failed to update user preference in database");
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                toast("Failed to update user preference in dataase");
            }
        });
    }

    private void loadPersonalDetails() {
        profileService.getUser(new UserService.UserCallback() {
            @Override
            public void onSuccess(User user) {
                if (binding==null) return;
                if (user != null) {
                    binding.tvName.setText(user.getDisplayName());
                    binding.tvEmail.setText(user.getEmail());
                    binding.switchLocationAlerts.setChecked(user.isLocationAlerts());
                    // Attach listener for changes
                    binding.switchLocationAlerts.setOnCheckedChangeListener(
                            (unsued, isChecked) -> switchLocationAlerts(isChecked));
                }
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;
                toast("Error fetching user details");
            }
        });
    }

    private void loadCategories() {
        categoryService.getAllCategories(new CategoryService.CategoriesCallback() {
            @Override
            public void onSuccess(List<Category> categories) {
                if (binding==null) return;
                binding.chipGroupExpenseCategories.removeAllViews();
                for (Category c : categories)
                    addChipToGroup(binding.chipGroupExpenseCategories, c.getName());
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Failed to load categories: " + e.getMessage(), e);
                // Ensure the fragment is still attached to its activity before updating the UI.
                // Callbacks may be triggered after the user navigates away from this screen.
                if (!isAdded()) return;
                toast("Failed to load categories. "+ e.getMessage());
            }
        });
    }

    private void addNewCategory() {
        String newCategory = safeText(binding.editTextNewCategory);
        if (TextUtils.isEmpty(newCategory)) {
            toast("Please enter a category");
            return;
        }

        Category newCategoryObj = new Category();
        newCategoryObj.setName(newCategory);

        categoryService.addCategory(newCategoryObj, new CategoryService.OperationCallback(){
            @Override
            public void onSuccess() {
                if (binding==null) return;
                if (!isAdded()) return;
                addChipToGroup(binding.chipGroupExpenseCategories, newCategory);
                binding.editTextNewCategory.setText("");
                toast("Category added successfully");
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;
                toast("Failed to add category. "+ e.getMessage());
            }
        });
    }

    private void addChipToGroup(ChipGroup group, String text) {
        Chip chip = new Chip(requireContext());
        chip.setText(text);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> removeCategory(text, chip));
        group.addView(chip);
    }

    private void removeCategory(String name, Chip chip) {
        categoryService.deleteCategory(name, new CategoryService.OperationCallback() {
            @Override public void onSuccess() {
                if (binding==null) return;
                if (!isAdded()) return;

                binding.chipGroupExpenseCategories.removeView(chip);
                toast("Category removed");
            }

            @Override public void onError(@NonNull Exception e) {
                Log.e(TAG, "Delete category error", e);
                if (!isAdded()) return;
                toast("Could not remove category");
            }
        });
    }

    private void signOut() {
        authService.signOut();
        // Check if fragment is still attached before starting new activity.
        if (!isAdded()) return;
        // Redirect to LoginActivity
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK); // Clear back stack
        startActivity(intent);
        requireActivity().finish(); // Finish the current HomeActivity
    }

    private static String safeText(TextInputEditText t) {
        return t == null || t.getText() == null ? "" : t.getText().toString().trim();
    }

    private void toast(String msg) {
        requireActivity().runOnUiThread(() ->
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
