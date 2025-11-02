package com.akshajramakrishnan.hybrid_phishing_detection.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.akshajramakrishnan.hybrid_phishing_detection.R;
import com.akshajramakrishnan.hybrid_phishing_detection.data.model.UrlResponse;
import com.akshajramakrishnan.hybrid_phishing_detection.ui.viewmodel.MainViewModel;

public class HomeActivity extends Fragment {

    private EditText urlInput;
    private Button scanBtn;
    private MainViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_home, container, false);

        urlInput = v.findViewById(R.id.urlInput);
        scanBtn = v.findViewById(R.id.scanButton);

        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        viewModel.getScanResult().observe(getViewLifecycleOwner(), this::onScanSuccess);
        viewModel.getErrorLiveData().observe(getViewLifecycleOwner(), this::onScanError);


        scanBtn.setOnClickListener(x -> {
            String url = urlInput.getText().toString().trim();
            if (url.isEmpty()) {
                Toast.makeText(requireContext(), "Enter a URL", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.scanUrl(url);
        });

        viewModel.getIncomingUrl().observe(getViewLifecycleOwner(), incomingUrl -> {
            if (incomingUrl != null && !incomingUrl.isEmpty()) {
                urlInput.setText(incomingUrl);
                Toast.makeText(requireContext(), "Scanning external link...", Toast.LENGTH_SHORT).show();
                viewModel.scanUrl(incomingUrl);
                viewModel.setIncomingUrl(null); // reset to prevent duplicate scans
            }
        });

        return v;
    }

    private void onScanSuccess(UrlResponse res) {
        Intent i = new Intent(requireContext(), ScanResultActivity.class);
        i.putExtra("original_url", urlInput.getText().toString());
        i.putExtra("final_url", res.getFinalUrl());
        i.putExtra("verdict", res.getVerdict());
        i.putExtra("score", res.getScore());
        i.putExtra("ml_prob", res.getMlProb());    // ✅ include ML probability
        i.putExtra("time_ms", res.getTimeMs());    // ✅ include scan time

        if (res.getReasons() != null && !res.getReasons().isEmpty()) {
            i.putExtra("reasons", res.getReasons().toArray(new String[0])); // ✅ for reasons display
        }

        getContext().startActivity(i);
    }

    private void onScanError(String error) {
        Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
    }
}
