from pydantic import BaseModel, HttpUrl
from typing import List, Optional

class ScanRequestSchema(BaseModel):
    url: HttpUrl
    use_ml: bool = True

class ScanResponseSchema(BaseModel):
    id: int
    original_url: HttpUrl
    final_url: HttpUrl
    verdict: str
    score: int
    reasons: List[str]
    ml_prob: Optional[float] = None
    time_ms: int