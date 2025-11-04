from fastapi import APIRouter, Body
from fastapi.responses import FileResponse, JSONResponse
import os, time, uuid, sys

sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../..')))
from app.services.report_generator import generate_pdf_report

router = APIRouter(prefix="/report", tags=["Report"])

@router.post("/")
async def generate_report(scan_json: dict = Body(...)):
    """
    Accepts a full scan JSON from the client and returns a generated PDF report.
    """
    try:
        url = scan_json.get("original_url", "unknown")

        output_dir = os.path.join("reports")
        os.makedirs(output_dir, exist_ok=True)
        output_path = os.path.join(output_dir, f"report_{uuid.uuid4().hex}.pdf")

        pdf_path = generate_pdf_report(url, scan_json, output_path=output_path)

        if not os.path.exists(pdf_path):
            return JSONResponse({"error": "Report generation failed"}, status_code=500)

        return FileResponse(
            pdf_path,
            media_type="application/pdf",
            filename=os.path.basename(pdf_path)
        )

    except Exception as e:
        return JSONResponse({"error": str(e)}, status_code=500)
