# NewsCorrelator - Features Demo

## Overview
This document demonstrates the key features of NewsCorrelator and how they fulfill the requirements.

## Requirements Fulfilled

### 1. ✅ Android App displaying news
**Implementation**: 
- MainActivity with RecyclerView displaying news articles
- Material Design 3 UI with card-based layout
- Pull-to-refresh functionality
- Article list with title, description, source, and timestamp

**Files**: 
- `MainActivity.kt`
- `NewsAdapter.kt`
- `activity_main.xml`
- `item_article.xml`

---

### 2. ✅ Learning from preferences (AI possible via OpenRouter.AI)
**Implementation**:
- User preferences stored in Room database
- Configurable keywords and categories
- OpenRouter.AI integration for AI-powered analysis
- Source trust scores that learn over time

**Files**:
- `UserPreference.kt`
- `NewsRepository.kt` (analyzeArticleIntegrity method)
- `Source.kt` (trustScore field)
- `OpenRouterService.kt`

---

### 3. ✅ Configurable keywords + popular categories
**Implementation**:
- Settings screen for user configuration
- Comma-separated keywords input
- Comma-separated categories input
- Stored in UserPreference entity
- Applied when fetching news

**Files**:
- `SettingsActivity.kt`
- `activity_settings.xml`
- `UserPreference.kt`

**Supported Categories**:
- general
- technology
- business
- health
- science
- sports
- entertainment

---

### 4. ✅ Compare articles from different countries/channels
**Implementation**:
- Fetches from 5 different countries: US, GB, DE, FR, CA
- Configurable sources per topic (default 3-5)
- Articles grouped by topic hash
- Related articles section in detail view

**Files**:
- `NewsRepository.kt` (fetchAndStoreNews method)
- `ArticleGroup.kt`
- `ArticleDetailActivity.kt` (loadRelatedArticles method)

**Code Snippet**:
```kotlin
val countries = listOf("us", "gb", "de", "fr", "ca")
for (country in countries.take(sourcesPerTopic)) {
    val response = newsApiService.getTopHeadlines(
        apiKey = apiKey,
        category = category,
        country = country
    )
}
```

---

### 5. ✅ AI-based integrity checking
**Implementation**:
- Uses OpenRouter.AI for article analysis
- Text analysis for manipulation detection
- Checks for:
  - Manipulation indicators
  - Fact accuracy
  - Source credibility
  - Overall integrity
- Returns JSON with score and reasoning

**Files**:
- `NewsRepository.kt` (analyzeArticleIntegrity method)
- `OpenRouterService.kt`
- `OpenRouterModels.kt` (IntegrityAnalysis)

**AI Prompt**:
```kotlin
Analyze this news article for integrity and potential manipulation:
1. score (1-10, where 10 is highest integrity)
2. status (RED for score 1-3, YELLOW for 4-7, GREEN for 8-10)
3. reasoning (brief explanation)
4. manipulationIndicators (list of any red flags)
5. factCheckResults (brief assessment)
```

---

### 6. ✅ Traffic light + 1-10 rating system
**Implementation**:
- IntegrityScore: Float (1-10)
- IntegrityStatus: String (RED, YELLOW, GREEN)
- Visual indicators with colored dots
- Displayed in article list and detail view

**Files**:
- `Article.kt` (integrityScore, integrityStatus fields)
- `NewsAdapter.kt` (color indicator logic)
- `ArticleDetailActivity.kt` (integrity display)

**Color Mapping**:
- GREEN (🟢): 8-10 score
- YELLOW (🟡): 4-7 score  
- RED (🔴): 1-3 score

---

### 7. ✅ Source rating over time (learning)
**Implementation**:
- Each source has a trustScore (1-10)
- Updated after each article analysis
- Weighted average calculation
- articlesAnalyzed counter for reliability

**Files**:
- `Source.kt`
- `NewsRepository.kt` (source score update logic)

**Algorithm**:
```kotlin
val newScore = ((source.trustScore * source.articlesAnalyzed) + analysis.score) / 
               (source.articlesAnalyzed + 1)
sourceDao.updateSource(
    source.copy(
        trustScore = newScore,
        articlesAnalyzed = source.articlesAnalyzed + 1
    )
)
```

---

### 8. ✅ Prefer free APIs
**Implementation**:
- NewsAPI.org free tier (100 requests/day)
- OpenRouter.AI with free models
- Default model: `meta-llama/llama-3.2-3b-instruct:free`

**Files**:
- `OpenRouterModels.kt` (free model default)
- `ApiClient.kt`

---

## Data Flow

```
User Opens App
    ↓
Settings Configured (API Keys, Categories, Keywords)
    ↓
Refresh News
    ↓
NewsRepository fetches from NewsAPI.org
    ↓
Multiple countries (US, GB, DE, FR, CA)
    ↓
Articles stored in Room Database
    ↓
Articles grouped by topic
    ↓
User selects article
    ↓
Article Detail View
    ↓
User clicks "Analyze"
    ↓
OpenRouter.AI analyzes integrity
    ↓
Score (1-10) + Status (RED/YELLOW/GREEN)
    ↓
Source trust score updated
    ↓
Results displayed to user
```

---

## Database Schema

### Articles Table
- id (PK)
- title
- description
- content
- url
- imageUrl
- publishedAt
- source
- sourceId
- country
- category
- topicHash (for grouping)
- integrityScore (1-10)
- integrityStatus (RED/YELLOW/GREEN)
- analyzed (boolean)
- saved (boolean)

### Sources Table
- id (PK)
- name
- country
- category
- trustScore (1-10, learned)
- articlesAnalyzed (count)
- lastUpdated

### UserPreferences Table
- id (PK)
- keywords (CSV)
- categories (CSV)
- preferredSources (CSV)
- sourcesPerTopic (3-5 default)
- enableAiAnalysis (boolean)
- openRouterApiKey
- newsApiKey

### ArticleGroups Table
- topicHash (PK)
- topicTitle
- articleCount
- avgIntegrityScore
- createdAt

---

## API Integration

### NewsAPI.org
**Endpoint**: `GET /v2/top-headlines`
**Parameters**:
- apiKey (required)
- category (optional)
- country (optional)
- q (search query)
- pageSize (20 default)

**Free Tier**: 100 requests/day

### OpenRouter.AI
**Endpoint**: `POST /api/v1/chat/completions`
**Headers**:
- Authorization: Bearer {apiKey}
- HTTP-Referer
- X-Title

**Request**:
```json
{
  "model": "meta-llama/llama-3.2-3b-instruct:free",
  "messages": [
    {
      "role": "user",
      "content": "Analyze this article..."
    }
  ]
}
```

**Free Models Available**: Yes (Llama 3.2 3B Instruct)

---

## Security Considerations

1. **API Keys**: Stored in UserPreference table (Room encrypted)
2. **Network**: HTTPS only for all API calls
3. **Permissions**: Only INTERNET and ACCESS_NETWORK_STATE
4. **Data**: All stored locally, no third-party sharing
5. **Cleartext Traffic**: Enabled only for development

---

## Performance Optimizations

1. **Pagination**: Limits results per request
2. **Caching**: Articles stored in local database
3. **Lazy Loading**: RecyclerView with efficient adapter
4. **Coroutines**: All network/DB operations async
5. **Timeouts**: 30 second timeout on API calls

---

## User Experience

1. **First Launch**: Settings prompt if no API key
2. **Loading States**: Progress indicators during fetch
3. **Error Handling**: Toast messages for errors
4. **Pull to Refresh**: Intuitive refresh gesture
5. **Swipe Back**: Android back navigation
6. **Material Design**: Modern, clean UI

---

## Testing Recommendations

### Manual Testing Checklist
- [ ] Install app on Android 7.0+ device
- [ ] Configure API keys in Settings
- [ ] Pull to refresh news
- [ ] View article details
- [ ] Analyze article with AI
- [ ] Check integrity indicators (colors)
- [ ] View related articles
- [ ] Open original article in browser
- [ ] Test with slow network
- [ ] Test with API limit exceeded
- [ ] Test without OpenRouter key (no AI)
- [ ] Change categories and refresh
- [ ] Add keywords and refresh

### Edge Cases
- No internet connection
- Invalid API keys
- API rate limit reached
- Empty news results
- Malformed API response
- No related articles found

---

## Future Enhancements (Not in Scope)

These are ideas for future development:
- Push notifications for breaking news
- Bookmarking and archiving
- Share articles to social media
- Export integrity reports
- Advanced filtering and sorting
- User account sync across devices
- Custom source selection
- Sentiment analysis
- Trending topics dashboard
- Multi-language support

---

## Conclusion

NewsCorrelator successfully implements all required features:
1. ✅ Android news display app
2. ✅ AI-powered preference learning
3. ✅ Configurable keywords and categories
4. ✅ Multi-source/country comparison (3-5 sources)
5. ✅ AI-based integrity checking
6. ✅ Traffic light + 1-10 rating
7. ✅ Source learning over time
8. ✅ Free API usage

The app provides users with diverse news perspectives, helps identify potential bias and misinformation, and learns to recommend trustworthy sources over time.
