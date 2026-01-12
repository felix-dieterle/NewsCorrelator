# NewsCorrelator

An intelligent Android news aggregation app that displays news from multiple sources, learns from user preferences, and uses AI to analyze article integrity and detect potential bias.

## CI/CD Automation

This repository includes automated workflows for:

- **Automated Testing**: All pull requests are automatically tested and linted
- **Version Management**: Version numbers are automatically incremented on merge to main
- **Release Builds**: APK files are automatically built and released with each version

For detailed information about the CI/CD workflows, see [.github/workflows/README.md](.github/workflows/README.md).

## Features

### Core Functionality
- **Multi-Source News Aggregation**: Fetches news from multiple countries and sources (default 3-5 for diversity)
- **AI-Powered Preference Learning**: Learns from user preferences using OpenRouter.AI
- **Configurable Categories & Keywords**: Users can customize news categories and search keywords
- **Cross-Source Correlation**: Compares articles about the same topic from different countries/channels to provide diverse perspectives
- **AI-Based Integrity Analysis**: 
  - Text analysis for manipulation detection
  - Fact-checking of claims
  - Traffic light system (RED/YELLOW/GREEN) + 1-10 scoring
  - Important factors evaluation
- **Source Rating System**: Learns source trustworthiness over time based on integrity scores
- **Free APIs**: Uses free tier of NewsAPI.org and OpenRouter.AI

### Technical Features
- Room Database for offline storage
- MVVM Architecture with LiveData
- Coroutines for asynchronous operations
- Retrofit for API calls
- Material Design 3 UI
- SwipeRefreshLayout for pull-to-refresh

## Setup

### Prerequisites
1. Android Studio
2. Android SDK (API 24+)
3. Free API Keys:
   - [NewsAPI.org](https://newsapi.org) - Get 100 requests/day for free
   - [OpenRouter.AI](https://openrouter.ai) - Free AI models available

### Installation
1. Clone this repository
2. Open the project in Android Studio
3. Sync Gradle files
4. Build and run the app

### Configuration
1. On first launch, go to Settings (menu → Settings)
2. Enter your NewsAPI.org API key (required)
3. Enter your OpenRouter.AI API key (optional, for AI analysis)
4. Configure categories (e.g., `general,technology,business`)
5. Set keywords for personalized news (optional)
6. Set sources per topic (3-5 recommended for diversity)
7. Enable/disable AI analysis
8. Save settings

## Usage

### Browsing News
- Pull down to refresh news feed
- Tap on any article to view details
- View integrity score (if analyzed) with color indicator:
  - 🟢 GREEN (8-10): High integrity
  - 🟡 YELLOW (4-7): Medium integrity
  - 🔴 RED (1-3): Low integrity

### Article Details
- View full article content
- See integrity analysis (score, status, reasoning)
- Click "Analyze" to run AI-based integrity check
- View related articles from other sources
- Open original article in browser

### AI Integrity Analysis
When an article is analyzed, the AI checks for:
- Manipulation indicators
- Fact accuracy
- Source credibility
- Overall integrity score (1-10)
- Traffic light status (RED/YELLOW/GREEN)

### Source Rating
- Sources are rated over time based on article integrity
- Trust scores update automatically as more articles are analyzed
- View source ratings in Settings

## Architecture

```
app/
├── data/
│   ├── Article.kt           # Article data model
│   ├── Source.kt            # News source model
│   ├── UserPreference.kt    # User preferences
│   ├── ArticleGroup.kt      # Grouped articles by topic
│   ├── NewsDatabase.kt      # Room database
│   ├── *Dao.kt              # Database access objects
│   └── NewsRepository.kt    # Data repository
├── api/
│   ├── NewsApiService.kt    # NewsAPI.org integration
│   ├── OpenRouterService.kt # OpenRouter.AI integration
│   └── ApiClient.kt         # Retrofit client
├── ui/
│   ├── MainActivity.kt      # Main news feed
│   ├── ArticleDetailActivity.kt
│   ├── SettingsActivity.kt
│   ├── NewsAdapter.kt       # RecyclerView adapter
│   └── NewsViewModel.kt     # ViewModel
└── utils/
    └── Extensions.kt        # Utility functions
```

## APIs Used

### NewsAPI.org
- Free tier: 100 requests/day
- Fetches headlines from multiple countries
- Categories: general, technology, business, health, science, sports, entertainment

### OpenRouter.AI
- Free models available (e.g., meta-llama/llama-3.2-3b-instruct:free)
- Used for AI-based article integrity analysis
- Analyzes text for manipulation, bias, and factual accuracy

## Privacy & Data
- All data stored locally using Room Database
- API keys stored securely in local preferences
- No data sent to third parties except configured APIs
- Users control all data and preferences

## Limitations
- Free tier API limits apply
- AI analysis requires OpenRouter API key
- News availability depends on NewsAPI.org coverage
- Integrity analysis is AI-based and may not be 100% accurate

## Future Enhancements
- More news source integrations
- Enhanced AI models for better analysis
- User feedback loop for preference learning
- Bookmark and save articles
- Notification system for important news
- Dark mode support
- Multi-language support

## License
This project is open source and available under the MIT License.

## Contributing
Contributions are welcome! Please feel free to submit issues and pull requests.

## Acknowledgments
- NewsAPI.org for free news API
- OpenRouter.AI for free AI model access
- Material Design for UI components

