package com.akshajramakrishnan.hybrid_phishing_detection.ui.main;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.akshajramakrishnan.hybrid_phishing_detection.R;
import com.akshajramakrishnan.hybrid_phishing_detection.data.model.BlockedUrl;
import com.akshajramakrishnan.hybrid_phishing_detection.data.model.UrlScan;
import com.akshajramakrishnan.hybrid_phishing_detection.data.repository.MainRepository;
import com.akshajramakrishnan.hybrid_phishing_detection.network.ApiService;
import com.akshajramakrishnan.hybrid_phishing_detection.network.RetrofitClient;
import com.akshajramakrishnan.hybrid_phishing_detection.ui.widget.GradiantBarView;
import com.akshajramakrishnan.hybrid_phishing_detection.util.SharedPrefManager;

import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;

public class ScanResultActivity extends AppCompatActivity {

    public static final String EXTRA_SCAN_ID = "extra_scan_id";

    private TextView verdictText, scoreText, urlText, extraInfo;
    private GradiantBarView gradientBarView;
    private Button redirectBtn, saveBtn, reportBtn;
    private ImageButton backBtn;
    private String finalUrl, originalUrl, verdict;
    private int score;
    private MainRepository repo;
    private SharedPrefManager pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_result);

        repo = new MainRepository(this);
        pref = new SharedPrefManager(this);

        urlText = findViewById(R.id.urlText);
        verdictText = findViewById(R.id.verdictText);
        scoreText = findViewById(R.id.scoreText);
        gradientBarView = findViewById(R.id.gradientBarView);
        extraInfo = findViewById(R.id.extraInfo);

        redirectBtn = findViewById(R.id.redirectBtn);
        saveBtn = findViewById(R.id.saveBtn);
        reportBtn = findViewById(R.id.reportBtn);
        backBtn = findViewById(R.id.backBtn);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(EXTRA_SCAN_ID)) {
                int id = intent.getIntExtra(EXTRA_SCAN_ID, -1);
                if (id != -1) loadScanFromDb(id);
            } else if (intent.hasExtra("verdict")) {
                originalUrl = intent.getStringExtra("original_url");
                finalUrl = intent.getStringExtra("final_url");
                verdict = intent.getStringExtra("verdict");
                score = intent.getIntExtra("score", 0);
                updateUI(originalUrl, finalUrl, verdict, score);
            }
        }

        backBtn.setOnClickListener(v -> onBackPressed());

        redirectBtn.setOnClickListener(v -> {
            if (finalUrl == null || finalUrl.isEmpty()) {
                Toast.makeText(this, "No URL to open", Toast.LENGTH_SHORT).show();
                return;
            }
            String pkg = pref.getDefaultBrowserPackage();
            Intent open;
            if (pkg != null && !pkg.isEmpty()) {
                open = new Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl));
                open.setPackage(pkg);
            } else {
                open = new Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl));
            }
            startActivity(open);
        });

        saveBtn.setOnClickListener(v -> {
            String uid = pref.getUid();
            if (uid == null || uid.isEmpty()) { Toast.makeText(this, "Login required", Toast.LENGTH_SHORT).show(); return; }
            BlockedUrl b = new BlockedUrl(finalUrl != null ? finalUrl : originalUrl, uid, System.currentTimeMillis());
            repo.insertBlockedUrl(b);
            Toast.makeText(this, "Added to blocked list", Toast.LENGTH_SHORT).show();
        });

        reportBtn.setOnClickListener(v -> {
            Toast.makeText(this, "Generating report...", Toast.LENGTH_SHORT).show();

            ApiService api = RetrofitClient.getApiService();

            // Build scan_json like your Python test_report_generator.py
            Map<String, Object> scanJson = new HashMap<>();
            scanJson.put("id", System.currentTimeMillis());
            scanJson.put("original_url", originalUrl);
            scanJson.put("final_url", finalUrl);
            scanJson.put("verdict", verdict);
            scanJson.put("score", score / 100.0);  // normalize to 0–1
            scanJson.put("ml_prob", 0.62); // replace with actual ML prob if available
            scanJson.put("heuristic_score", score);
            scanJson.put("time_ms", 1090);

            // example nested objects
            scanJson.put("reasons", new String[]{"keyword_check", "ssl_mismatch"});
            Map<String, Object> features = new HashMap<>();
            features.put("has_ip", 0);
            features.put("num_dots", 3);
            features.put("url_length", originalUrl.length());
            features.put("contains_login_keyword", originalUrl.contains("login") ? 1 : 0);
            features.put("age_domain_days", 40);
            features.put("uses_https", originalUrl.startsWith("https") ? 1 : 0);
            features.put("whois_country", "US");
            scanJson.put("features", features);

            api.generateReport(scanJson).enqueue(new retrofit2.Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            // Save PDF locally
                            java.io.File downloadsDir = new java.io.File(getExternalFilesDir(null), "reports");
                            if (!downloadsDir.exists()) downloadsDir.mkdirs();

                            String fileName = "SafeLink_Report_" + System.currentTimeMillis() + ".pdf";
                            java.io.File pdfFile = new java.io.File(downloadsDir, fileName);

                            java.io.InputStream inputStream = response.body().byteStream();
                            java.io.OutputStream outputStream = new java.io.FileOutputStream(pdfFile);

                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                            }
                            outputStream.flush();
                            inputStream.close();
                            outputStream.close();

                            Toast.makeText(ScanResultActivity.this, "Report saved: " + pdfFile.getAbsolutePath(), Toast.LENGTH_LONG).show();

                            Intent openIntent = new Intent(Intent.ACTION_VIEW);
                            openIntent.setDataAndType(androidx.core.content.FileProvider.getUriForFile(
                                            ScanResultActivity.this,
                                            getPackageName() + ".provider",
                                            pdfFile),
                                    "application/pdf");
                            openIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(Intent.createChooser(openIntent, "Open Report"));

                        } catch (Exception e) {
                            Toast.makeText(ScanResultActivity.this, "Error saving report: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ScanResultActivity.this, "Failed to generate report", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(ScanResultActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });


    }

    private void loadScanFromDb(int id) {
        AsyncTask.execute(() -> {
            UrlScan scan = repo.getScanById(id);
            runOnUiThread(() -> {
                if (scan != null) updateUI(scan.getOriginalUrl(), scan.getFinalUrl(), scan.getVerdict(), scan.getScore());
                else Toast.makeText(this, "Scan not found", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void updateUI(String original, String finalU, String verdict, int score) {
        this.originalUrl = original;
        this.finalUrl = finalU;
        this.verdict = verdict;
        this.score = score;

        urlText.setText(finalU != null && !finalU.isEmpty() ? finalU : original);
        this.finalUrl = (finalU != null && !finalU.isEmpty()) ? finalU : original;

        // 🎨 Verdict color logic
        if (verdict == null) verdict = "unknown";
        verdictText.setText("Verdict: " + verdict);

        switch (verdict.toLowerCase()) {
            case "safe":
                verdictText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                break;
            case "suspicious":
                verdictText.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                break;
            case "malicious":
            case "phishing":
                verdictText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                break;
            default:
                verdictText.setTextColor(getResources().getColor(android.R.color.darker_gray));
        }

        if (gradientBarView != null) gradientBarView.setScore(score);
        extraInfo.setText("ML Probability: — | Time: — ms");
    }

}
