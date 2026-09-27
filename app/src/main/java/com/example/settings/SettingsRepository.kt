package com.example.settings

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("peeyoosh_settings_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun loadSettings(): AppSettings {
        val themeOrdinal = prefs.getInt(KEY_THEME, ThemeMode.SYSTEM.ordinal)
        val theme = ThemeMode.entries.getOrElse(themeOrdinal) { ThemeMode.SYSTEM }

        val langOrdinal = prefs.getInt(KEY_LANG, AppLanguage.ENGLISH.ordinal)
        val lang = AppLanguage.entries.getOrElse(langOrdinal) { AppLanguage.ENGLISH }

        val defaultGst = prefs.getFloat(KEY_GST, 3.0f).toDouble()
        val rounding = prefs.getBoolean(KEY_ROUNDING, true)
        val ttsEnabled = prefs.getBoolean(KEY_TTS_ENABLED, true)
        val voiceControlEnabled = prefs.getBoolean(KEY_VOICE_CONTROL_ENABLED, true)
        val continuousListening = prefs.getBoolean(KEY_CONTINUOUS_LISTENING, false)
        val voiceResponseEnabled = prefs.getBoolean(KEY_VOICE_RESPONSE_ENABLED, true)

        val voiceLangOrdinal = prefs.getInt(KEY_VOICE_LANG, VoiceLanguage.HINDI_ENGLISH.ordinal)
        val voiceLang = VoiceLanguage.entries.getOrElse(voiceLangOrdinal) { VoiceLanguage.HINDI_ENGLISH }

        val speechSpeed = prefs.getFloat(KEY_SPEECH_SPEED, 1.0f)
        val autoSave = prefs.getBoolean(KEY_AUTO_SAVE, true)

        return AppSettings(
            themeMode = theme,
            language = lang,
            defaultGst = defaultGst,
            isRoundingEnabled = rounding,
            isTtsEnabled = ttsEnabled,
            isVoiceControlEnabled = voiceControlEnabled,
            isContinuousListening = continuousListening,
            isVoiceResponseEnabled = voiceResponseEnabled,
            voiceLanguage = voiceLang,
            speechSpeed = speechSpeed,
            autoSaveHistory = autoSave
        )
    }

    fun updateTheme(themeMode: ThemeMode) {
        prefs.edit().putInt(KEY_THEME, themeMode.ordinal).apply()
        _settings.value = _settings.value.copy(themeMode = themeMode)
    }

    fun updateLanguage(language: AppLanguage) {
        prefs.edit().putInt(KEY_LANG, language.ordinal).apply()
        _settings.value = _settings.value.copy(language = language)
    }

    fun updateDefaultGst(gst: Double) {
        prefs.edit().putFloat(KEY_GST, gst.toFloat()).apply()
        _settings.value = _settings.value.copy(defaultGst = gst)
    }

    fun updateRounding(rounding: Boolean) {
        prefs.edit().putBoolean(KEY_ROUNDING, rounding).apply()
        _settings.value = _settings.value.copy(isRoundingEnabled = rounding)
    }

    fun updateTtsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_TTS_ENABLED, enabled).apply()
        _settings.value = _settings.value.copy(isTtsEnabled = enabled)
    }

    fun updateVoiceControlEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VOICE_CONTROL_ENABLED, enabled).apply()
        _settings.value = _settings.value.copy(isVoiceControlEnabled = enabled)
    }

    fun updateContinuousListening(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_CONTINUOUS_LISTENING, enabled).apply()
        _settings.value = _settings.value.copy(isContinuousListening = enabled)
    }

    fun updateVoiceResponseEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VOICE_RESPONSE_ENABLED, enabled).apply()
        _settings.value = _settings.value.copy(isVoiceResponseEnabled = enabled)
    }

    fun updateVoiceLanguage(language: VoiceLanguage) {
        prefs.edit().putInt(KEY_VOICE_LANG, language.ordinal).apply()
        _settings.value = _settings.value.copy(voiceLanguage = language)
    }

    fun updateSpeechSpeed(speed: Float) {
        prefs.edit().putFloat(KEY_SPEECH_SPEED, speed).apply()
        _settings.value = _settings.value.copy(speechSpeed = speed)
    }

    fun updateAutoSave(autoSave: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_SAVE, autoSave).apply()
        _settings.value = _settings.value.copy(autoSaveHistory = autoSave)
    }

    companion object {
        private const val KEY_THEME = "theme_mode"
        private const val KEY_LANG = "app_language"
        private const val KEY_GST = "default_gst"
        private const val KEY_ROUNDING = "is_rounding"
        private const val KEY_TTS_ENABLED = "tts_enabled"
        private const val KEY_VOICE_CONTROL_ENABLED = "voice_control_enabled"
        private const val KEY_CONTINUOUS_LISTENING = "continuous_listening"
        private const val KEY_VOICE_RESPONSE_ENABLED = "voice_response_enabled"
        private const val KEY_VOICE_LANG = "voice_language"
        private const val KEY_SPEECH_SPEED = "speech_speed"
        private const val KEY_AUTO_SAVE = "auto_save"
    }
}
