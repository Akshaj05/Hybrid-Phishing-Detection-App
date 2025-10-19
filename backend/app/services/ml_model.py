import os, joblib, numpy as np

MODEL_PATH = os.path.join(os.path.dirname(__file__), "..", "..", "models", "rf_model.joblib")
FEATURE_ORDER = ["url_len","is_https","num_dots","num_hyphens","has_at","sus_kw_count","host_entropy"]

_model = None
def _load_model():
    global _model
    if _model is None:
        if os.path.exists(MODEL_PATH):
            _model = joblib.load(MODEL_PATH)
        else:
            _model = None
    return _model

def model_available():
    return os.path.exists(MODEL_PATH)

def predict_proba(features: dict) -> float:
    m = _load_model()
    if m is None:
        raise RuntimeError("ML model not available")
    X = np.array([features[c] for c in FEATURE_ORDER]).reshape(1,-1)
    proba = m.predict_proba(X)[0][1]
    return float(proba)
