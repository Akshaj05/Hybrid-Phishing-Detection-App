import os
import tempfile
from datetime import datetime
from reportlab.lib import colors
from reportlab.lib.pagesizes import A4
from reportlab.pdfgen import canvas
import matplotlib.pyplot as plt

def draw_verdict_bar(c, x, y, width, height, score):
    """
    Draw horizontal gradient risk bar (green -> orange -> red) and a pointer.
    score: float 0..1
    """
    num_steps = 100
    step_w = width / num_steps
    for i in range(num_steps):
        ratio = i / (num_steps - 1)
        if ratio < 0.5:
            # green -> yellow
            r = ratio * 2
            g = 1.0
        else:
            # yellow -> red
            r = 1.0
            g = 1.0 - (ratio - 0.5) * 2
        c.setFillColorRGB(r, g, 0)
        c.rect(x + i * step_w, y, step_w, height, stroke=0, fill=1)

    # pointer (vertical line)
    pointer_x = x + max(0.0, min(1.0, score)) * width
    c.setStrokeColor(colors.black)
    c.setLineWidth(1.2)
    c.line(pointer_x, y - 6, pointer_x, y + height + 6)
    c.setFont("Helvetica-Bold", 10)
    c.setFillColor(colors.black)
    c.drawString(pointer_x - 18, y + height + 10, f"{score*100:.0f}%")

    # labels
    c.setFont("Helvetica", 9)
    c.setFillColor(colors.black)
    c.drawString(x, y - 16, "Safe")
    c.drawString(x + width/2 - 28, y - 16, "Suspicious")
    c.drawString(x + width - 55, y - 16, "Phishing")


def _create_score_chart(ml_prob, heuristic_score, final_score, path):
    """
    Create a clear bar chart comparing ML Probability, Heuristic Score, and Final Score.
    """
    labels = ["ML Probability", "Heuristic Score", "Final Score"]
    values = [ml_prob * 100, heuristic_score, final_score*100]

    plt.figure(figsize=(4.2, 2.8), dpi=150)
    bars = plt.bar(labels, values, width=0.4)

    plt.ylim(0, 100)
    plt.ylabel("Score (%)")
    plt.title("Score Comparison")

    for bar, val in zip(bars, values):
        plt.text(
            bar.get_x() + bar.get_width() / 2,
            val + 3,
            f"{val:.1f}%",
            ha='center',
            va='bottom',
            fontsize=9,
            fontweight='bold'
        )

    # Improve layout
    plt.tight_layout()
    plt.savefig(path, bbox_inches="tight")
    plt.close()




def generate_pdf_report(url, result_data, output_path="url_analysis_report.pdf"):
    """
    result_data expected format:
      {
        "label": "phishing" or "suspicious" or "safe",
        "score": 0.0-1.0,            # overall normalized score (0..1)
        "ml_prob": 0.0-1.0,          # model probability
        "heuristic_score": 0-100,    # heuristic numeric score (0..100)
        "features": { ... }          # features dict (optional)
      }
    """
    # ensure sensible defaults & normalization
    score = float(result_data.get("score"))
    ml_prob = float(result_data.get("ml_prob", 0.0))
    heuristic = float(result_data.get("heuristic_score", 0.0))

    # prepare PDF canvas
    c = canvas.Canvas(output_path, pagesize=A4)
    page_w, page_h = A4
    margin_left = 60
    current_y = page_h - 50

    # Title (centered, bold black)
    c.setFont("Helvetica-Bold", 22)
    c.setFillColor(colors.HexColor("#0B0B61"))  # dark blue title color
    c.drawCentredString(page_w / 2, current_y, "Phishing URL Analysis Report")
    current_y -= 28

    # subtitle / timestamp
    c.setFont("Helvetica", 10)
    c.setFillColor(colors.black)
    c.drawCentredString(page_w / 2, current_y, f"Generated on: {datetime.now().strftime('%d %B %Y, %I:%M %p')}")
    current_y -= 28

    # Scanned URL
    c.setFont("Helvetica-Bold", 12)
    c.drawString(margin_left, current_y, "Scanned URL:")
    c.setFont("Helvetica", 11)
    c.drawString(margin_left + 90, current_y, url)
    current_y -= 30

    # Prediction Summary heading
    c.setFont("Helvetica-Bold", 14)
    c.drawString(margin_left, current_y, "Prediction Summary")
    current_y -= 18

    # Prediction summary contents (black normal)
    c.setFont("Helvetica", 11)
    label_disp = str(result_data.get("verdict", "Unknown")).capitalize()
    c.drawString(margin_left + 20, current_y, f"Prediction Verdict:  {label_disp}")
    current_y -= 16
    c.drawString(margin_left + 20, current_y, f"Overall Risk Score:  {score*100:.2f}%")
    current_y -= 16
    c.drawString(margin_left + 20, current_y, f"ML Probability:      {ml_prob*100:.2f}%")
    current_y -= 16
    c.drawString(margin_left + 20, current_y, f"Heuristic Score:     {heuristic:.2f}")
    current_y -= 28

    # Risk bar
    c.setFont("Helvetica-Bold", 13)
    c.drawString(margin_left, current_y, "Phishing Risk Indicator:")
    current_y -= 18
    bar_x = margin_left + 40
    bar_w = 380
    bar_h = 18
    draw_verdict_bar(c, bar_x, current_y - bar_h, bar_w, bar_h, score)
    current_y -= (bar_h + 36)  # spacing after bar

    # Score comparison chart - TITLE FIRST
    c.setFont("Helvetica-Bold", 13)
    c.drawString(margin_left, current_y, "Score Comparison (ML vs Heuristic):")
    current_y -= 22

    # Score comparison chart (matplotlib -> temp png -> embed)
    tmp_chart = tempfile.NamedTemporaryFile(delete=False, suffix=".png")
    tmp_chart.close()
    try:
        _create_score_chart(ml_prob, heuristic, score, tmp_chart.name)
        # place the chart on PDF
        chart_x = margin_left + 20
        chart_w = 360
        chart_h = 180
        c.drawImage(tmp_chart.name, chart_x, current_y - chart_h, width=chart_w, height=chart_h)
    finally:
        # we'll delete after saving PDF
        pass

    current_y -= (chart_h + 20)

    # Key Extracted Features - bullet list, larger font for content
    features = result_data.get("features", {})
    if features:
        c.setFont("Helvetica-Bold", 13)
        c.setFillColor(colors.black)
        c.drawString(margin_left, current_y, "Key Extracted Features:")
        current_y -= 18

        # build bullet sentences
        bullets = []
        # generate readable lines (same logic as earlier helper)
        if features.get("uses_https"):
            bullets.append("The URL uses a secure HTTPS connection.")
        else:
            bullets.append("The URL does not use HTTPS, which could indicate risk.")
        if features.get("has_ip"):
            bullets.append("The URL contains an IP address instead of a proper domain, often suspicious.")
        else:
            bullets.append("The URL uses a valid domain name.")
        if "num_dots" in features:
            bullets.append(f"It includes {features['num_dots']} dots in the domain structure.")
        if "url_length" in features:
            ln = features["url_length"]
            if ln > 75:
                bullets.append(f"The URL length is {ln} characters, which is quite long and potentially suspicious.")
            else:
                bullets.append(f"The URL length is {ln} characters, within a normal range.")
        if features.get("contains_login_keyword"):
            bullets.append("It contains login-related keywords, which may be linked to credential phishing.")
        if "age_domain_days" in features:
            age = features["age_domain_days"]
            if age < 90:
                bullets.append(f"The domain is newly registered ({age} days old).")
            else:
                bullets.append(f"The domain is well-established ({age} days old).")
        if "whois_country" in features:
            bullets.append(f"The domain is registered in {features['whois_country']}.")

        # Draw bullets with slightly larger font and spacing
        text_x = margin_left + 20
        c.setFont("Helvetica", 12)  # larger font for content
        c.setFillColor(colors.black)
        leading = 18
        for i, line in enumerate(bullets, 1):
            # wrap lines manually if too long: use simple split by 90 chars
            max_chars = 90
            words = line.split()
            cur_line = ""
            for w in words:
                if len(cur_line) + 1 + len(w) <= max_chars:
                    cur_line = (cur_line + " " + w).strip()
                else:
                    c.drawString(text_x, current_y, f"• {cur_line}")
                    current_y -= leading
                    cur_line = w
                # end for
            if cur_line:
                c.drawString(text_x, current_y, f"• {cur_line}")
                current_y -= leading
        current_y -= 6

    # Final Verdict text block (bold heading, normal text)
    c.setFont("Helvetica-Bold", 13)
    c.setFillColor(colors.black)
    c.drawString(margin_left, current_y, "Final Verdict:")
    current_y -= 18
    #Add spacing below before the paragraph
    current_y -= 12

    c.setFont("Helvetica", 14)
    if score < 0.4:
        verdict_text = "This website appears SAFE based on heuristic and ML analysis."
        c.setFillColor(colors.green)
    elif score < 0.7:
        verdict_text = "This website shows SUSPICIOUS characteristics. Proceed with caution."
        c.setFillColor(colors.orange)
    else:
        verdict_text = "This website is likely PHISHING. Avoid entering sensitive data."
        c.setFillColor(colors.red)
    # Draw the verdict paragraph (wrap)
    max_chars = 100
    parts = []
    words = verdict_text.split()
    cur_line = ""
    for w in words:
        if len(cur_line) + 1 + len(w) <= max_chars:
            cur_line = (cur_line + " " + w).strip()
        else:
            parts.append(cur_line)
            cur_line = w
    if cur_line:
        parts.append(cur_line)
    for p in parts:
        c.drawString(margin_left + 20, current_y, p)
        current_y -= 14

    # Footer
    c.setFont("Helvetica-Oblique", 9)
    c.setFillColor(colors.black)
    c.drawCentredString(page_w / 2, 30, "© Hybrid Phishing Detection System by Akshaj | Confidential Report")

    # finalize PDF
    c.showPage()
    c.save()

    # cleanup temp chart
    try:
        os.remove(tmp_chart.name)
    except Exception:
        pass

    return os.path.abspath(output_path)

