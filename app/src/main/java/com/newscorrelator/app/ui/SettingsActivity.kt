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
import com.newscorrelator.app.utils.LogManager

class SettingsActivity : AppCompatActivity() {
    private lateinit var viewModel: NewsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        LogManager.i("SettingsActivity.onCreate() started")
        
        try {
            super.onCreate(savedInstanceState)
            LogManager.i("SettingsActivity.super.onCreate() completed")
            
            LogManager.i("Setting content view")
            setContentView(R.layout.activity_settings)
            LogManager.i("Content view set successfully")

            try {
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
                supportActionBar?.title = "Settings"
                LogManager.i("Action bar configured")
            } catch (e: Exception) {
                LogManager.e("Error configuring action bar", e)
            }

            LogManager.i("Getting ViewModel")
            viewModel = ViewModelProvider(this)[NewsViewModel::class.java]
            LogManager.i("ViewModel created successfully")

            LogManager.i("Finding views")
            val newsApiKeyEdit = findViewById<EditText>(R.id.newsApiKeyEdit)
            val openRouterApiKeyEdit = findViewById<EditText>(R.id.openRouterApiKeyEdit)
            val categoriesEdit = findViewById<EditText>(R.id.categoriesEdit)
            val keywordsEdit = findViewById<EditText>(R.id.keywordsEdit)
            val sourcesPerTopicEdit = findViewById<EditText>(R.id.sourcesPerTopicEdit)
            val enableAiSwitch = findViewById<Switch>(R.id.enableAiSwitch)
            val saveButton = findViewById<Button>(R.id.saveButton)
            LogManager.i("All views found successfully")

            // Load current preferences
            try {
                LogManager.i("Observing preferences")
                viewModel.preferences.observe(this) { prefs ->
                    try {
                        LogManager.i("Preferences changed: ${prefs != null}")
                        if (prefs != null) {
                            newsApiKeyEdit.setText(prefs.newsApiKey)
                            openRouterApiKeyEdit.setText(prefs.openRouterApiKey)
                            categoriesEdit.setText(prefs.categories)
                            keywordsEdit.setText(prefs.keywords)
                            sourcesPerTopicEdit.setText(prefs.sourcesPerTopic.toString())
                            enableAiSwitch.isChecked = prefs.enableAiAnalysis
                            LogManager.i("Preferences loaded into UI")
                        }
                    } catch (e: Exception) {
                        LogManager.e("Error loading preferences into UI", e)
                        Toast.makeText(this, "Error loading preferences: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                LogManager.e("Error setting up preferences observer", e)
                Toast.makeText(this, "Error loading preferences", Toast.LENGTH_SHORT).show()
            }

            saveButton.setOnClickListener {
                try {
                    LogManager.i("Save button clicked")
                    val newsApiKey = newsApiKeyEdit.text.toString()
                    val openRouterApiKey = openRouterApiKeyEdit.text.toString()
                    val categories = categoriesEdit.text.toString()
                    val keywords = keywordsEdit.text.toString()
                    val sourcesPerTopic = sourcesPerTopicEdit.text.toString().toIntOrNull() ?: 4
                    val enableAi = enableAiSwitch.isChecked

                    if (newsApiKey.isEmpty()) {
                        LogManager.w("Save attempted with empty News API Key")
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

                    LogManager.i("Saving preferences")
                    viewModel.savePreferences(preferences)
                    Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()
                    LogManager.i("Settings saved successfully")
                    finish()
                } catch (e: Exception) {
                    LogManager.e("Error saving settings", e)
                    Toast.makeText(this, "Error saving settings: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
            
            LogManager.i("SettingsActivity.onCreate() completed successfully")
        } catch (e: Exception) {
            LogManager.e("Error in SettingsActivity.onCreate()", e)
            Toast.makeText(this, "Error initializing settings: ${e.message}", Toast.LENGTH_LONG).show()
            // Don't crash the app, let user try to navigate back
        }
    }

    override fun onStart() {
        super.onStart()
        LogManager.i("SettingsActivity.onStart()")
    }
    
    override fun onResume() {
        super.onResume()
        LogManager.i("SettingsActivity.onResume()")
    }
    
    override fun onPause() {
        super.onPause()
        LogManager.i("SettingsActivity.onPause()")
    }
    
    override fun onStop() {
        super.onStop()
        LogManager.i("SettingsActivity.onStop()")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        LogManager.i("SettingsActivity.onDestroy()")
    }

    override fun onSupportNavigateUp(): Boolean {
        LogManager.i("SettingsActivity navigate up")
        finish()
        return true
    }
}
