# NewsCorrelator - Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                          NewsCorrelator App                          │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                            UI Layer                                  │
├─────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐              │
│  │  MainActivity │  │  ArticleDet. │  │  SettingsAct.│              │
│  │              │  │   Activity   │  │   ivity      │              │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘              │
│         │                 │                 │                        │
│         └─────────────────┴─────────────────┘                        │
│                           │                                          │
│                  ┌────────▼─────────┐                                │
│                  │  NewsViewModel   │                                │
│                  │  (LiveData)      │                                │
│                  └────────┬─────────┘                                │
└───────────────────────────┼──────────────────────────────────────────┘
                            │
┌───────────────────────────▼──────────────────────────────────────────┐
│                       Repository Layer                                │
├──────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  ┌────────────────────────────────────────────────────────────────┐ │
│  │                     NewsRepository                              │ │
│  │                                                                 │ │
│  │  • fetchAndStoreNews()                                         │ │
│  │  • analyzeArticleIntegrity()                                   │ │
│  │  • groupArticlesByTopic()                                      │ │
│  │  • updateSourceTrustScore()                                    │ │
│  └──────────┬──────────────────────────────────┬──────────────────┘ │
│             │                                   │                    │
└─────────────┼───────────────────────────────────┼────────────────────┘
              │                                   │
    ┌─────────▼────────┐              ┌──────────▼──────────┐
    │   API Layer      │              │   Database Layer    │
    │                  │              │                     │
┌───┴──────────────────┴───┐      ┌───┴─────────────────────┴───┐
│                           │      │                             │
│  ┌─────────────────────┐ │      │  ┌───────────────────────┐ │
│  │  NewsApiService     │ │      │  │   NewsDatabase        │ │
│  │  • getTopHeadlines()│ │      │  │   (Room)              │ │
│  │  • searchEverything()│ │      │  └───────────┬───────────┘ │
│  └─────────────────────┘ │      │              │             │
│                           │      │   ┌──────────▼──────────┐ │
│  ┌─────────────────────┐ │      │   │  DAOs               │ │
│  │ OpenRouterService   │ │      │   │  • ArticleDao       │ │
│  │  • chat() (AI)      │ │      │   │  • SourceDao        │ │
│  └─────────────────────┘ │      │   │  • UserPrefDao      │ │
│                           │      │   │  • ArticleGroupDao  │ │
│  ┌─────────────────────┐ │      │   └─────────────────────┘ │
│  │   ApiClient         │ │      │                             │
│  │  • Retrofit         │ │      │   ┌─────────────────────┐ │
│  │  • OkHttp           │ │      │   │  Entities           │ │
│  └─────────────────────┘ │      │   │  • Article          │ │
│                           │      │   │  • Source           │ │
└───────────────────────────┘      │   │  • UserPreference   │ │
                                    │   │  • ArticleGroup     │ │
                                    │   └─────────────────────┘ │
                                    │                             │
                                    └─────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                        External APIs                                 │
├─────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  ┌──────────────────────┐          ┌──────────────────────┐         │
│  │   NewsAPI.org        │          │   OpenRouter.AI      │         │
│  │                      │          │                      │         │
│  │  • Top Headlines     │          │  • Chat Completions  │         │
│  │  • Multi-country     │          │  • Free AI Models    │         │
│  │  • Categories        │          │  • Integrity Check   │         │
│  │                      │          │                      │         │
│  │  Free: 100 req/day   │          │  Free: Llama 3.2     │         │
│  └──────────────────────┘          └──────────────────────┘         │
│                                                                       │
└───────────────────────────────────────────────────────────────────────┘


┌─────────────────────────────────────────────────────────────────────┐
│                           Data Flow                                  │
└─────────────────────────────────────────────────────────────────────┘

User Action (Refresh)
        │
        ▼
MainActivity.onRefresh()
        │
        ▼
NewsViewModel.refreshNews()
        │
        ▼
NewsRepository.fetchAndStoreNews()
        │
        ├───────────────────────────────────┐
        ▼                                   ▼
NewsApiService                    Multiple Countries
(US, GB, DE, FR, CA)              Multiple Categories
        │
        ▼
NewsApiResponse (JSON)
        │
        ▼
Convert to Article entities
        │
        ▼
ArticleDao.insertArticles()
        │
        ▼
Room Database (SQLite)
        │
        ▼
LiveData<List<Article>>
        │
        ▼
NewsViewModel (observe)
        │
        ▼
MainActivity.updateUI()
        │
        ▼
RecyclerView Display


┌─────────────────────────────────────────────────────────────────────┐
│                    AI Integrity Analysis Flow                        │
└─────────────────────────────────────────────────────────────────────┘

User clicks "Analyze"
        │
        ▼
NewsViewModel.analyzeArticle()
        │
        ▼
NewsRepository.analyzeArticleIntegrity()
        │
        ▼
Create AI prompt with article details
        │
        ▼
OpenRouterService.chat()
        │
        ▼
OpenRouter API (Llama 3.2 Free)
        │
        ▼
AI Response (JSON)
{
  score: 7.5,
  status: "YELLOW",
  reasoning: "...",
  manipulationIndicators: [...],
  factCheckResults: "..."
}
        │
        ▼
Parse IntegrityAnalysis
        │
        ▼
Update Source Trust Score
        │
        ▼
ArticleDao.updateArticle()
        │
        ▼
LiveData updates UI
        │
        ▼
Display colored indicator + score


┌─────────────────────────────────────────────────────────────────────┐
│                       Component Breakdown                            │
└─────────────────────────────────────────────────────────────────────┘

Activities (3):
  • MainActivity         - News feed, list view
  • ArticleDetailActivity - Full article, related articles
  • SettingsActivity     - Configure preferences

ViewModels (1):
  • NewsViewModel        - Business logic coordinator

Adapters (1):
  • NewsAdapter          - RecyclerView binding

Repository (1):
  • NewsRepository       - Data operations

Database (1):
  • NewsDatabase         - Room database instance

DAOs (4):
  • ArticleDao          - Article queries
  • SourceDao           - Source queries
  • UserPreferenceDao   - Preference queries
  • ArticleGroupDao     - Group queries

Entities (4):
  • Article             - News article data
  • Source              - News source data
  • UserPreference      - User settings
  • ArticleGroup        - Topic grouping

API Services (2):
  • NewsApiService      - NewsAPI.org integration
  • OpenRouterService   - OpenRouter.AI integration

API Models (2):
  • NewsApiModels       - Request/Response models
  • OpenRouterModels    - Request/Response models

Utils (1):
  • Extensions          - Helper functions


┌─────────────────────────────────────────────────────────────────────┐
│                      Key Design Patterns                             │
└─────────────────────────────────────────────────────────────────────┘

1. MVVM Architecture
   View ← ViewModel ← Repository ← Data Sources

2. Repository Pattern
   Single source of truth for data operations

3. Dependency Injection (Manual)
   ViewModels get database/repository instances

4. Observer Pattern
   LiveData for reactive UI updates

5. Adapter Pattern
   RecyclerView adapters for list display

6. Singleton Pattern
   Database instance, API clients

7. Builder Pattern
   Retrofit, Room database builders
