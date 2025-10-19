from sqlalchemy import Column, Integer, String, DateTime, Float, JSON
from sqlalchemy.sql import func
from .base import Base

class ScanRecord(Base):
    __tablename__ = "scan_records"
    id = Column(Integer, primary_key=True, index=True)
    original_url = Column(String, nullable=False)
    final_url = Column(String, nullable=True)
    verdict = Column(String, index=True)
    score = Column(Integer)
    reasons = Column(JSON)
    ml_prob = Column(Float, nullable=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
