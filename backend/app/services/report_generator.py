import matplotlib.pyplot as plt
from reportlab.lib.pagesizes import letter
from reportlab.pdfgen import canvas
import tempfile, os

def build_chart(scores: dict):
    # scores: {'heuristic': 40, 'ml': 60, 'final': 52}
    tmp = tempfile.NamedTemporaryFile(delete=False, suffix=".png")
    path = tmp.name; tmp.close()
    keys = list(scores.keys()); vals = [scores[k] for k in keys]
    plt.figure(figsize=(4,2))
    plt.bar(keys, vals)
    plt.ylim(0,100)
    plt.tight_layout()
    plt.savefig(path)
    plt.close()
    return path

def build_pdf_report(scan_json: dict):
    pdf_path = tempfile.NamedTemporaryFile(delete=False, suffix=".pdf").name
    c = canvas.Canvas(pdf_path, pagesize=letter)
    c.setFont("Helvetica-Bold", 16)
    c.drawString(50, 750, "HybridPhishDetector - Scan Report")
    c.setFont("Helvetica", 11)
    y = 720
    for k,v in scan_json.items():
        c.drawString(50, y, f"{k}: {v}")
        y -= 15
    chart = build_chart({"heuristic": scan_json.get("heuristic_score",0),
                         "ml": int((scan_json.get("ml_prob") or 0)*100),
                         "final": scan_json.get("score",0)})
    c.drawImage(chart, 50, y-220, width=500, height=200)
    c.showPage()
    c.save()
    os.remove(chart)
    return pdf_path
