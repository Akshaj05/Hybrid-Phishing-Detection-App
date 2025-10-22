package com.akshajramakrishnan.hybrid_phishing_detection.ui.main;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.akshajramakrishnan.hybrid_phishing_detection.R;
import com.akshajramakrishnan.hybrid_phishing_detection.ui.viewmodel.AuthViewModel;
import com.akshajramakrishnan.hybrid_phishing_detection.ui.viewmodel.MainViewModel;
import com.akshajramakrishnan.hybrid_phishing_detection.ui.viewmodel.ViewModelFactory;

public class MainActivity extends AppCompatActivity {

    private MainViewModel viewModel;
    private EditText urlInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        urlInput = findViewById(R.id.urlInput);
        Button scanBtn = findViewById(R.id.scanBtn);

        viewModel = new ViewModelProvider(
                this,
                new ViewModelFactory(getApplication())
        ).get(MainViewModel.class);

        scanBtn.setOnClickListener(v -> {
            String url = urlInput.getText().toString().trim();
            if (url.isEmpty()) {
                Toast.makeText(this, "Enter a URL", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.scanUrl(url);
        });

        viewModel.getScanResult().observe(this, result ->
                Toast.makeText(this,
                        "Verdict: " + result.getVerdict() + "\nScore: " + result.getScore(),
                        Toast.LENGTH_LONG).show());

        viewModel.getErrorLiveData().observe(this,
                err -> Toast.makeText(this, err, Toast.LENGTH_SHORT).show());
    }
}
