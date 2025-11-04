package com.akshajramakrishnan.hybrid_phishing_detection.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.akshajramakrishnan.hybrid_phishing_detection.R;
import com.akshajramakrishnan.hybrid_phishing_detection.ui.viewmodel.AuthViewModel;
import com.akshajramakrishnan.hybrid_phishing_detection.ui.viewmodel.ViewModelFactory;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

public class SignupActivity extends AppCompatActivity {

    private EditText nameField, emailField, passwordField;
    private AuthViewModel authViewModel;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        nameField = findViewById(R.id.nameField);
        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        Button signupBtn = findViewById(R.id.signupBtn);
        Button googleBtn = findViewById(R.id.googleBtn);
        TextView loginLink = findViewById(R.id.loginLink);

        authViewModel = new ViewModelProvider(
                this,
                new ViewModelFactory(getApplication())
        ).get(AuthViewModel.class);

        signupBtn.setOnClickListener(v -> {
            String name = nameField.getText().toString().trim();
            String email = emailField.getText().toString().trim();
            String pass = passwordField.getText().toString().trim();

            if (email.isEmpty() || pass.isEmpty() || name.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Firebase signup + Email verification
            authViewModel.signup(email, pass, this);
        });

        // Observe signup success
        authViewModel.getUserLiveData().observe(this, user -> {
            if (user != null) {
                user.sendEmailVerification()
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(this, "Verification email sent! Please verify before login.", Toast.LENGTH_LONG).show();

                            // 🔒 Force logout to prevent unverified login
                            FirebaseAuth.getInstance().signOut();

                            startActivity(new Intent(this, LoginActivity.class));
                            finish();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Failed to send verification: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });

        authViewModel.getErrorLiveData().observe(this,
                err -> Toast.makeText(this, err, Toast.LENGTH_SHORT).show());

        //Login redirect
        loginLink.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));

        //Google Sign-up
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        googleBtn.setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivity(signInIntent);
        });
    }
}
