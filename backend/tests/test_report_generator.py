# test_report_generator.py
import os
import sys
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))

from app.services.report_generator import generate_pdf_report

def test_report_generator():
    scan_json = {
        "id": 6,
        "original_url": "https://paypal.com/",
        "final_url": "https://paypal.com/",
        "verdict": "suspicious",
        # NOTE: score normalized 0..1
        "score": 0.45,
        # ML probability normalized 0..1
        "ml_prob": 0.62,
        # heuristic score in 0..100
        "heuristic_score": 35,
        "time_ms": 1090,
        "reasons": ["suspicious_keywords", "unusual_redirect"],
        "features": {
            "has_ip": 0,
            "num_dots": 2,
            "url_length": 28,
            "contains_login_keyword": 1,
            "age_domain_days": 45,
            "uses_https": 1,
            "whois_country": "US"
        }
    }

    out = generate_pdf_report(scan_json["original_url"], scan_json, output_path=f"report_test_{scan_json['id']}.pdf")
    print("Generated:", out)

    # Auto-open on Windows (works in PowerShell cmd)
    try:
        os.system(f"start {out}")
    except Exception:
        print("PDF created at:", out)


if __name__ == "__main__":
    test_report_generator()
