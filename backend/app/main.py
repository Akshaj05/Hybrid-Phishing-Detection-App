from fastapi import FastAPI
from app.routes import urls
from app.db import base

app = FastAPI(title="Hybrid Phishing Detector API", version="0.1")

app.include_router(urls.router, prefix="/api")

@app.get("/")
async def root():
    return {"message": "Hybrid Phishing Backend Running"}

