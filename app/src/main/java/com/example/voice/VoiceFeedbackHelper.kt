package com.example.voice

import com.example.models.CalculationResult
import com.example.settings.AppSettings
import com.example.tts.TextToSpeechHelper

class VoiceFeedbackHelper(
    private val ttsHelper: TextToSpeechHelper
) {

    fun speakConfirmation(message: String, settings: AppSettings) {
        if (!settings.isVoiceResponseEnabled || !settings.isTtsEnabled) return
        // We can pass short phrases or calculation results
    }

    fun speakResult(result: CalculationResult, settings: AppSettings) {
        if (!settings.isVoiceResponseEnabled || !settings.isTtsEnabled) return
        ttsHelper.speakResult(result, settings.language, settings.speechSpeed)
    }

    fun stop() {
        ttsHelper.stop()
    }
}
