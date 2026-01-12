package com.newscorrelator.app.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.newscorrelator.app.R
import com.newscorrelator.app.data.Article
import com.newscorrelator.app.utils.toRelativeTime

class NewsAdapter(
    private val onArticleClick: (Article) -> Unit,
    private val onSaveClick: (Article) -> Unit,
    private val onAnalyzeClick: (Article) -> Unit
) : ListAdapter<Article, NewsAdapter.ArticleViewHolder>(ArticleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_article, parent, false)
        return ArticleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText: TextView = itemView.findViewById(R.id.titleText)
        private val descriptionText: TextView = itemView.findViewById(R.id.descriptionText)
        private val sourceText: TextView = itemView.findViewById(R.id.sourceText)
        private val timeText: TextView = itemView.findViewById(R.id.timeText)
        private val integrityLayout: View = itemView.findViewById(R.id.integrityLayout)
        private val integrityIndicator: View = itemView.findViewById(R.id.integrityIndicator)
        private val integrityScoreText: TextView = itemView.findViewById(R.id.integrityScoreText)

        fun bind(article: Article) {
            titleText.text = article.title
            descriptionText.text = article.description ?: ""
            sourceText.text = article.source
            timeText.text = article.publishedAt.toRelativeTime()

            // Show integrity info if analyzed
            if (article.analyzed && article.integrityScore != null) {
                integrityLayout.visibility = View.VISIBLE
                integrityScoreText.text = String.format("%.1f/10", article.integrityScore)
                
                // Set indicator color based on status
                val color = when (article.integrityStatus) {
                    "GREEN" -> Color.parseColor("#4CAF50")
                    "YELLOW" -> Color.parseColor("#FFC107")
                    "RED" -> Color.parseColor("#F44336")
                    else -> Color.parseColor("#9E9E9E")
                }
                integrityIndicator.setBackgroundColor(color)
            } else {
                integrityLayout.visibility = View.GONE
            }

            itemView.setOnClickListener { onArticleClick(article) }
        }
    }

    class ArticleDiffCallback : DiffUtil.ItemCallback<Article>() {
        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem == newItem
        }
    }
}
