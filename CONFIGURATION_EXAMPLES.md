# Example Configuration for NewsCorrelator

This file demonstrates recommended configurations for different use cases.

## General News (Balanced)

**Categories**: `general,technology,business`
**Keywords**: `politics,economy,world news`
**Sources per topic**: `4`
**Enable AI Analysis**: `Yes`

Good for: Getting a balanced view of current events with integrity checking.

---

## Technology Focus

**Categories**: `technology,science,business`
**Keywords**: `artificial intelligence,software,innovation,startups`
**Sources per topic**: `5`
**Enable AI Analysis**: `Yes`

Good for: Tech professionals and enthusiasts who want diverse tech news perspectives.

---

## Fact-Checking Heavy

**Categories**: `general,health,science`
**Keywords**: `research,studies,facts,data`
**Sources per topic**: `5`
**Enable AI Analysis**: `Yes`

Good for: Users who prioritize accuracy and want maximum verification.

---

## Quick Updates (API Limit Conscious)

**Categories**: `general`
**Keywords**: `` (leave empty)
**Sources per topic**: `3`
**Enable AI Analysis**: `No`

Good for: Staying within free API limits with quick news updates.

---

## Comprehensive Coverage

**Categories**: `general,technology,business,health,science,sports`
**Keywords**: `breaking news,innovation,global`
**Sources per topic**: `4`
**Enable AI Analysis**: `Yes`

Good for: Users who want comprehensive news across all topics.
Note: Uses more API calls, monitor your daily limit.

---

## Privacy-Focused (Minimal AI)

**Categories**: `general,technology`
**Keywords**: `` (leave empty)
**Sources per topic**: `3`
**Enable AI Analysis**: `No`

Good for: Users who prefer not to use AI analysis and want basic news aggregation.

---

## Academic/Research

**Categories**: `science,health,technology`
**Keywords**: `research,study,university,breakthrough`
**Sources per topic**: `5`
**Enable AI Analysis**: `Yes`

Good for: Researchers and students tracking scientific developments.

---

## API Keys Recommendations

### NewsAPI.org
- **Free Tier**: 100 requests/day
- **When to upgrade**: If you need more than ~15 refreshes per day
- **Tip**: Each category counts as a separate request

### OpenRouter.AI
- **Free Models**: Available (e.g., llama-3.2-3b-instruct:free)
- **Cost**: Pay-as-you-go for premium models
- **Tip**: Free models are sufficient for integrity checking

---

## Performance Tips

1. **Start Small**: Begin with 2-3 categories
2. **Adjust Sources**: 3-5 sources provides good diversity without slowdown
3. **Selective Analysis**: Only analyze important articles to save time
4. **Refresh Wisely**: Don't refresh too frequently (API limits)

---

## Privacy Considerations

- All data stored locally on device
- API keys never shared beyond configured services
- No telemetry or tracking
- You control all data

---

## Troubleshooting Common Configurations

### Too slow loading
- **Reduce** sources per topic to 3
- **Limit** categories to 2-3
- **Check** internet connection

### Hitting API limits
- **Reduce** number of categories
- **Limit** refreshes to 5-6 per day
- **Consider** NewsAPI paid tier

### No AI analysis working
- **Verify** OpenRouter API key is entered
- **Check** "Enable AI Analysis" is ON
- **Confirm** API key has credits

---

## Example Progressive Setup

### Week 1: Getting Started
```
Categories: general
Keywords: (empty)
Sources: 3
AI Analysis: No
```

### Week 2: Adding Features
```
Categories: general,technology
Keywords: your interests
Sources: 4
AI Analysis: Yes (if you have key)
```

### Week 3: Full Featured
```
Categories: general,technology,business
Keywords: detailed interests
Sources: 4-5
AI Analysis: Yes
```

This gradual approach helps you:
1. Learn the app
2. Understand API usage
3. Find your ideal configuration
