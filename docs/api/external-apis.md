# API Comparison: Kakao vs Naver vs Google Places

> **⚠️ DEPRECATION NOTICE (2025-11-04)**
>
> **Kakao API is NO LONGER USED in Oddiya.**
>
> The project now uses **Google Gemini AI** for all travel planning, which generates comprehensive Korea travel itineraries without needing external location APIs. Gemini has built-in knowledge of Korean destinations, restaurants, and attractions.
>
> This document is kept for **reference only** in case future expansion requires external location APIs.

---

## Quick Summary for Oddiya

| Feature | Kakao Local API | Naver Places API | Google Places API |
|---------|----------------|------------------|-------------------|
| **Target Region** | Korea (Primary) | Korea (Excellent) | Global |
| **Korean Support** | ⭐⭐⭐⭐⭐ Native | ⭐⭐⭐⭐⭐ Native | ⭐⭐⭐ Good |
| **Free Tier** | ⭐⭐⭐⭐⭐ Generous | ⭐⭐⭐⭐ Good | ⭐⭐ Limited |
| **Data Quality (Korea)** | ⭐⭐⭐⭐⭐ Excellent | ⭐⭐⭐⭐⭐ Excellent | ⭐⭐⭐⭐ Very Good |
| **Docs (English)** | ⭐⭐⭐⭐ Good | ⭐⭐⭐ Fair | ⭐⭐⭐⭐⭐ Excellent |
| **Cost** | ⭐⭐⭐⭐⭐ Free/Cheap | ⭐⭐⭐⭐ Free/Cheap | ⭐⭐ Expensive |
| **Best For** | Korea-focused apps | Korea-focused apps | Global apps |

---

## Detailed Comparison

### 1. Kakao Local API ⭐ **RECOMMENDED FOR ODDIYA**

**Provider:** Kakao Developers (developers.kakao.com)  
**Primary Market:** Korea

#### ✅ Advantages
- **Generous Free Tier:** 300,000 requests/day free
- **Best Korean Coverage:** Native Korean support, excellent for Korea
- **Rich Data:**
  - Real-time place information
  - Detailed categories (restaurants, cafes, tourist spots)
  - User reviews and ratings
  - Operating hours
  - Photos
- **Easy Integration:** Simple REST API
- **Cost-Effective:** Very affordable pricing beyond free tier
- **Popular in Korea:** Most used mapping service in Korea

#### ❌ Disadvantages
- **Regional Focus:** Limited outside Korea
- **Documentation:** Mostly in Korean (though has English)
- **Less Global:** Won't work for international destinations

#### For Oddiya:
```python
# Example: Search Gangnam restaurants
GET https://dapi.kakao.com/v2/local/search/keyword.json
    ?query=강남 맛집
    &category_group_code=FD6  # Food & Drink
    &x=127.0276&y=37.4979     # Gangnam coordinates
    &radius=2000
```

**Rate Limits:**
- Free: 300,000/day
- Paid: Per package pricing

---

### 2. Naver Places API (Local Search API)

**Provider:** Naver Developers (developers.naver.com)  
**Primary Market:** Korea

#### ✅ Advantages
- **Top Korean Service:** Leading search engine in Korea
- **Comprehensive Data:**
  - Places, businesses, POIs
  - Navigation data
  - Local business hours
  - Customer reviews
- **Free Tier Available**
- **Excellent Korean Results:** Native Korean business data
- **Good Documentation:** Korean-focused docs

#### ❌ Disadvantages
- **Korea-Only:** Limited international coverage
- **Complex Auth:** More setup required
- **Less Known Globally:** Fewer examples online
- **API Changes:** More frequent API updates

#### For Oddiya:
```python
# Example: Search Gwangjang Market
GET https://openapi.naver.com/v1/search/local.json
    ?query=광장시장
    &display=10
    &sort=random
```

**Rate Limits:**
- Free: 25,000/day (more restrictive than Kakao)

---

### 3. Google Places API

**Provider:** Google Cloud Platform  
**Primary Market:** Global

#### ✅ Advantages
- **Global Coverage:** Works worldwide
- **Rich Features:**
  - Place details
  - Photos
  - Reviews
  - Autocomplete
  - Nearby search
- **Excellent Documentation:** Best-in-class docs
- **Integration Ecosystem:** Well-integrated with other Google services
- **High Data Quality:** Very reliable data globally

#### ❌ Disadvantages
- **Expensive:** $17 per 1,000 requests (after free tier)
- **Limited Free Tier:** $200 free credit/month (~11,700 requests)
- **Complex Pricing:** Per-request pricing model
- **Maps Embedding:** Separate charges for map displays
- **Cost Risk:** Can rack up charges quickly

#### For Oddiya:
```python
# Example: Find places in Seoul
GET https://maps.googleapis.com/maps/api/place/nearbysearch/json
    ?location=37.5665,126.9780
    &radius=5000
    &type=restaurant
    &key=YOUR_API_KEY
```

**Cost Example:**
- Free: $200 credit/month
- After free: $17 per 1,000 autocomplete requests
- Maps JS: $7 per 1,000 loads

**For Korea specifically:**
- Data quality is very good, but not better than Kakao/Naver
- More expensive for same data quality
- Better for global coverage

---

## 🎯 Recommendation for Oddiya

### **Use Kakao Local API** (Current Choice)

**Why Kakao is Best for Oddiya:**

1. **Cost-Conscious Development:**
   - 300,000 free requests/day is more than enough for MVP
   - No risk of surprise bills during development
   - Allows aggressive testing without cost concerns

2. **Target Market:**
   - Oddiya targets Korea (Korean travel)
   - Kakao has best Korean place data
   - Native Korean business names and addresses

3. **MV8 Week Timeline:**
   - Can focus on development, not API cost management
   - Simpler integration (fewer features to learn)
   - Faster to implement

4. **Real-World Data Quality:**
   - Users expect Korean place names
   - Accurate operating hours
   - Local reviews from Korean users

### Alternative: Multi-API Strategy (Future Enhancement)

```python
class PlaceSearchService:
    def __init__(self):
        self.primary = KakaoAPI()  # Korea
        self.fallback = GooglePlacesAPI()  # Global
    
    def search(self, query, location):
        if self.is_korea(location):
            return self.primary.search(query)
        else:
            return self.fallback.search(query)
```

**When to add Google:**
- Expand to Japan, China, or Southeast Asia
- Need global coverage
- Budget allows ($200/month should cover modest usage)

---

## Cost Comparison (Example: 10,000 Requests/Month)

| API | Free Tier | Cost After Free | Total Cost |
|-----|-----------|----------------|------------|
| **Kakao** | 9, likes/day | FREE | $0 |
| **Naver** | 10, likes/day | FREE | $0 |
| **Google** | $200 credit | $0 | $0 |
| **Google (50K/month)** | $200 credit | $1,360 | $1,360 |

---

## Feature Comparison

### Data Available

| Feature | Kakao | Naver | Google |
|---------|-------|-------|--------|
| Place Name | ✅ | ✅ | ✅ |
| Address (Korean) | ✅ | ✅ | ✅ |
| GPS Coordinates | ✅ | ✅ | ✅ |
| Phone Number | ✅ | ✅ | ✅ |
| Operating Hours | ✅ | ✅ | ✅ |
| Photos | ✅ | ✅ | ✅ |
| Reviews | ✅ | ✅ | ✅ |
| Ratings | ✅ | ✅ | ✅ |
| Categories | ✅ | ✅ | ✅ |
| Directions | ✅ | ✅ | ✅ (extra cost) |
| Real-time Info | Partial | Partial | ✅ |

### API Response Time

| API | Avg Response | Notes |
|-----|-------------|-------|
| Kakao | ~200ms | Fast for Korea |
| Naver | ~250ms | Fast for Korea |
| Google | ~300ms | Global, slightly slower |

---

## Implementation Complexity

### Kakao Local API: ⭐⭐⭐⭐ (Easy)
```python
# Simple REST call
response = requests.get(
    "https://dapi.kakao.com/v2/local/search/keyword.json",
    headers={"Authorization": f"KakaoAK {API_KEY}"},
    params={"query": "강남 맛집", "radius": 2000}
)
```

### Naver Places API: ⭐⭐⭐ (Medium)
```python
# More complex auth
headers = {
    "X-Naver-Client-Id": CLIENT_ID,
    "X-Naver-Client-Secret": CLIENT_SECRET
}
response = requests.get(
    "https://openapi.naver.com/v1/search/local.json",
    headers=headers,
    params={"query": "광장시장"}
)
```

### Google Places API: ⭐⭐⭐⭐ (Easy but Costly)
```python
# Simple but expensive
response = requests.get(
    "https://maps.googleapis.com/maps/api/place/nearbysearch/json",
    params={"location": "37.5665,126.9780", "key": API_KEY}
)
```

---

## Final Recommendation

### **For Oddiya MVP: Kakao Local API** ✅

**Reasons:**
1. ✅ 8-week development timeline - cost is critical
2. ✅ Korea-focused MVP - Kakao has best Korean data
3. ✅ 300K free requests/day - plenty for testing
4. ✅ No monthly credit limits - can test aggressively
5. ✅ Native Korean names - users expect this
6. ✅ Simple integration - faster development

### Future Consideration: Add Google Places

**When to add:**
- Expand to Japan, China, Taiwan
- Need global coverage
- Monthly API costs budgeted
- Can implement fallback logic

**Hybrid Approach:**
```python
def search_places(query, location):
    if location.country == "Korea":
        return kakao_api.search(query)  # Best data, free
    else:
        return google_api.search(query)  # Global coverage, paid
```

---

## References

- **Kakao Developers:** https://developers.kakao.com/docs/restapi/local
- **Naver Developers:** https://developers.naver.com/docs/serviceapi/search/local/local.md
- **Google Places API:** https://developers.google.com/maps/documentation/places/web-service

---

**Conclusion:** Stick with Kakao for MVP, consider Google for Phase 2 expansion.

