package com.example.silver

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calculation.JewelleryCalculator
import com.example.database.CalculationHistoryEntity
import com.example.database.CalculationHistoryRepository
import com.example.models.CalculationInput
import com.example.models.CalculationResult
import com.example.models.MetalType
import com.example.models.SilverPurity
import com.example.settings.AppSettings
import com.example.settings.SettingsRepository
import com.example.sharing.CalculationShareHelper
import com.example.tts.TextToSpeechHelper
import com.example.validation.CalculationValidator
import com.example.validation.ValidationResult
import com.example.voice.VoiceCommandParser
import com.example.voice.VoiceSpeechRecognizer
import com.example.voice.VoiceUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SilverUiState(
    val gramsText: String = "",
    val milligramsText: String = "",
    val selectedPurity: SilverPurity = SilverPurity.P999,
    val marketRateText: String = "",
    val makingChargeText: String = "5.0",
    val wastageText: String = "0.0",
    val gstText: String = "3.0",
    val result: CalculationResult? = null,
    val errorMessage: String? = null,
    val isSaved: Boolean = false,
    val showResult: Boolean = false
)

class SilverViewModel(
    context: Context,
    private val historyRepository: CalculationHistoryRepository,
    private val settingsRepository: SettingsRepository,
    private val ttsHelper: TextToSpeechHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(SilverUiState())
    val uiState: StateFlow<SilverUiState> = _uiState.asStateFlow()

    val settings: StateFlow<AppSettings> = settingsRepository.settings

    private val voiceRecognizer = VoiceSpeechRecognizer(context.applicationContext) { spokenText ->
        processVoiceCommand(spokenText)
    }
    val voiceState: StateFlow<VoiceUiState> = voiceRecognizer.uiState

    init {
        val defaultGst = settingsRepository.settings.value.defaultGst
        _uiState.value = _uiState.value.copy(gstText = defaultGst.toString())
    }

    fun startVoiceListening() {
        if (!settings.value.isVoiceControlEnabled) return
        voiceRecognizer.startListening(
            voiceLanguage = settings.value.voiceLanguage,
            continuous = settings.value.isContinuousListening
        )
    }

    fun stopVoiceListening() {
        voiceRecognizer.stopListening()
    }

    fun processVoiceCommand(spokenText: String) {
        val parsed = VoiceCommandParser.parse(spokenText, MetalType.SILVER)

        if (parsed.isResetTriggered) {
            resetCalculation()
            voiceRecognizer.setStatusMessage(parsed.feedbackMessage, parsed.feedbackMessage)
            return
        }

        if (parsed.clarificationNeeded) {
            voiceRecognizer.setStatusMessage(parsed.clarificationPrompt, parsed.clarificationPrompt)
            return
        }

        var currentGrams = _uiState.value.gramsText
        var currentMg = _uiState.value.milligramsText
        var currentRate = _uiState.value.marketRateText
        var currentMaking = _uiState.value.makingChargeText
        var currentWastage = _uiState.value.wastageText
        var currentGst = _uiState.value.gstText
        var currentPurity = _uiState.value.selectedPurity

        if (parsed.grams != null) {
            currentGrams = if (parsed.grams == parsed.grams.toLong().toDouble()) {
                parsed.grams.toLong().toString()
            } else {
                parsed.grams.toString()
            }
        }
        if (parsed.milligrams != null) {
            currentMg = if (parsed.milligrams == parsed.milligrams.toLong().toDouble()) {
                parsed.milligrams.toLong().toString()
            } else {
                parsed.milligrams.toString()
            }
        }
        if (parsed.marketRate != null) {
            currentRate = if (parsed.marketRate == parsed.marketRate.toLong().toDouble()) {
                parsed.marketRate.toLong().toString()
            } else {
                parsed.marketRate.toString()
            }
        }
        if (parsed.makingChargePercent != null) {
            currentMaking = parsed.makingChargePercent.toString()
        }
        if (parsed.wastagePercent != null) {
            currentWastage = parsed.wastagePercent.toString()
        }
        if (parsed.gstPercent != null) {
            currentGst = parsed.gstPercent.toString()
        }
        // ONLY update purity if user explicitly stated it (OPTIONAL)
        if (parsed.silverPurity != null) {
            currentPurity = parsed.silverPurity
        }

        _uiState.value = _uiState.value.copy(
            gramsText = currentGrams,
            milligramsText = currentMg,
            marketRateText = currentRate,
            makingChargeText = currentMaking,
            wastageText = currentWastage,
            gstText = currentGst,
            selectedPurity = currentPurity,
            errorMessage = null
        )

        voiceRecognizer.setStatusMessage(parsed.feedbackMessage, parsed.feedbackMessage)

        if (parsed.isCalculateTriggered) {
            calculateSilverPrice()
        }
    }

    fun onGramsChange(value: String) {
        _uiState.value = _uiState.value.copy(gramsText = value, errorMessage = null)
    }

    fun onMilligramsChange(value: String) {
        _uiState.value = _uiState.value.copy(milligramsText = value, errorMessage = null)
    }

    fun onPuritySelect(purity: SilverPurity) {
        _uiState.value = _uiState.value.copy(selectedPurity = purity, errorMessage = null)
    }

    fun onMarketRateChange(value: String) {
        _uiState.value = _uiState.value.copy(marketRateText = value, errorMessage = null)
    }

    fun onMakingChargeChange(value: String) {
        _uiState.value = _uiState.value.copy(makingChargeText = value, errorMessage = null)
    }

    fun onWastageChange(value: String) {
        _uiState.value = _uiState.value.copy(wastageText = value, errorMessage = null)
    }

    fun onGstChange(value: String) {
        _uiState.value = _uiState.value.copy(gstText = value, errorMessage = null)
    }

    fun calculateSilverPrice() {
        val state = _uiState.value
        val validation = CalculationValidator.validateInput(
            gramsText = state.gramsText,
            milligramsText = state.milligramsText,
            marketRateText = state.marketRateText,
            makingChargeText = state.makingChargeText,
            wastageText = state.wastageText,
            gstText = state.gstText
        )

        when (validation) {
            is ValidationResult.Invalid -> {
                _uiState.value = _uiState.value.copy(errorMessage = validation.errorMessage)
            }
            is ValidationResult.Valid -> {
                val grams = state.gramsText.toDoubleOrNull() ?: 0.0
                val mg = state.milligramsText.toDoubleOrNull() ?: 0.0
                val marketRate = state.marketRateText.toDoubleOrNull() ?: 0.0
                val making = state.makingChargeText.toDoubleOrNull() ?: 0.0
                val wastage = state.wastageText.toDoubleOrNull() ?: 0.0
                val gst = state.gstText.toDoubleOrNull() ?: 0.0

                val input = CalculationInput(
                    metalType = MetalType.SILVER,
                    grams = grams,
                    milligrams = mg,
                    purityLabel = state.selectedPurity.label,
                    purityFineness = state.selectedPurity.fineness,
                    purityFactor = state.selectedPurity.purityFactor,
                    marketRate = marketRate,
                    makingChargePercent = making,
                    wastagePercent = wastage,
                    gstPercent = gst
                )

                val result = JewelleryCalculator.calculate(input)
                _uiState.value = _uiState.value.copy(
                    result = result,
                    showResult = true,
                    isSaved = false,
                    errorMessage = null
                )

                if (settings.value.autoSaveHistory) {
                    saveCalculation()
                }

                if (settings.value.isTtsEnabled && settings.value.isVoiceResponseEnabled) {
                    speakResult()
                }
            }
        }
    }

    fun speakResult() {
        val result = _uiState.value.result ?: return
        val currentSettings = settings.value
        ttsHelper.speakResult(result, currentSettings.language, currentSettings.speechSpeed)
    }

    fun saveCalculation() {
        val result = _uiState.value.result ?: return
        if (_uiState.value.isSaved) return

        viewModelScope.launch {
            val entity = CalculationHistoryEntity.fromCalculationResult(result)
            historyRepository.insert(entity)
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }

    fun shareCalculation(context: Context) {
        val result = _uiState.value.result ?: return
        CalculationShareHelper.shareCalculation(context, result)
    }

    fun resetCalculation() {
        _uiState.value = _uiState.value.copy(
            gramsText = "",
            milligramsText = "",
            marketRateText = "",
            result = null,
            showResult = false,
            errorMessage = null,
            isSaved = false
        )
    }

    fun editCalculation() {
        _uiState.value = _uiState.value.copy(showResult = false)
    }

    override fun onCleared() {
        super.onCleared()
        voiceRecognizer.destroy()
    }
}
