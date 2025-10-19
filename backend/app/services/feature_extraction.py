import re
import socket
import whois
import datetime
from urllib.parse import urlparse

def get_domain_age(domain):
    """Returns domain age in days or -1 if lookup fails"""
    try:
        w = whois.whois(domain)
        creation_date = w.creation_date
        expiration_date = w.expiration_date
        if isinstance(creation_date, list):
            creation_date = creation_date[0]
        if isinstance(expiration_date, list):
            expiration_date = expiration_date[0]
        if not creation_date or not expiration_date:
            return -1
        age_days = (datetime.datetime.now() - creation_date).days
        return age_days
    except Exception:
        return -1

def is_domain_resolvable(domain):
    """Check if DNS resolution works"""
    try:
        socket.gethostbyname(domain)
        return 1
    except:
        return 0

def extract_features(url):
    parsed = urlparse(url)
    domain = parsed.netloc.lower()
    path = parsed.path
    query = parsed.query

    # Lexical / structural features
    features = {
        "url_length": len(url),
        "hostname_length": len(domain),
        "path_length": len(path),
        "count_digits": sum(c.isdigit() for c in url),
        "count_special_chars": sum(c in "-_@#%&?=+" for c in url),
        "count_dots": url.count('.'),
        "count_subdirs": url.count('/'),
        "count_params": url.count('&'),
        "has_ip_address": 1 if re.search(r'\d{1,3}(?:\.\d{1,3}){3}', domain) else 0,
        "has_https": 1 if parsed.scheme == "https" else 0,
        "https_in_domain": 1 if "https" in domain else 0,
        "domain_length": len(domain),
        "tld_in_subdomain": 1 if re.search(r"\.(com|net|org|info|xyz|co|ru|tk)\.", domain) else 0,
        "contains_login": 1 if "login" in url.lower() else 0,
        "contains_secure": 1 if "secure" in url.lower() else 0,
        "contains_bank": 1 if "bank" in url.lower() else 0,
        "contains_verify": 1 if "verify" in url.lower() else 0,
        "num_hyphens": url.count('-'),
        "num_equal_signs": url.count('='),
        "num_question_marks": url.count('?'),
    }

    # Host-based / WHOIS features
    features.update({
        "domain_age_days": get_domain_age(domain),
        "is_domain_resolvable": is_domain_resolvable(domain),
    })

    # Normalize failed WHOIS lookups
    if features["domain_age_days"] < 0:
        features["domain_age_days"] = 0

    return features
