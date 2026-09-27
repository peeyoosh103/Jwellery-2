package com.example

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.database.CalculationHistoryRepository
import com.example.gold.GoldViewModel
import com.example.history.HistoryViewModel
import com.example.settings.SettingsRepository
import com.example.settings.SettingsViewModel
import com.example.silver.SilverViewModel
import com.example.tts.TextToSpeechHelper

class ViewModelFactory(
    private val context: Context,
    private val historyRepository: CalculationHistoryRepository,
    private val settingsRepository: SettingsRepository,
    private val ttsHelper: TextToSpeechHelper
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(GoldViewModel::class.java) -> {
                GoldViewModel(context, historyRepository, settingsRepository, ttsHelper) as T
            }
            modelClass.isAssignableFrom(SilverViewModel::class.java) -> {
                SilverViewModel(context, historyRepository, settingsRepository, ttsHelper) as T
            }
            modelClass.isAssignableFrom(HistoryViewModel::class.java) -> {
                HistoryViewModel(historyRepository) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(settingsRepository, historyRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
