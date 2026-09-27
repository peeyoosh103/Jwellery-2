package com.example.voice

import com.example.models.GoldPurity
import com.example.models.MetalType
import com.example.models.SilverPurity

enum class VoiceListeningState {
    IDLE,
    LISTENING,
    PROCESSING,
    SUCCESS,
    CLARIFICATION,
    ERROR
}

enum class VoiceActionType {
    UPDATE_FIELDS,
    CALCULATE,
    RESET,
    CLARIFICATION,
    UNKNOWN
}

data class ParsedVoiceData(
    val metalType: MetalType? = null,
    val grams: Double? = null,
    val milligrams: Double? = null,
    val marketRate: Double? = null,
    val makingChargePercent: Double? = null,
    val wastagePercent: Double? = null,
    val gstPercent: Double? = null,
    val goldPurity: GoldPurity? = null, // OPTIONAL: Only filled if user explicitly spoken
    val silverPurity: SilverPurity? = null, // OPTIONAL: Only filled if user explicitly spoken
    val isCalculateTriggered: Boolean = false,
    val isResetTriggered: Boolean = false,
    val rawSpokenText: String = "",
    val feedbackMessage: String? = null,
    val spokenConfirmation: String? = null,
    val clarificationNeeded: Boolean = false,
    val clarificationPrompt: String? = null
)

data class VoiceUiState(
    val state: VoiceListeningState = VoiceListeningState.IDLE,
    val partialTranscript: String = "",
    val finalTranscript: String = "",
    val audioRms: Float = 0f,
    val statusMessage: String? = null,
    val feedbackMessage: String? = null,
    val isContinuousListeningActive: Boolean = false,
    val errorMessage: String? = null
)
