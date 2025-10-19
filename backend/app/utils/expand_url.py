import httpx

async def expand_url(url: str, timeout: float = 5.0):
    # follow redirects to final url
    try:
        async with httpx.AsyncClient(follow_redirects=True, timeout=timeout) as client:
            r = await client.head(url, follow_redirects=True)
            return r.url
    except Exception:
        return url
