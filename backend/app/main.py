from fastapi import FastAPI
from .routes import urls
from .db import base

app = FastAPI(title="Hybrid Phishing Detector API", version="0.1")

app.include_router(urls.router, prefix="/api")

@app.get("/")
async def root():
    return {"message": "Hybrid Phishing Backend Running"}

