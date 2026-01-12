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
import com.newscorrelator.app.utils.toRelativeTime
import kotlinx.coroutines.launch

class ArticleDetailActivity : AppCompatActivity() {
    private lateinit var viewModel: NewsViewModel
    private var currentArticle: Article? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article_detail)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel = ViewModelProvider(this)[NewsViewModel::class.java]

        val articleId = intent.getLongExtra("article_id", -1)
        if (articleId != -1L) {
            loadArticle(articleId)
        }

        findViewById<Button>(R.id.viewOriginalButton).setOnClickListener {
            currentArticle?.let { article ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.url))
                startActivity(intent)
            }
        }

        findViewById<Button>(R.id.analyzeButton).setOnClickListener {
            currentArticle?.let { article ->
                viewModel.analyzeArticle(article)
                Toast.makeText(this, "Analyzing article...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadArticle(articleId: Long) {
        lifecycleScope.launch {
            val db = NewsDatabase.getDatabase(this@ArticleDetailActivity)
            viewModel.articles.observe(this@ArticleDetailActivity) { articles ->
                val article = articles.find { it.id == articleId }
                article?.let {
                    displayArticle(it)
                    loadRelatedArticles(it)
                }
            }
        }
    }

    private fun displayArticle(article: Article) {
        currentArticle = article

        findViewById<TextView>(R.id.titleText).text = article.title
        findViewById<TextView>(R.id.sourceText).text = article.source
        findViewById<TextView>(R.id.timeText).text = article.publishedAt.toRelativeTime()
        findViewById<TextView>(R.id.contentText).text = article.content ?: article.description ?: ""

        // Display integrity info
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
        } else {
            integrityScoreText.text = "Not analyzed yet"
            integrityStatusText.text = "Click Analyze to check integrity"
            integrityIndicator.setBackgroundColor(Color.parseColor("#9E9E9E"))
            analyzeButton.visibility = View.VISIBLE
        }
    }

    private fun loadRelatedArticles(article: Article) {
        if (article.topicHash == null) return

        lifecycleScope.launch {
            val db = NewsDatabase.getDatabase(this@ArticleDetailActivity)
            db.articleDao().getArticlesByTopic(article.topicHash).observe(this@ArticleDetailActivity) { relatedArticles ->
                val filtered = relatedArticles.filter { it.id != article.id }
                if (filtered.isNotEmpty()) {
                    findViewById<TextView>(R.id.relatedArticlesTitle).visibility = View.VISIBLE
                    val recycler = findViewById<RecyclerView>(R.id.relatedArticlesRecycler)
                    recycler.visibility = View.VISIBLE
                    recycler.layoutManager = LinearLayoutManager(this@ArticleDetailActivity)
                    
                    val adapter = NewsAdapter(
                        onArticleClick = { relatedArticle ->
                            loadArticle(relatedArticle.id)
                        },
                        onSaveClick = { viewModel.toggleSaveArticle(it) },
                        onAnalyzeClick = { viewModel.analyzeArticle(it) }
                    )
                    recycler.adapter = adapter
                    adapter.submitList(filtered)
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
