package com.newscorrelator.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.newscorrelator.app.R

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: NewsViewModel
    private lateinit var adapter: NewsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var progressBar: CircularProgressIndicator
    private lateinit var toolbar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        recyclerView = findViewById(R.id.recyclerView)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        progressBar = findViewById(R.id.progressBar)

        viewModel = ViewModelProvider(this)[NewsViewModel::class.java]

        setupRecyclerView()
        observeViewModel()

        swipeRefresh.setOnRefreshListener {
            viewModel.refreshNews()
        }

        // Load initial data if preferences are set
        viewModel.preferences.observe(this) { prefs ->
            if (prefs != null && prefs.newsApiKey.isNotEmpty()) {
                viewModel.refreshNews()
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = NewsAdapter(
            onArticleClick = { article ->
                val intent = Intent(this, ArticleDetailActivity::class.java)
                intent.putExtra("article_id", article.id)
                startActivity(intent)
            },
            onSaveClick = { article ->
                viewModel.toggleSaveArticle(article)
            },
            onAnalyzeClick = { article ->
                viewModel.analyzeArticle(article)
                Toast.makeText(this, "Analyzing article...", Toast.LENGTH_SHORT).show()
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.articles.observe(this) { articles ->
            adapter.submitList(articles)
        }

        viewModel.isLoading.observe(this) { isLoading ->
            swipeRefresh.isRefreshing = isLoading
            progressBar.visibility = if (isLoading && adapter.itemCount == 0) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.action_refresh -> {
                viewModel.refreshNews()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
