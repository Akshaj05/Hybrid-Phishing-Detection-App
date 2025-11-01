package com.akshajramakrishnan.hybrid_phishing_detection.ui.main;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;

public class ScanResultActivity extends AppCompatActivity {

    public static final String EXTRA_SCAN_ID = "extra_scan_id";

    private TextView verdictText, urlText, extraInfo;
    private GradiantBarView gradientBarView;
    private Button redirectBtn, saveBtn, reportBtn;
    private ImageButton backBtn;
    private String finalUrl, originalUrl, verdict;
    private int score;
    private double mlProb;
    private long timeMs;
    private String[] reasons;
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
                mlProb = intent.getDoubleExtra("ml_prob", 0.0);
                timeMs = intent.getLongExtra("time_ms", 0);
                reasons = intent.getStringArrayExtra("reasons");

                Log.d("SCAN_RESULT", "verdict=" + verdict +
                        ", score=" + score +
                        ", ml_prob=" + mlProb +
                        ", time_ms=" + timeMs +
                        ", reasons=" + Arrays.toString(reasons));

                updateUI(originalUrl, finalUrl, verdict, score, mlProb, timeMs, reasons);
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
            if (uid == null || uid.isEmpty()) {
                Toast.makeText(this, "Login required", Toast.LENGTH_SHORT).show();
                return;
            }
            BlockedUrl b = new BlockedUrl(finalUrl != null ? finalUrl : originalUrl, uid, System.currentTimeMillis());
            repo.insertBlockedUrl(b);
            Toast.makeText(this, "Added to blocked list", Toast.LENGTH_SHORT).show();
        });

        reportBtn.setOnClickListener(v -> generateReport());
    }

    private void generateReport() {
        if (originalUrl == null || verdict == null) {
            Toast.makeText(this, "Scan data not available", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Generating report...", Toast.LENGTH_SHORT).show();
        ApiService api = RetrofitClient.getApiService();

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("original_url", originalUrl);
        metadata.put("final_url", finalUrl);
        metadata.put("verdict", verdict);
        metadata.put("score", score / 100.0);
        metadata.put("ml_prob", mlProb);
        metadata.put("heuristic_score", score);
        metadata.put("time_ms", timeMs);

        Map<String, Object> features = new HashMap<>();
        features.put("has_ip", originalUrl.matches(".*\\d+\\.\\d+\\.\\d+\\.\\d+.*") ? 1 : 0);
        features.put("num_dots", originalUrl.length() - originalUrl.replace(".", "").length());
        features.put("url_length", originalUrl.length());
        features.put("contains_login_keyword", originalUrl.toLowerCase().contains("login") ? 1 : 0);
        features.put("uses_https", originalUrl.startsWith("https") ? 1 : 0);
        features.put("whois_country", "US");
        metadata.put("features", features);

        api.generateReport(metadata).enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        java.io.File reportsDir = new java.io.File(getExternalFilesDir(null), "reports");
                        if (!reportsDir.exists()) reportsDir.mkdirs();
                        String fileName = "SafeLink_Report_" + System.currentTimeMillis() + ".pdf";
                        java.io.File pdfFile = new java.io.File(reportsDir, fileName);

                        InputStream in = response.body().byteStream();
                        FileOutputStream out = new FileOutputStream(pdfFile);
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = in.read(buffer)) != -1) out.write(buffer, 0, bytesRead);
                        out.flush();
                        in.close();
                        out.close();

                        Toast.makeText(ScanResultActivity.this,
                                "Report saved: " + pdfFile.getAbsolutePath(),
                                Toast.LENGTH_LONG).show();

                        Intent openIntent = new Intent(Intent.ACTION_VIEW);
                        openIntent.setDataAndType(
                                androidx.core.content.FileProvider.getUriForFile(
                                        ScanResultActivity.this,
                                        getPackageName() + ".provider",
                                        pdfFile),
                                "application/pdf");
                        openIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(Intent.createChooser(openIntent, "Open Report"));

                    } catch (Exception e) {
                        Toast.makeText(ScanResultActivity.this,
                                "Error saving report: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ScanResultActivity.this,
                            "Failed to generate report", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(ScanResultActivity.this,
                        "Error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadScanFromDb(int id) {
        AsyncTask.execute(() -> {
            UrlScan scan = repo.getScanById(id);
            runOnUiThread(() -> {
                if (scan != null) {
                    updateUI(scan.getOriginalUrl(), scan.getFinalUrl(), scan.getVerdict(),
                            scan.getScore(), scan.getMlProb(), scan.getTimestamp(), null);
                } else {
                    Toast.makeText(this, "Scan not found", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @SuppressLint("DefaultLocale")
    private void updateUI(String original, String finalU, String verdict, int score, double mlProb, long timeMs, String[] reasons) {
        this.originalUrl = original;
        this.finalUrl = finalU;
        this.verdict = verdict;
        this.score = score;
        this.mlProb = mlProb;
        this.timeMs = timeMs;

        urlText.setText(finalU != null && !finalU.isEmpty() ? finalU : original);

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

        StringBuilder info = new StringBuilder(
                String.format("ML Probability: %.1f%% | Scan Time: %d ms", mlProb * 100, timeMs)
        );

        if (reasons != null && reasons.length > 0 &&
                (verdict.equalsIgnoreCase("suspicious") || verdict.equalsIgnoreCase("malicious"))) {
            info.append("\n\nReasons:\n");
            for (String r : reasons) {
                info.append("• ").append(r.replace("_", " ")).append("\n");
            }
        }

        extraInfo.setText(info.toString());
    }
}
