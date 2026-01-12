package com.newscorrelator.app.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.newscorrelator.app.R
import com.newscorrelator.app.data.UserPreference

class SettingsActivity : AppCompatActivity() {
    private lateinit var viewModel: NewsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Settings"

        viewModel = ViewModelProvider(this)[NewsViewModel::class.java]

        val newsApiKeyEdit = findViewById<EditText>(R.id.newsApiKeyEdit)
        val openRouterApiKeyEdit = findViewById<EditText>(R.id.openRouterApiKeyEdit)
        val categoriesEdit = findViewById<EditText>(R.id.categoriesEdit)
        val keywordsEdit = findViewById<EditText>(R.id.keywordsEdit)
        val sourcesPerTopicEdit = findViewById<EditText>(R.id.sourcesPerTopicEdit)
        val enableAiSwitch = findViewById<Switch>(R.id.enableAiSwitch)
        val saveButton = findViewById<Button>(R.id.saveButton)

        // Load current preferences
        viewModel.preferences.observe(this) { prefs ->
            if (prefs != null) {
                newsApiKeyEdit.setText(prefs.newsApiKey)
                openRouterApiKeyEdit.setText(prefs.openRouterApiKey)
                categoriesEdit.setText(prefs.categories)
                keywordsEdit.setText(prefs.keywords)
                sourcesPerTopicEdit.setText(prefs.sourcesPerTopic.toString())
                enableAiSwitch.isChecked = prefs.enableAiAnalysis
            }
        }

        saveButton.setOnClickListener {
            val newsApiKey = newsApiKeyEdit.text.toString()
            val openRouterApiKey = openRouterApiKeyEdit.text.toString()
            val categories = categoriesEdit.text.toString()
            val keywords = keywordsEdit.text.toString()
            val sourcesPerTopic = sourcesPerTopicEdit.text.toString().toIntOrNull() ?: 4
            val enableAi = enableAiSwitch.isChecked

            if (newsApiKey.isEmpty()) {
                Toast.makeText(this, "News API Key is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val preferences = UserPreference(
                newsApiKey = newsApiKey,
                openRouterApiKey = openRouterApiKey,
                categories = categories,
                keywords = keywords,
                sourcesPerTopic = sourcesPerTopic.coerceIn(1, 10),
                enableAiAnalysis = enableAi
            )

            viewModel.savePreferences(preferences)
            Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
