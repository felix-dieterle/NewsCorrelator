# NewsCorrelator

An intelligent Android news aggregation app that displays news from multiple sources, learns from user preferences, and uses AI to analyze article integrity and detect potential bias.

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

### API Optimization Features ⚡
- **Intelligent Rate Limiting**: Tracks and enforces API limits to prevent quota exhaustion
- **Smart Caching**: Caches responses with TTL-based expiration (30min-24hr depending on data type)
- **Query Optimization**: Batches requests and deduplicates queries for efficiency
- **AI/Non-AI Mode**: Different optimization strategies based on whether AI analysis is enabled
  - AI mode: More data for better analysis
  - Non-AI mode: Minimal requests for efficiency
- **60-80% API call reduction** through intelligent caching and optimization

See [API_OPTIMIZATION.md](API_OPTIMIZATION.md) for detailed documentation.

### Technical Features
- Room Database for offline storage
- MVVM Architecture with LiveData
- Coroutines for asynchronous operations
- Retrofit for API calls
- Material Design 3 UI
- SwipeRefreshLayout for pull-to-refresh
- WorkManager for background cache cleanup

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

## CI/CD & Releases

### Continuous Integration
This project uses GitHub Actions for continuous integration. On every push and pull request:
- **Linting**: Runs `./gradlew lintDebug` to check code quality
- **Unit Tests**: Runs `./gradlew test` to execute unit tests
- **Build Verification**: Runs `./gradlew assembleDebug` to ensure the app builds successfully

### Automated Releases
On every merge to the `main` branch:
- The workflow automatically checks if a release with the current version exists
- If the version tag already exists, the `versionCode` is **automatically incremented**
- A release APK is automatically built using `./gradlew assembleRelease`
- A new GitHub release is created with:
  - Version tag based on `versionName` and `versionCode` from `app/build.gradle`
  - Downloadable APK artifact
  - Automatically generated changelog

To create a new release:
1. Simply merge your changes to the `main` branch
2. The build number (`versionCode`) will be automatically incremented if needed
3. The release will be automatically created and published

**Optional**: To update the version name (e.g., from 1.0 to 1.1), manually update `versionName` in `app/build.gradle` before merging to main.

### Building Locally
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK (signed with debug key for testing)
./gradlew assembleRelease

# Run tests
./gradlew test

# Run lint checks
./gradlew lint
```

### APK Signing Configuration

The app is configured to use debug signing for both debug and release builds by default. This ensures that:
- Release APKs can be installed on devices without signing errors
- Testing and development releases work smoothly

**For production releases**, you should configure proper release signing:

1. Generate a release keystore (store it outside the project directory):
   ```bash
   keytool -genkey -v -keystore ~/keystore/release.keystore -alias my-key-alias -keyalg RSA -keysize 2048 -validity 10000
   ```

2. Create a `keystore.properties` file in the project root (this file should be gitignored):
   ```properties
   storeFile=../keystore/release.keystore
   storePassword=your-keystore-password
   keyAlias=my-key-alias
   keyPassword=your-key-password
   ```

3. Add `keystore.properties` to `.gitignore`:
   ```
   keystore.properties
   ```

4. Update `app/build.gradle` to use the keystore properties:
   ```gradle
   def keystorePropertiesFile = rootProject.file("keystore.properties")
   def keystoreProperties = new Properties()
   if (keystorePropertiesFile.exists()) {
       keystoreProperties.load(new FileInputStream(keystorePropertiesFile))
   }
   
   android {
       ...
       signingConfigs {
           release {
               if (keystorePropertiesFile.exists()) {
                   storeFile file(keystoreProperties['storeFile'])
                   storePassword keystoreProperties['storePassword']
                   keyAlias keystoreProperties['keyAlias']
                   keyPassword keystoreProperties['keyPassword']
               }
           }
       }
       
       buildTypes {
           release {
               if (keystorePropertiesFile.exists()) {
                   signingConfig signingConfigs.release
               } else {
                   signingConfig signingConfigs.debug
               }
               // ... other config
           }
       }
   }
   ```

5. **Important Security Notes**:
   - Never commit your keystore file or passwords to version control!
   - Store the keystore file securely outside the project directory
   - Keep your keystore password safe - if lost, you cannot update your app on Google Play

For more information on app signing, see the [Android Developer Documentation](https://developer.android.com/studio/publish/app-signing).

## Contributing
Contributions are welcome! Please feel free to submit issues and pull requests.

## Acknowledgments
- NewsAPI.org for free news API
- OpenRouter.AI for free AI model access
- Material Design for UI components