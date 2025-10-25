from fastapi import FastAPI
from .routes import urls
from .routes import report
from .db import base

app = FastAPI(title="Hybrid Phishing Detector API", version="0.1")

app.include_router(urls.router, prefix="/api")
app.include_router(report.router)

@app.get("/")
async def root():
    return {"message": "Hybrid Phishing Backend Running"}

