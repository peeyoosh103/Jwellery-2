package com.example.settings

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class AppLanguage(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    HINDI("hi", "हिंदी (Hindi)")
}

enum class VoiceLanguage(val code: String, val displayName: String, val localeTag: String) {
    HINDI_ENGLISH("hi_en", "Hindi / Hinglish (हिंदी)", "hi-IN"),
    HINDI("hi", "Pure Hindi (शुद्ध हिंदी)", "hi-IN"),
    ENGLISH("en", "English (India)", "en-IN")
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val defaultGst: Double = 3.0,
    val isRoundingEnabled: Boolean = true,
    val isTtsEnabled: Boolean = true,
    val isVoiceControlEnabled: Boolean = true,
    val isContinuousListening: Boolean = false,
    val isVoiceResponseEnabled: Boolean = true,
    val voiceLanguage: VoiceLanguage = VoiceLanguage.HINDI_ENGLISH,
    val speechSpeed: Float = 1.0f,
    val autoSaveHistory: Boolean = true
)
