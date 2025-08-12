package edu.northeastern.numad25su_group9.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import edu.northeastern.numad25su_group9.R;
import edu.northeastern.numad25su_group9.services.AuthService;

public class LoginActivity extends AppCompatActivity {
    // Dependencies
    private EditText emailInput, passwordInput;
    private AuthService authService;
    // Keys for saving state
    private static final String EMAIL_KEY = "email";
    private static final String PASSWORD_KEY = "password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authService = new AuthService();

        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        Button loginButton = findViewById(R.id.login_button);
        Button signupButton = findViewById(R.id.signup_button);
        Button guestButton = findViewById(R.id.guest_button);

        // Restore state on rotation
        if (savedInstanceState != null) {
            emailInput.setText(savedInstanceState.getString(EMAIL_KEY));
            passwordInput.setText(savedInstanceState.getString(PASSWORD_KEY));
        }

        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email and password are required", Toast.LENGTH_SHORT).show();
                return;
            }

            authService.login(email, password, new AuthService.AuthCallback() {
                @Override
                public void onSuccess(com.google.firebase.auth.FirebaseUser user) {
                    launchHomeActivity();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(LoginActivity.this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        guestButton.setOnClickListener(v -> {
            authService.signInAnonymously(new AuthService.AuthCallback() {
                @Override
                public void onSuccess(com.google.firebase.auth.FirebaseUser user) {
                    launchHomeActivity();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(LoginActivity.this, "Guest login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        signupButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }

    private void launchHomeActivity() {
        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
        finish();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EMAIL_KEY, emailInput.getText().toString());
        outState.putString(PASSWORD_KEY, passwordInput.getText().toString());
    }
}
