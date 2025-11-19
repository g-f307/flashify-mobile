package com.example.flashify.view.ui.screen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.flashify.model.manager.ThemeManager
import com.example.flashify.model.util.AppNavigation
import com.example.flashify.view.ui.theme.FlashifyTheme

class MainActivity : ComponentActivity() {

    private lateinit var themeManager: ThemeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        themeManager = ThemeManager(this)

        setContent {
            val isDarkTheme by themeManager.isDarkTheme.collectAsState(initial = true)

            FlashifyTheme(darkTheme = isDarkTheme) {
                AppNavigation()
            }
        }
    }
}