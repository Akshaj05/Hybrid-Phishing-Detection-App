from fastapi import APIRouter, HTTPException
from pydantic import BaseModel, HttpUrl
from ..services.feature_extraction import extract_features
from ..services.heuristics import analyze_heuristics
from ..services.ml_model import predict_proba, model_available
from ..db import crud, base
from ..schemas.url_schema import ScanResponseSchema, ScanRequestSchema
import time

router = APIRouter()

# @router.post("/scan", response_model=ScanResponseSchema)
# def scan_url(req: ScanRequestSchema):
#     start = time.perf_counter()
#     url = str(req.url)
#     use_ml = req.use_ml

#     #Extract features
#     try:
#         features = extract_features(url)
#     except Exception as e:
#         raise HTTPException(status_code=400, detail=f"Feature extraction failed: {str(e)}")
    
#     #Heuristic scoring
#     heur = analyze_heuristics(url, features)

#     #ML prediction
#     ml_prob = None
#     if use_ml and model_available():
#         ml_prob = predict_proba(features)
    
#     #Aggregate Final Score
#     heuristic_score = heur.get("score", 0)
#     ml_score = int((ml_prob or 0) * 100)

#     final_score = int(0.6 * heuristic_score + 0.4 * ml_score)

#     if final_score < 30:
#         verdict = "safe"
#     elif final_score < 70:
#         verdict = "suspicious"
#     else:
#         verdict = "malicious"

#     #Persist to Backend DB

#     scan_record = {
#         "original_url": url,
#         "final_url": url,
#         "verdict": verdict,
#         "score": final_score,
#         "reasons": heur.get("reasons", []),
#         "ml_prob": ml_prob
#     }
#     rec = crud.create_scan(scan_record)

#     elapsed_ms = int((time.perf_counter() - start) * 1000)

#     response = {
#         "id": rec.id,
#         "original_url": url,
#         "final_url": url,
#         "verdict": verdict,
#         "score": final_score,
#         "reasons": heur.get("reasons", []),
#         "ml_prob": ml_prob,
#         "time_ms": elapsed_ms
#     }
#     return response

# @router.get("/history")
# def get_history(limit: int = 50):
#     return crud.get_history(limit = limit)

# @router.get("/report/{scan_id}")
# def get_report(scan_id: int):
#     rec = crud.get_scan(scan_id)
#     if not rec:
#         raise HTTPException(status_code=404, detail="Scan not found")   
#     return rec

@router.post("/scan", response_model=ScanResponseSchema)
def scan_url(req: ScanRequestSchema):
    start = time.perf_counter()
    url = str(req.url)
    use_ml = req.use_ml

    # Extract features
    try:
        features = extract_features(url)
    except Exception as e:
        raise HTTPException(status_code=400, detail=f"Feature extraction failed: {str(e)}")
    
    # Heuristic scoring
    try:
        heur = analyze_heuristics(url, features)
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Heuristic analysis failed: {str(e)}")

    # ML prediction
    ml_prob = None
    if use_ml and model_available():
        try:
            ml_prob = predict_proba(features)
        except Exception as e:
            raise HTTPException(status_code=500, detail=f"ML prediction failed: {str(e)}")
    
    # Calculate scores
    try:
        heuristic_score = heur.get("score", 0)
        ml_score = int((ml_prob or 0) * 100)
        final_score = int(0.6 * heuristic_score + 0.4 * ml_score)

        if final_score < 30:
            verdict = "safe"
        elif final_score < 70:
            verdict = "suspicious"
        else:
            verdict = "malicious"
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Score calculation failed: {str(e)}")

    # Persist to Backend DB
    scan_record = {
        "original_url": url,
        "final_url": url,
        "verdict": verdict,
        "score": final_score,
        "reasons": heur.get("reasons", []),
        "ml_prob": ml_prob
    }
    
    try:
        rec = crud.create_scan(scan_record)
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Database error: {str(e)}")

    elapsed_ms = int((time.perf_counter() - start) * 1000)

    try:
        response = {
            "id": rec.id,
            "original_url": url,
            "final_url": url,
            "verdict": verdict,
            "score": final_score,
            "reasons": heur.get("reasons", []),
            "ml_prob": ml_prob,
            "time_ms": elapsed_ms
        }
        return response
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Response preparation failed: {str(e)}")