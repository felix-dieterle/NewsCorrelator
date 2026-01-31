package com.newscorrelator.app.ui

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.newscorrelator.app.R
import com.newscorrelator.app.data.Article
import com.newscorrelator.app.data.NewsDatabase
import com.newscorrelator.app.utils.LogManager
import com.newscorrelator.app.utils.toRelativeTime
import kotlinx.coroutines.launch

class ArticleDetailActivity : AppCompatActivity() {
    private lateinit var viewModel: NewsViewModel
    private var currentArticle: Article? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        LogManager.i("ArticleDetailActivity.onCreate() started")
        
        try {
            super.onCreate(savedInstanceState)
            LogManager.i("ArticleDetailActivity.super.onCreate() completed")
            
            LogManager.i("Setting content view")
            setContentView(R.layout.activity_article_detail)
            LogManager.i("Content view set successfully")

            try {
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
                LogManager.i("Action bar configured")
            } catch (e: Exception) {
                LogManager.e("Error configuring action bar", e)
            }

            LogManager.i("Getting ViewModel")
            viewModel = ViewModelProvider(this)[NewsViewModel::class.java]
            LogManager.i("ViewModel created successfully")

            val articleId = intent.getLongExtra("article_id", -1)
            LogManager.i("Article ID from intent: $articleId")
            
            if (articleId != -1L) {
                LogManager.i("Loading article with ID: $articleId")
                loadArticle(articleId)
            } else {
                LogManager.w("No valid article ID provided")
                Toast.makeText(this, "Error: No article specified", Toast.LENGTH_SHORT).show()
            }

            try {
                findViewById<Button>(R.id.viewOriginalButton).setOnClickListener {
                    try {
                        currentArticle?.let { article ->
                            LogManager.i("Opening original article URL: ${article.url}")
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.url))
                            startActivity(intent)
                        } ?: run {
                            LogManager.w("View original clicked but no article loaded")
                            Toast.makeText(this, "Article not loaded", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        LogManager.e("Error opening article URL", e)
                        Toast.makeText(this, "Error opening article", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                LogManager.e("Error setting up view original button", e)
            }

            try {
                findViewById<Button>(R.id.analyzeButton).setOnClickListener {
                    try {
                        currentArticle?.let { article ->
                            LogManager.i("Analyze button clicked for article: ${article.title}")
                            viewModel.analyzeArticle(article)
                            Toast.makeText(this, "Analyzing article...", Toast.LENGTH_SHORT).show()
                        } ?: run {
                            LogManager.w("Analyze clicked but no article loaded")
                            Toast.makeText(this, "Article not loaded", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        LogManager.e("Error analyzing article", e)
                        Toast.makeText(this, "Error analyzing article", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                LogManager.e("Error setting up analyze button", e)
            }
            
            LogManager.i("ArticleDetailActivity.onCreate() completed successfully")
        } catch (e: Exception) {
            LogManager.e("Error in ArticleDetailActivity.onCreate()", e)
            Toast.makeText(this, "Error loading article details: ${e.message}", Toast.LENGTH_LONG).show()
            // Don't crash the app
        }
    }

    private fun loadArticle(articleId: Long) {
        try {
            LogManager.i("loadArticle() called for ID: $articleId")
            lifecycleScope.launch {
                try {
                    val db = NewsDatabase.getDatabase(this@ArticleDetailActivity)
                    LogManager.i("Database obtained")
                    
                    viewModel.articles.observe(this@ArticleDetailActivity) { articles ->
                        try {
                            LogManager.i("Articles list updated, count: ${articles.size}")
                            val article = articles.find { it.id == articleId }
                            if (article != null) {
                                LogManager.i("Article found: ${article.title}")
                                displayArticle(article)
                                loadRelatedArticles(article)
                            } else {
                                LogManager.w("Article with ID $articleId not found in database")
                                Toast.makeText(this@ArticleDetailActivity, "Article not found", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            LogManager.e("Error processing article in observer", e)
                            Toast.makeText(this@ArticleDetailActivity, "Error loading article", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    LogManager.e("Error in loadArticle coroutine", e)
                    Toast.makeText(this@ArticleDetailActivity, "Error accessing database", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            LogManager.e("Error in loadArticle()", e)
            Toast.makeText(this, "Error loading article", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayArticle(article: Article) {
        try {
            LogManager.i("displayArticle() called for: ${article.title}")
            currentArticle = article

            findViewById<TextView>(R.id.titleText).text = article.title
            findViewById<TextView>(R.id.sourceText).text = article.source
            findViewById<TextView>(R.id.timeText).text = article.publishedAt.toRelativeTime()
            findViewById<TextView>(R.id.contentText).text = article.content ?: article.description ?: ""
            LogManager.i("Basic article info displayed")

            // Display integrity info
            try {
                val integrityCard = findViewById<MaterialCardView>(R.id.integrityCard)
                val integrityIndicator = findViewById<View>(R.id.integrityIndicator)
                val integrityScoreText = findViewById<TextView>(R.id.integrityScoreText)
                val integrityStatusText = findViewById<TextView>(R.id.integrityStatusText)
                val analyzeButton = findViewById<Button>(R.id.analyzeButton)

                if (article.analyzed && article.integrityScore != null) {
                    integrityScoreText.text = String.format("Integrity: %.1f/10", article.integrityScore)
                    integrityStatusText.text = article.integrityStatus ?: "Unknown"
                    
                    val color = when (article.integrityStatus) {
                        "GREEN" -> Color.parseColor("#4CAF50")
                        "YELLOW" -> Color.parseColor("#FFC107")
                        "RED" -> Color.parseColor("#F44336")
                        else -> Color.parseColor("#9E9E9E")
                    }
                    integrityIndicator.setBackgroundColor(color)
                    analyzeButton.visibility = View.GONE
                    LogManager.i("Integrity info displayed: ${article.integrityStatus}")
                } else {
                    integrityScoreText.text = "Not analyzed yet"
                    integrityStatusText.text = "Click Analyze to check integrity"
                    integrityIndicator.setBackgroundColor(Color.parseColor("#9E9E9E"))
                    analyzeButton.visibility = View.VISIBLE
                    LogManager.i("Article not yet analyzed")
                }
            } catch (e: Exception) {
                LogManager.e("Error displaying integrity info", e)
            }
        } catch (e: Exception) {
            LogManager.e("Error in displayArticle()", e)
            Toast.makeText(this, "Error displaying article", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadRelatedArticles(article: Article) {
        if (article.topicHash == null) {
            LogManager.i("No topic hash for article, skipping related articles")
            return
        }

        try {
            LogManager.i("loadRelatedArticles() called for topic: ${article.topicHash}")
            lifecycleScope.launch {
                try {
                    val db = NewsDatabase.getDatabase(this@ArticleDetailActivity)
                    db.articleDao().getArticlesByTopic(article.topicHash).observe(this@ArticleDetailActivity) { relatedArticles ->
                        try {
                            val filtered = relatedArticles.filter { it.id != article.id }
                            LogManager.i("Found ${filtered.size} related articles")
                            
                            if (filtered.isNotEmpty()) {
                                findViewById<TextView>(R.id.relatedArticlesTitle).visibility = View.VISIBLE
                                val recycler = findViewById<RecyclerView>(R.id.relatedArticlesRecycler)
                                recycler.visibility = View.VISIBLE
                                recycler.layoutManager = LinearLayoutManager(this@ArticleDetailActivity)
                                
                                val adapter = NewsAdapter(
                                    onArticleClick = { relatedArticle ->
                                        LogManager.i("Related article clicked: ${relatedArticle.title}")
                                        loadArticle(relatedArticle.id)
                                    },
                                    onSaveClick = { 
                                        LogManager.i("Save clicked for related article: ${it.title}")
                                        viewModel.toggleSaveArticle(it) 
                                    },
                                    onAnalyzeClick = { 
                                        LogManager.i("Analyze clicked for related article: ${it.title}")
                                        viewModel.analyzeArticle(it) 
                                    }
                                )
                                recycler.adapter = adapter
                                adapter.submitList(filtered)
                                LogManager.i("Related articles displayed")
                            }
                        } catch (e: Exception) {
                            LogManager.e("Error displaying related articles", e)
                        }
                    }
                } catch (e: Exception) {
                    LogManager.e("Error loading related articles from database", e)
                }
            }
        } catch (e: Exception) {
            LogManager.e("Error in loadRelatedArticles()", e)
        }
    }

    override fun onStart() {
        super.onStart()
        LogManager.i("ArticleDetailActivity.onStart()")
    }
    
    override fun onResume() {
        super.onResume()
        LogManager.i("ArticleDetailActivity.onResume()")
    }
    
    override fun onPause() {
        super.onPause()
        LogManager.i("ArticleDetailActivity.onPause()")
    }
    
    override fun onStop() {
        super.onStop()
        LogManager.i("ArticleDetailActivity.onStop()")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        LogManager.i("ArticleDetailActivity.onDestroy()")
    }

    override fun onSupportNavigateUp(): Boolean {
        LogManager.i("ArticleDetailActivity navigate up")
        finish()
        return true
    }
}
