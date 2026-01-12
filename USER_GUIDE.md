# NewsCorrelator - User Guide

## Getting Started

### First Time Setup

1. **Install the App**
   - Download and install NewsCorrelator on your Android device (Android 7.0+ required)

2. **Get API Keys** (Required)
   
   **NewsAPI.org** (Required):
   - Visit https://newsapi.org
   - Click "Get API Key"
   - Sign up for a free account
   - Copy your API key
   
   **OpenRouter.AI** (Optional, for AI analysis):
   - Visit https://openrouter.ai
   - Sign up for an account
   - Navigate to API Keys section
   - Create a new API key
   - Copy the key

3. **Configure the App**
   - Open NewsCorrelator
   - Tap the menu (⋮) in the top right
   - Select "Settings"
   - Enter your NewsAPI.org API key
   - (Optional) Enter your OpenRouter.AI API key
   - Configure your preferences:
     - **Categories**: comma-separated list (e.g., `general,technology,business,health`)
     - **Keywords**: topics you're interested in (e.g., `AI,climate,politics`)
     - **Sources per topic**: 3-5 recommended for diverse perspectives
     - **Enable AI Analysis**: Toggle on/off
   - Tap "Save Settings"

## Using the App

### Main News Feed

- **Refresh News**: Pull down on the screen or tap the refresh icon
- **View Article**: Tap on any article card
- **Integrity Indicator**: Look for colored dots:
  - 🟢 **Green** (8-10): High integrity, trustworthy
  - 🟡 **Yellow** (4-7): Medium integrity, verify claims
  - 🔴 **Red** (1-3): Low integrity, potential issues

### Article Details

1. **View Full Article**
   - Tap on an article from the main feed
   - Read the full content
   - Check the integrity score and analysis

2. **Analyze Article**
   - If not already analyzed, tap "Analyze" button
   - AI will check for:
     - Manipulation indicators
     - Fact accuracy
     - Source credibility
     - Overall integrity

3. **View Related Articles**
   - Scroll down to see related articles from other sources
   - Compare different perspectives on the same topic
   - Each related article shows its own integrity score

4. **Open Original**
   - Tap "View Original Article" to open in your browser
   - Read the full article on the source website

### Understanding Integrity Scores

The AI analyzes articles for:

1. **Manipulation Detection**
   - Emotional language
   - Clickbait headlines
   - Misleading information
   - Biased framing

2. **Fact Checking**
   - Verifiable claims
   - Source citations
   - Cross-reference with known facts

3. **Source Credibility**
   - Historical accuracy of the source
   - Professional journalism standards
   - Transparency

**Score Breakdown**:
- **9-10**: Excellent - highly trustworthy, well-sourced
- **7-8**: Good - generally reliable, minor issues
- **5-6**: Fair - some concerns, verify important claims
- **3-4**: Poor - significant issues, multiple red flags
- **1-2**: Very Poor - highly questionable, likely misinformation

### Settings Configuration

#### Categories
Available categories:
- `general` - General news
- `technology` - Tech news and innovations
- `business` - Business and finance
- `health` - Health and medical news
- `science` - Scientific discoveries
- `sports` - Sports news
- `entertainment` - Entertainment news

Example: `general,technology,business`

#### Keywords
Add topics you want to follow:
- Comma-separated list
- Can be any topic (e.g., `artificial intelligence,climate change,space exploration`)

#### Sources Per Topic
- **Minimum**: 1
- **Recommended**: 3-5 (for diverse perspectives)
- **Maximum**: 10

More sources = more diverse viewpoints but slower loading

### Tips for Best Experience

1. **API Limits**
   - NewsAPI.org free tier: 100 requests/day
   - Refresh strategically to stay within limits
   - Each refresh fetches news for all configured categories

2. **AI Analysis**
   - Requires OpenRouter API key
   - Uses free AI models by default
   - Analysis takes a few seconds per article
   - Analyzed articles are cached

3. **Diverse Sources**
   - Set 3-5 sources per topic for balanced news
   - App fetches from multiple countries automatically (US, UK, Germany, France, Canada)

4. **Stay Informed**
   - Check integrity scores before sharing news
   - Compare related articles for different perspectives
   - Use AI analysis on important or controversial topics

## Troubleshooting

### "Please configure API keys in Settings"
- You need to add your NewsAPI.org key in Settings
- Make sure you've saved the settings

### No news showing
- Check your internet connection
- Verify your NewsAPI key is valid
- Try pulling down to refresh
- Check if you've exceeded daily API limit (100 requests)

### AI analysis not working
- Ensure you've entered an OpenRouter.AI API key
- Check that "Enable AI Analysis" is turned on in Settings
- Verify your OpenRouter account has credits/free tier access

### App is slow
- Reduce "Sources per topic" to 3
- Limit number of categories
- Check your internet connection speed

## Privacy & Security

- All data is stored locally on your device
- API keys are stored securely in app preferences
- No data is shared with third parties
- Only configured APIs (NewsAPI, OpenRouter) receive data
- You can delete the app to remove all data

## Support

For issues, suggestions, or contributions:
- Visit: https://github.com/felix-dieterle/NewsCorrelator
- Report bugs via GitHub Issues
- Contributions welcome via Pull Requests

## Advanced Features

### Source Rating System
- App learns source trustworthiness over time
- Based on integrity scores of analyzed articles
- Helps identify reliable vs unreliable sources
- Updates automatically in the background

### Article Grouping
- Similar articles are automatically grouped by topic
- View all perspectives on the same story
- Helps identify bias by comparing coverage

### Preference Learning
- App tracks which categories you read most
- Uses keywords to find relevant news
- Can be enhanced with more sophisticated AI in future updates
