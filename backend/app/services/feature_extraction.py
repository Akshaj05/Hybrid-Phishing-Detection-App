from collections import Counter
import math
import re
import socket
import whois
import datetime
from urllib.parse import urlparse

def calculate_entropy(text):
    if not text:
        return 0
    entropy = 0
    text_len = len(text)
    # Count character frequencies
    char_counts = Counter(text)
    # Calculate entropy
    for count in char_counts.values():
        probability = count / text_len
        entropy -= probability * math.log2(probability)
    return entropy

def get_domain_age(domain):
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

    # entropy of the hostname
    host_entropy = calculate_entropy(domain)

    #suspicious keywords
    suspicious_keywords = [
        'login', 'secure', 'bank', 'verify', 'account', 'update', 'confirm',
        'password', 'wallet', 'alert', 'authenticate', 'verification'
    ]
    
    # count suspicious keywords
    url_lower = url.lower()
    sus_kw_count = sum(1 for kw in suspicious_keywords if kw in url_lower)

    # structural features
    features = {
        "url_len": len(url),
        "hostname_length": len(domain),
        "path_length": len(path),
        "count_digits": sum(c.isdigit() for c in url),
        "count_special_chars": sum(c in "-_@#%&?=+" for c in url),
        "num_dots": url.count('.'),
        "count_subdirs": url.count('/'),
        "count_params": url.count('&'),
        "has_ip_address": 1 if re.search(r'\d{1,3}(?:\.\d{1,3}){3}', domain) else 0,
        "is_https": 1 if parsed.scheme == "https" else 0,
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
        "has_at": 1 if '@' in url else 0,
        "sus_kw_count": sus_kw_count,
        "host_entropy": host_entropy,
    }

    # WHOIS features
    features.update({
        "domain_age_days": get_domain_age(domain),
        "is_domain_resolvable": is_domain_resolvable(domain),
    })

    if features["domain_age_days"] < 0:
        features["domain_age_days"] = 0

    return features
