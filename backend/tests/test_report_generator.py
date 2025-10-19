import sys
import os
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))

from app.services.report_generator import build_pdf_report

def test_report_generator():
    # Sample scan result JSON that mimics what your API returns
    scan_json = {
        "id": 6,
        "original_url": "https://paypal.com/",
        "final_url": "https://paypal.com/",
        "verdict": "suspicious",
        "score": 45,
        "heuristic_score": 35,  # Added for chart
        "reasons": ["suspicious_keywords"],
        "ml_prob": 0.62,
        "time_ms": 1090
    }
    
    # Generate the PDF report
    pdf_path = build_pdf_report(scan_json)
    
    print(f"PDF report generated at: {pdf_path}")
    
    # Open the PDF file (Windows)
    os.system(f'start {pdf_path}')

if __name__ == "__main__":
    test_report_generator()