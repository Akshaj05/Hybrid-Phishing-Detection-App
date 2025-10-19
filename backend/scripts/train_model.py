import sys, os
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..', 'app')))
import pandas as pd
from app.services.feature_extraction import extract_features
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
from sklearn.metrics import classification_report, roc_auc_score
import joblib, os

# Path to labeled CSV: columns 'url','label' (1 phishing, 0 benign)
df = pd.read_csv("backend/data/processed/labels.csv")  # adjust path

# build features
X = pd.DataFrame([extract_features(u) for u in df['url']])
if 'tld' in X.columns:
    X = X.drop(columns=['tld'])
y = df['label']

X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)
clf = RandomForestClassifier(n_estimators=200, random_state=42, n_jobs=-1)
clf.fit(X_train, y_train)
y_pred = clf.predict(X_test)
print(classification_report(y_test, y_pred))
print("ROC AUC:", roc_auc_score(y_test, clf.predict_proba(X_test)[:,1]))

# save model
os.makedirs("../models", exist_ok=True)
joblib.dump(clf, "../models/rf_model.joblib")
print("Saved model to ../models/rf_model.joblib")
