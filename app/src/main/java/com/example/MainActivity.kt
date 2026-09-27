package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.database.CalculationHistoryRepository
import com.example.database.PeeyooshDatabase
import com.example.gold.GoldCalculatorScreen
import com.example.gold.GoldViewModel
import com.example.history.HistoryScreen
import com.example.history.HistoryViewModel
import com.example.home.HomeScreen
import com.example.models.MetalType
import com.example.settings.SettingsRepository
import com.example.settings.SettingsScreen
import com.example.settings.SettingsViewModel
import com.example.silver.SilverCalculatorScreen
import com.example.silver.SilverViewModel
import com.example.tts.TextToSpeechHelper
import com.example.ui.theme.PeeyooshTheme

enum class AppScreen {
    HOME,
    GOLD_CALCULATOR,
    SILVER_CALCULATOR,
    HISTORY,
    SETTINGS
}

class MainActivity : ComponentActivity() {

    private lateinit var ttsHelper: TextToSpeechHelper
    private lateinit var database: PeeyooshDatabase
    private lateinit var historyRepository: CalculationHistoryRepository
    private lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        ttsHelper = TextToSpeechHelper(this)
        database = PeeyooshDatabase.getDatabase(this)
        historyRepository = CalculationHistoryRepository(database.calculationHistoryDao())
        settingsRepository = SettingsRepository(this)

        setContent {
            val settings by settingsRepository.settings.collectAsState()
            var currentScreen by remember { mutableStateOf(AppScreen.HOME) }

            val historyCount by historyRepository.allHistory.collectAsState(initial = emptyList())

            val currentMetalTheme = when (currentScreen) {
                AppScreen.SILVER_CALCULATOR -> MetalType.SILVER
                AppScreen.GOLD_CALCULATOR -> MetalType.GOLD
                else -> null
            }

            PeeyooshTheme(themeMode = settings.themeMode, metalTheme = currentMetalTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "ScreenTransition"
                    ) { screen ->
                        when (screen) {
                            AppScreen.HOME -> {
                                HomeScreen(
                                    onNavigateToGold = { currentScreen = AppScreen.GOLD_CALCULATOR },
                                    onNavigateToSilver = { currentScreen = AppScreen.SILVER_CALCULATOR },
                                    onNavigateToHistory = { currentScreen = AppScreen.HISTORY },
                                    onNavigateToSettings = { currentScreen = AppScreen.SETTINGS },
                                    historyCount = historyCount.size
                                )
                            }
                            AppScreen.GOLD_CALCULATOR -> {
                                val goldViewModel: GoldViewModel = viewModel(
                                    factory = ViewModelFactory(this@MainActivity.applicationContext, historyRepository, settingsRepository, ttsHelper)
                                )
                                GoldCalculatorScreen(
                                    viewModel = goldViewModel,
                                    onNavigateBack = { currentScreen = AppScreen.HOME }
                                )
                            }
                            AppScreen.SILVER_CALCULATOR -> {
                                val silverViewModel: SilverViewModel = viewModel(
                                    factory = ViewModelFactory(this@MainActivity.applicationContext, historyRepository, settingsRepository, ttsHelper)
                                )
                                SilverCalculatorScreen(
                                    viewModel = silverViewModel,
                                    onNavigateBack = { currentScreen = AppScreen.HOME }
                                )
                            }
                            AppScreen.HISTORY -> {
                                val historyViewModel: HistoryViewModel = viewModel(
                                    factory = ViewModelFactory(this@MainActivity.applicationContext, historyRepository, settingsRepository, ttsHelper)
                                )
                                HistoryScreen(
                                    viewModel = historyViewModel,
                                    isRounding = settings.isRoundingEnabled,
                                    onNavigateBack = { currentScreen = AppScreen.HOME }
                                )
                            }
                            AppScreen.SETTINGS -> {
                                val settingsViewModel: SettingsViewModel = viewModel(
                                    factory = ViewModelFactory(this@MainActivity.applicationContext, historyRepository, settingsRepository, ttsHelper)
                                )
                                SettingsScreen(
                                    viewModel = settingsViewModel,
                                    onNavigateBack = { currentScreen = AppScreen.HOME }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsHelper.shutdown()
    }
}
