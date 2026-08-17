package com.example.yzuwifilocationresearch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.yzuwifilocationresearch.navigation.AppNavGraph
import com.example.yzuwifilocationresearch.ui.theme.YZUWifiLocationResearchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YZUWifiLocationResearchTheme {
                AppNavGraph()
            }
        }
    }
}
