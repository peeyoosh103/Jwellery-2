package com.example.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.database.CalculationHistoryRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val historyRepository: CalculationHistoryRepository
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settings

    fun setThemeMode(themeMode: ThemeMode) {
        settingsRepository.updateTheme(themeMode)
    }

    fun setLanguage(language: AppLanguage) {
        settingsRepository.updateLanguage(language)
    }

    fun setDefaultGst(gst: Double) {
        settingsRepository.updateDefaultGst(gst)
    }

    fun setRounding(rounding: Boolean) {
        settingsRepository.updateRounding(rounding)
    }

    fun setTtsEnabled(enabled: Boolean) {
        settingsRepository.updateTtsEnabled(enabled)
    }

    fun setVoiceControlEnabled(enabled: Boolean) {
        settingsRepository.updateVoiceControlEnabled(enabled)
    }

    fun setContinuousListening(enabled: Boolean) {
        settingsRepository.updateContinuousListening(enabled)
    }

    fun setVoiceResponseEnabled(enabled: Boolean) {
        settingsRepository.updateVoiceResponseEnabled(enabled)
    }

    fun setVoiceLanguage(language: VoiceLanguage) {
        settingsRepository.updateVoiceLanguage(language)
    }

    fun setSpeechSpeed(speed: Float) {
        settingsRepository.updateSpeechSpeed(speed)
    }

    fun setAutoSaveHistory(autoSave: Boolean) {
        settingsRepository.updateAutoSave(autoSave)
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            historyRepository.clearAll()
        }
    }
}
