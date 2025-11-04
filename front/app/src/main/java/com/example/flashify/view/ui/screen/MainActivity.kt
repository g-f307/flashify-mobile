package com.example.flashify.view.ui.screen // <-- ALTERADO

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.flashify.model.util.AppNavigation
import com.example.flashify.view.ui.theme.FlashifyTheme

// Esta classe agora está no pacote correto
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FlashifyTheme {
                AppNavigation()
            }
        }
    }
}
