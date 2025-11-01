import sys, os
import pandas as pd
import joblib
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
from sklearn.metrics import classification_report, roc_auc_score

# --- Setup paths ---
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..', 'app')))
from app.services.feature_extraction import extract_features

# --- Load dataset ---
df = pd.read_csv("backend/data/processed/labels.csv")

# --- Build features ---
print("Extracting features...")
X = pd.DataFrame([extract_features(u) for u in df['url']])

# Drop non-numeric or unneeded columns
if 'tld' in X.columns:
    X = X.drop(columns=['tld'])

y = df['label']

# --- Split data ---
X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.2, random_state=42, stratify=y
)

clf = RandomForestClassifier(
    n_estimators=300,
    max_depth=20,
    min_samples_leaf=2,
    random_state=37,
    n_jobs=-1
)


print("Training model...")
clf.fit(X_train, y_train)

# --- Evaluate ---
y_pred = clf.predict(X_test)
proba = clf.predict_proba(X_test)[:, 1]

print("\nEvaluation Report:")
print(classification_report(y_test, y_pred, target_names=["safe (0)", "phish (1)"]))
print(f"ROC AUC: {roc_auc_score(y_test, proba):.4f}")

# --- Save model ---
MODEL_DIR = os.path.join("backend", "models")
os.makedirs(MODEL_DIR, exist_ok=True)
MODEL_PATH = os.path.join(MODEL_DIR, "rf_model.joblib")

joblib.dump(clf, MODEL_PATH)
print(f"\nSaved balanced model to {MODEL_PATH}")
