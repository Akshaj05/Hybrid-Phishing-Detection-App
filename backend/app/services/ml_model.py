# import os, joblib, numpy as np

# MODEL_PATH = os.path.join(os.path.dirname(__file__), "..", "..", "models", "rf_model.joblib")
# FEATURE_ORDER = ["url_len","is_https","num_dots","num_hyphens","has_at","sus_kw_count","host_entropy"]

# _model = None
# def _load_model():
#     global _model
#     if _model is None:
#         if os.path.exists(MODEL_PATH):
#             _model = joblib.load(MODEL_PATH)
#         else:
#             _model = None
#     return _model

# def model_available():
#     return os.path.exists(MODEL_PATH)

# def predict_proba(features: dict) -> float:
#     m = _load_model()
#     if m is None:
#         raise RuntimeError("ML model not available")
#     X = np.array([features[c] for c in FEATURE_ORDER]).reshape(1,-1)
#     proba = m.predict_proba(X)[0][1]
#     return float(proba)

import os
import joblib
import numpy as np
import pandas as pd

def model_available():
    model_path = os.path.join("backend", "models", "rf_model.joblib")
    return os.path.exists(model_path)

def predict_proba(features):
    """Predict phishing probability using the trained model"""
    try:
        # Load the model
        model_path = os.path.join("backend", "models", "rf_model.joblib")
        model = joblib.load(model_path)
        
        # Debug information
        expected_features = model.feature_names_in_ if hasattr(model, "feature_names_in_") else None
        print(f"Model expects {len(expected_features) if expected_features is not None else 'unknown'} features")
        print(f"Received {len(features)} features: {sorted(features.keys())}")
        
        if expected_features is not None:
            print(f"Expected features: {sorted(expected_features)}")
            missing = [f for f in expected_features if f not in features]
            extra = [f for f in features if f not in expected_features]
            if missing:
                print(f"Missing features: {missing}")
            if extra:
                print(f"Extra features not used by model: {extra}")
        
        # Convert features dictionary to DataFrame with correct columns
        if expected_features is not None:
            df = pd.DataFrame({f: [features.get(f, 0)] for f in expected_features})
            return model.predict_proba(df)[0][1]
        else:
            # If we can't determine expected features, try direct prediction
            feature_values = list(features.values())
            return model.predict_proba([feature_values])[0][1]
            
    except Exception as e:
        raise Exception(f"Prediction error: {str(e)}")