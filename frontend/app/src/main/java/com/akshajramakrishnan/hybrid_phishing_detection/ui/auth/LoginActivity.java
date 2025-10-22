package com.akshajramakrishnan.hybrid_phishing_detection.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.akshajramakrishnan.hybrid_phishing_detection.R;
import com.akshajramakrishnan.hybrid_phishing_detection.ui.main.MainActivity;
import com.akshajramakrishnan.hybrid_phishing_detection.ui.viewmodel.AuthViewModel;
import com.akshajramakrishnan.hybrid_phishing_detection.ui.viewmodel.ViewModelFactory;
import com.akshajramakrishnan.hybrid_phishing_detection.util.SharedPrefManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText emailField, passwordField;
    private CheckBox rememberMe;
    private AuthViewModel authViewModel;
    private SharedPrefManager prefManager;
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth mAuth;

    // Handle Google sign-in result
    private final ActivityResultLauncher<Intent> googleLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getData() != null) {
                            var task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                            task.addOnSuccessListener(acct -> authViewModel.firebaseAuthWithGoogle(acct));
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        prefManager = new SharedPrefManager(this);

        // Auto-login check
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (prefManager.isLoggedIn() && currentUser != null && currentUser.isEmailVerified()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        rememberMe = findViewById(R.id.rememberMe);
        Button loginBtn = findViewById(R.id.loginBtn);
        Button googleBtn = findViewById(R.id.googleBtn);
        TextView signupLink = findViewById(R.id.signupLink);

        // Use ViewModelFactory
        authViewModel = new ViewModelProvider(
                this,
                new ViewModelFactory(getApplication())
        ).get(AuthViewModel.class);

        // Email/password login
        loginBtn.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();
            String pass = passwordField.getText().toString().trim();
            authViewModel.login(email, pass);
        });

        // Signup redirect
        signupLink.setOnClickListener(v -> {
            startActivity(new Intent(this, SignupActivity.class));
        });

        // Observe login results
        authViewModel.getUserLiveData().observe(this, user -> {
            if (user != null) {
                if (user.isEmailVerified()) {
                    if (rememberMe.isChecked())
                        prefManager.saveUserSession(user.getUid(), user.getEmail());
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                } else {
                    user.sendEmailVerification();
                    Toast.makeText(this, "Please verify your email. Verification sent again.", Toast.LENGTH_LONG).show();
                }
            }
        });

        authViewModel.getErrorLiveData().observe(this,
                err -> Toast.makeText(this, err, Toast.LENGTH_SHORT).show());

        // Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        googleBtn.setOnClickListener(v -> googleLauncher.launch(googleSignInClient.getSignInIntent()));
    }
}
