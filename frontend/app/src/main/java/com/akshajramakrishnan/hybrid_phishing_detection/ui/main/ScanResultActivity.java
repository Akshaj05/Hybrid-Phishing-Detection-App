package com.akshajramakrishnan.hybrid_phishing_detection.ui.main;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
    private int heuristicScore;

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

        //This part handles both cases: loading from DB or from intent extras
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
                heuristicScore = intent.getIntExtra("heuristic_score", 0);

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

            Uri uri = Uri.parse(finalUrl);


            String[] browsers = new String[]{
                    "com.android.chrome",
                    "com.sec.android.app.sbrowser",
                    "org.mozilla.firefox",
                    "com.brave.browser",
                    "com.opera.browser",
                    "com.microsoft.emmx"
            };

            boolean opened = false;

            for (String browser : browsers) {
                try {
                    Intent intent1 = new Intent(Intent.ACTION_VIEW, uri);
                    intent1.setPackage(browser);
                    intent1.addCategory(Intent.CATEGORY_BROWSABLE);
                    intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getPackageManager().getPackageInfo(browser, 0); // check if installed
                    startActivity(intent1);
                    Toast.makeText(this, "Opening in " + browser, Toast.LENGTH_SHORT).show();
                    opened = true;
                    break;
                } catch (Exception ignored) { }
            }

            if (!opened) {
                try {
                    Intent fallback = new Intent(Intent.ACTION_VIEW, uri);
                    fallback.addCategory(Intent.CATEGORY_BROWSABLE);
                    fallback.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(fallback);
                    Toast.makeText(this, "Opening in default browser", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "No browser found!", Toast.LENGTH_SHORT).show();
                }
            }
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
        metadata.put("heuristic_score", heuristicScore);
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
                        //Create folder and save PDF
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

                        //URI for fileProvider which handles file access permissions
                        Uri uri = androidx.core.content.FileProvider.getUriForFile(
                                ScanResultActivity.this,
                                getPackageName() + ".provider",
                                pdfFile
                        );

                        //intent to open PDF
                        Intent openIntent = new Intent(Intent.ACTION_VIEW);
                        openIntent.setDataAndType(uri, "application/pdf");
                        openIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NO_HISTORY);

                        //Auto open if viewer exists
                        if (openIntent.resolveActivity(getPackageManager()) != null) {
                            startActivity(openIntent);
                        } else {
                            Toast.makeText(ScanResultActivity.this,
                                    "PDF saved but no viewer app installed.\nFile: " + pdfFile.getAbsolutePath(),
                                    Toast.LENGTH_LONG).show();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
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
                String.format("Heuristic Score: %d | ML Probability: %.1f%% | Scan Time: %d ms",heuristicScore, mlProb * 100, timeMs)
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
