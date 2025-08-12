package edu.northeastern.numad25su_group9.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;

import edu.northeastern.numad25su_group9.R;
import edu.northeastern.numad25su_group9.services.AuthService;

public class SignupActivity extends AppCompatActivity {
    // Dependencies
    private EditText emailInput, displayNameInput, passwordInput, confirmPasswordInput;
    private AuthService authService;
    // Keys for saving state
    private static final String EMAIL_KEY = "email";
    private static final String DISPLAYNAME_KEY = "displayName";
    private static final String PASSWORD_KEY = "password";
    private static final String CONFIRM_KEY = "confirm";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        authService = new AuthService();

        emailInput = findViewById(R.id.email_input);
        displayNameInput = findViewById(R.id.displayName_input);
        passwordInput = findViewById(R.id.password_input);
        confirmPasswordInput = findViewById(R.id.confirm_password_input);
        Button signupButton = findViewById(R.id.signup_button);

        // Restore state on rotation
        if (savedInstanceState != null) {
            emailInput.setText(savedInstanceState.getString(EMAIL_KEY, ""));
            displayNameInput.setText(savedInstanceState.getString(DISPLAYNAME_KEY, ""));
            passwordInput.setText(savedInstanceState.getString(PASSWORD_KEY, ""));
            confirmPasswordInput.setText(savedInstanceState.getString(CONFIRM_KEY, ""));
        }

        signupButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String displayName = displayNameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String confirmPassword = confirmPasswordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            authService.signup(email, displayName, password, new AuthService.AuthCallback() {
                @Override
                public void onSuccess(FirebaseUser user) {
                    Toast.makeText(SignupActivity.this, "Signup successful", Toast.LENGTH_SHORT).show();
                    launchHomeActivityAndClearBackStack();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(SignupActivity.this, "Signup failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void launchHomeActivityAndClearBackStack() {
        Intent intent = new Intent(SignupActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EMAIL_KEY, emailInput.getText().toString());
        outState.putString(DISPLAYNAME_KEY, displayNameInput.getText().toString());
        outState.putString(PASSWORD_KEY, passwordInput.getText().toString());
        outState.putString(CONFIRM_KEY, confirmPasswordInput.getText().toString());
    }
}
