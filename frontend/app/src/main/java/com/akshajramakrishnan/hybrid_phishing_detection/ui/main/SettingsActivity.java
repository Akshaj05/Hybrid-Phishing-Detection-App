package com.akshajramakrishnan.hybrid_phishing_detection.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.akshajramakrishnan.hybrid_phishing_detection.R;
import com.akshajramakrishnan.hybrid_phishing_detection.util.SharedPrefManager;
import com.akshajramakrishnan.hybrid_phishing_detection.util.ThemeUtils;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends Fragment {

    private Switch themeSwitch, autoBlockSwitch;
    private Button btnBlockedList, btnChooseBrowser, btnLogout, btnChangeEmail;
    private SharedPrefManager pref;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.activity_settings, container, false);

        // 🔹 Initialize views
        themeSwitch = v.findViewById(R.id.themeSwitch);
        autoBlockSwitch = v.findViewById(R.id.autoBlockSwitch);
        btnBlockedList = v.findViewById(R.id.btnBlockedList);
        btnChooseBrowser = v.findViewById(R.id.btnChooseBrowser);
        btnLogout = v.findViewById(R.id.logoutBtn);

        // 🔹 Initialize SharedPrefManager
        pref = new SharedPrefManager(requireContext());

        // 🔹 Prevent early listener triggering during setup
        themeSwitch.setOnCheckedChangeListener(null);
        autoBlockSwitch.setOnCheckedChangeListener(null);

        // 🔹 Load saved settings
        themeSwitch.setChecked(pref.isDarkMode());
        autoBlockSwitch.setChecked(pref.isAutoBlockEnabled());

        // 🔹 Apply listeners
        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            pref.setDarkMode(isChecked);
            ThemeUtils.applyTheme(isChecked, requireActivity());
            Toast.makeText(requireContext(),
                    isChecked ? "Dark mode enabled" : "Light mode enabled",
                    Toast.LENGTH_SHORT).show();
        });

        autoBlockSwitch.setOnCheckedChangeListener((b, checked) -> {
            pref.setAutoBlockEnabled(checked);
            Toast.makeText(requireContext(),
                    "Auto-block " + (checked ? "enabled" : "disabled"),
                    Toast.LENGTH_SHORT).show();
        });

        // 🔹 Blocked list
        btnBlockedList.setOnClickListener(x ->
                startActivity(new Intent(requireContext(), BlockedListActivity.class))
        );

        // 🔹 Choose browser intent
        btnChooseBrowser.setOnClickListener(x -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("http://"));
            startActivity(Intent.createChooser(intent, "Choose default browser"));
        });

        // 🔹 Logout
        btnLogout.setOnClickListener(x -> {
            FirebaseAuth.getInstance().signOut();
            pref.logout();
            requireActivity().finish();
        });

        return v;
    }
}
