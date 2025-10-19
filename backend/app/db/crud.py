from .base import SessionLocal, engine, Base
from .models import ScanRecord
from sqlalchemy.orm import Session
Base.metadata.create_all(bind=engine)

def create_scan(data: dict):
    db: Session = SessionLocal()
    rec = ScanRecord(
        original_url=data.get("original_url"),
        final_url=data.get("final_url"),
        verdict=data.get("verdict"),
        score=data.get("score"),
        reasons=data.get("reasons"),
        ml_prob=data.get("ml_prob")
    )
    db.add(rec)
    db.commit()
    db.refresh(rec)
    db.close()
    return rec

def get_history(limit: int = 50):
    db: Session = SessionLocal()
    rows = db.query(ScanRecord).order_by(ScanRecord.created_at.desc()).limit(limit).all()
    db.close()
    return rows

def get_scan(scan_id: int):
    db: Session = SessionLocal()
    rec = db.query(ScanRecord).filter(ScanRecord.id == scan_id).first()
    db.close()
    return rec
