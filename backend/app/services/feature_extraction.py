from urllib.parse import urlparse
import tldextract, math
SUS_KWS = ["login","secure","account","update","confirm","bank","verify"]

# Full Explanation of hostname entropy calculation:
# The hostname entropy is a measure of the unpredictability or randomness of the hostname part of a URL.
# It is calculated using the Shannon entropy formula, which takes into account the frequency of each character
# in the hostname. A higher entropy value indicates a more complex and less predictable hostname, which is often
# associated with malicious or phishing URLs.
def hostname_entropy(s: str) -> float:
    if not s: return 0.0
    from collections import Counter
    cnt = Counter(s); n=len(s)
    return -sum((v/n)*math.log2(v/n) for v in cnt.values())

def extract_features(url: str) -> dict:
    p = urlparse(url)
    host = p.hostname or ""
    ext = tldextract.extract(url) #tldextract is used because it handles subdomains and TLDs better than urlparse
    features = {
        "url_len": len(url),
        "is_https": 1 if p.scheme == "https" else 0,
        "num_dots": host.count("."),
        "num_hyphens": host.count("-"),
        "has_at": 1 if "@" in url else 0,
        "sus_kw_count": sum(1 for k in SUS_KWS if k in url.lower()),
        "host_entropy": hostname_entropy(host),
        "tld": ext.suffix or ""
    }
    return features
