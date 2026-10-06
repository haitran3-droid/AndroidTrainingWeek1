package com.example.localization

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.os.LocaleListCompat
import com.example.localization.ui.theme.LocalizationTheme
import androidx.core.content.edit

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val preferences = getSharedPreferences("input", MODE_PRIVATE)

        setContent {
            LocalizationTheme {
                var name by rememberSaveable {
                    mutableStateOf(preferences.getString("name", "Android").orEmpty())
                }
                LocalizationScreen(
                    name = name,
                    onNameChange = { value ->
                        name = value
                        preferences.edit { putString("name", value) }
                    },
                    onLanguageChange = { languageTag ->
                        preferences.edit { putString("name", name) }
                        AppCompatDelegate.setApplicationLocales(
                            LocaleListCompat.forLanguageTags(languageTag)
                        )
                    }
                )
            }
        }
    }
}


