package com.newscorrelator.app.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.newscorrelator.app.R
import com.newscorrelator.app.utils.LogManager
import com.newscorrelator.app.utils.RateLimitManager

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: NewsViewModel
    private lateinit var adapter: NewsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var progressBar: CircularProgressIndicator
    private lateinit var toolbar: MaterialToolbar
    private lateinit var newsApiIndicator: View
    private lateinit var openRouterIndicator: View
    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            updateRateLimitIndicators()
            handler.postDelayed(this, 2000) // Update every 2 seconds
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        LogManager.i("MainActivity.onCreate() started")
        
        try {
            super.onCreate(savedInstanceState)
            LogManager.i("MainActivity.super.onCreate() completed")
            
            LogManager.i("Setting content view")
            setContentView(R.layout.activity_main)
            LogManager.i("Content view set successfully")

            LogManager.i("Finding views")
            toolbar = findViewById(R.id.toolbar)
            LogManager.i("Toolbar found")
            setSupportActionBar(toolbar)
            LogManager.i("Toolbar set as action bar")

            recyclerView = findViewById(R.id.recyclerView)
            LogManager.i("RecyclerView found")
            swipeRefresh = findViewById(R.id.swipeRefresh)
            LogManager.i("SwipeRefreshLayout found")
            progressBar = findViewById(R.id.progressBar)
            LogManager.i("ProgressBar found")
            newsApiIndicator = findViewById(R.id.newsApiIndicator)
            LogManager.i("NewsAPI indicator found")
            openRouterIndicator = findViewById(R.id.openRouterIndicator)
            LogManager.i("OpenRouter indicator found")

            LogManager.i("Getting ViewModel")
            viewModel = ViewModelProvider(this)[NewsViewModel::class.java]
            LogManager.i("ViewModel created successfully")

            LogManager.i("Setting up RecyclerView")
            setupRecyclerView()
            LogManager.i("RecyclerView setup completed")
            
            LogManager.i("Observing ViewModel")
            observeViewModel()
            LogManager.i("ViewModel observation setup completed")

            swipeRefresh.setOnRefreshListener {
                LogManager.i("Swipe refresh triggered")
                viewModel.refreshNews()
            }

            // Load initial data if preferences are set
            viewModel.preferences.observe(this) { prefs ->
                LogManager.i("Preferences changed: ${prefs != null}")
                if (prefs != null) {
                    LogManager.i("API Key configured: ${prefs.newsApiKey.isNotEmpty()}")
                    if (prefs.newsApiKey.isNotEmpty()) {
                        LogManager.i("Triggering initial news refresh")
                        viewModel.refreshNews()
                    } else {
                        LogManager.w("API key is empty - user needs to configure settings")
                    }
                } else {
                    LogManager.w("No preferences found - user needs to configure settings")
                }
            }
            
            LogManager.i("MainActivity.onCreate() completed successfully")
        } catch (e: Exception) {
            LogManager.e("Error in MainActivity.onCreate()", e)
            // Show error to user but don't crash the app
            Toast.makeText(this, "Error initializing main screen: ${e.message}", Toast.LENGTH_LONG).show()
            // Try to continue despite the error
        }
    }
    
    private fun updateRateLimitIndicators() {
        try {
            // Update NewsAPI indicator
            val (newsUsagePercent, newsColor) = RateLimitManager.getUsagePercentage(RateLimitManager.API_NEWS)
            newsApiIndicator.setBackgroundColor(getColorForIndicator(newsColor))
            
            // Update OpenRouter indicator
            val (openRouterUsagePercent, openRouterColor) = RateLimitManager.getUsagePercentage(RateLimitManager.API_OPENROUTER)
            openRouterIndicator.setBackgroundColor(getColorForIndicator(openRouterColor))
            
            LogManager.d("Rate limit indicators updated - NewsAPI: $newsUsagePercent% ($newsColor), OpenRouter: $openRouterUsagePercent% ($openRouterColor)")
        } catch (e: Exception) {
            LogManager.e("Error updating rate limit indicators", e)
        }
    }
    
    private fun getColorForIndicator(color: String): Int {
        return when (color) {
            "red" -> ContextCompat.getColor(this, R.color.red)
            "yellow" -> ContextCompat.getColor(this, R.color.yellow)
            else -> ContextCompat.getColor(this, R.color.green)
        }
    }

    private fun setupRecyclerView() {
        try {
            LogManager.i("Creating NewsAdapter")
            adapter = NewsAdapter(
                onArticleClick = { article ->
                    try {
                        LogManager.i("Article clicked: ${article.title}")
                        val intent = Intent(this, ArticleDetailActivity::class.java)
                        intent.putExtra("article_id", article.id)
                        startActivity(intent)
                    } catch (e: Exception) {
                        LogManager.e("Error opening article detail", e)
                        Toast.makeText(this, "Error opening article", Toast.LENGTH_SHORT).show()
                    }
                },
                onSaveClick = { article ->
                    try {
                        LogManager.i("Save clicked for article: ${article.title}")
                        viewModel.toggleSaveArticle(article)
                    } catch (e: Exception) {
                        LogManager.e("Error saving article", e)
                        Toast.makeText(this, "Error saving article", Toast.LENGTH_SHORT).show()
                    }
                },
                onAnalyzeClick = { article ->
                    try {
                        LogManager.i("Analyze clicked for article: ${article.title}")
                        viewModel.analyzeArticle(article)
                        Toast.makeText(this, "Analyzing article...", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        LogManager.e("Error analyzing article", e)
                        Toast.makeText(this, "Error analyzing article", Toast.LENGTH_SHORT).show()
                    }
                }
            )
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = adapter
            LogManager.i("RecyclerView adapter set")
        } catch (e: Exception) {
            LogManager.e("Error in setupRecyclerView()", e)
            // Don't re-throw, let the app continue
            Toast.makeText(this, "Error setting up article list", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        try {
            viewModel.articles.observe(this) { articles ->
                try {
                    LogManager.i("Articles updated: ${articles.size} articles")
                    adapter.submitList(articles)
                } catch (e: Exception) {
                    LogManager.e("Error updating article list", e)
                }
            }

            viewModel.isLoading.observe(this) { isLoading ->
                try {
                    LogManager.i("Loading state changed: $isLoading")
                    swipeRefresh.isRefreshing = isLoading
                    progressBar.visibility = if (isLoading && adapter.itemCount == 0) View.VISIBLE else View.GONE
                } catch (e: Exception) {
                    LogManager.e("Error updating loading state", e)
                }
            }

            viewModel.error.observe(this) { error ->
                try {
                    error?.let {
                        LogManager.e("ViewModel error: $it")
                        Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                        viewModel.clearError()
                    }
                } catch (e: Exception) {
                    LogManager.e("Error displaying error message", e)
                }
            }
        } catch (e: Exception) {
            LogManager.e("Error in observeViewModel()", e)
            // Don't re-throw, let the app continue
            Toast.makeText(this, "Error setting up data observers", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        LogManager.i("MainActivity.onStart()")
    }
    
    override fun onResume() {
        super.onResume()
        LogManager.i("MainActivity.onResume()")
        // Start updating rate limit indicators
        handler.post(updateRunnable)
    }
    
    override fun onPause() {
        super.onPause()
        LogManager.i("MainActivity.onPause()")
        // Stop updating rate limit indicators
        handler.removeCallbacks(updateRunnable)
    }
    
    override fun onStop() {
        super.onStop()
        LogManager.i("MainActivity.onStop()")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        LogManager.i("MainActivity.onDestroy()")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                LogManager.i("Settings menu item clicked")
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.action_refresh -> {
                LogManager.i("Refresh menu item clicked")
                viewModel.refreshNews()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
