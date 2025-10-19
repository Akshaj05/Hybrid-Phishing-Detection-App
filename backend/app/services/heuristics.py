SUS_TLDS = {"xyz","top","cf","tk","ml"}
def analyze_heuristics(url: str, features: dict) -> dict:
    score = 0
    reasons = []

    if features.get("is_https", 0) == 0:
        score += 15; reasons.append("no_https")
    if features.get("sus_kw_count", 0) > 0:
        score += 20; reasons.append("suspicious_keywords")
    if features.get("url_len", 0) > 100:
        score += 10; reasons.append("long_url")
    if features.get("num_dots", 0) > 4:
        score += 8; reasons.append("many_subdomains")
    if features.get("tld", "").lower() in SUS_TLDS:
        score += 10; reasons.append("risky_tld")
    if features.get("has_at", 0) == 1:
        score += 12; reasons.append("has_at_symbol")

    if score > 100: score = 100
    return {"score": score, "reasons": reasons}
