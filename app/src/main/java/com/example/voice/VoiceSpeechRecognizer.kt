package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.example.settings.VoiceLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceSpeechRecognizer(
    private val context: Context,
    private val onCommandRecognized: (String) -> Unit
) : RecognitionListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _uiState = MutableStateFlow(VoiceUiState())
    val uiState: StateFlow<VoiceUiState> = _uiState.asStateFlow()

    private var isContinuousListening: Boolean = false
    private var isManuallyStopped: Boolean = false
    private var currentVoiceLanguage: VoiceLanguage = VoiceLanguage.HINDI_ENGLISH

    fun startListening(
        voiceLanguage: VoiceLanguage = VoiceLanguage.HINDI_ENGLISH,
        continuous: Boolean = false
    ) {
        currentVoiceLanguage = voiceLanguage
        isContinuousListening = continuous
        isManuallyStopped = false

        mainHandler.post {
            try {
                if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                    _uiState.value = _uiState.value.copy(
                        state = VoiceListeningState.ERROR,
                        errorMessage = "Speech recognition is not available on this device."
                    )
                    return@post
                }

                speechRecognizer?.destroy()
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(this@VoiceSpeechRecognizer)
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, voiceLanguage.localeTag)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, voiceLanguage.localeTag)
                    putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, voiceLanguage.localeTag)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    // Enable both Hindi and English recognition
                    putExtra(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES, arrayListOf("hi-IN", "en-IN", "en-US"))
                }

                speechRecognizer?.startListening(intent)

                _uiState.value = _uiState.value.copy(
                    state = VoiceListeningState.LISTENING,
                    partialTranscript = "",
                    finalTranscript = "",
                    audioRms = 0f,
                    statusMessage = "Listening... Bolna shuru karein",
                    isContinuousListeningActive = continuous,
                    errorMessage = null
                )
            } catch (e: Exception) {
                Log.e("VoiceSpeechRecognizer", "Error starting speech recognizer", e)
                _uiState.value = _uiState.value.copy(
                    state = VoiceListeningState.ERROR,
                    errorMessage = "Could not start voice recognition: ${e.localizedMessage}"
                )
            }
        }
    }

    fun stopListening() {
        isManuallyStopped = true
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
            } catch (e: Exception) {
                Log.w("VoiceSpeechRecognizer", "Error stopping speech recognizer", e)
            }
            _uiState.value = _uiState.value.copy(
                state = VoiceListeningState.IDLE,
                partialTranscript = "",
                audioRms = 0f,
                statusMessage = null,
                isContinuousListeningActive = false
            )
        }
    }

    fun setContinuousListeningMode(enabled: Boolean) {
        isContinuousListening = enabled
        _uiState.value = _uiState.value.copy(isContinuousListeningActive = enabled)
    }

    fun setStatusMessage(msg: String?, feedback: String? = null) {
        _uiState.value = _uiState.value.copy(
            statusMessage = msg,
            feedbackMessage = feedback
        )
    }

    fun resetState() {
        _uiState.value = VoiceUiState()
    }

    fun destroy() {
        isManuallyStopped = true
        mainHandler.post {
            try {
                speechRecognizer?.destroy()
                speechRecognizer = null
            } catch (e: Exception) {
                Log.w("VoiceSpeechRecognizer", "Error destroying speech recognizer", e)
            }
        }
    }

    // RecognitionListener Callbacks

    override fun onReadyForSpeech(params: Bundle?) {
        _uiState.value = _uiState.value.copy(
            state = VoiceListeningState.LISTENING,
            statusMessage = "Listening... Bolna shuru karein"
        )
    }

    override fun onBeginningOfSpeech() {
        _uiState.value = _uiState.value.copy(
            state = VoiceListeningState.LISTENING,
            statusMessage = "Listening..."
        )
    }

    override fun onRmsChanged(rmsdB: Float) {
        _uiState.value = _uiState.value.copy(audioRms = (rmsdB + 2f).coerceAtLeast(0f))
    }

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {
        _uiState.value = _uiState.value.copy(
            state = VoiceListeningState.PROCESSING,
            statusMessage = "Processing voice input..."
        )
    }

    override fun onError(error: Int) {
        val errorMessage = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
            SpeechRecognizer.ERROR_NETWORK -> "Network issue for speech recognition"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "Kuch samajh nahi aaya, dobara bolein"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition engine busy"
            SpeechRecognizer.ERROR_SERVER -> "Server error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
            else -> "Voice recognition error ($error)"
        }

        Log.w("VoiceSpeechRecognizer", "SpeechRecognizer error: $error ($errorMessage)")

        _uiState.value = _uiState.value.copy(
            state = if (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) VoiceListeningState.IDLE else VoiceListeningState.ERROR,
            statusMessage = if (error == SpeechRecognizer.ERROR_NO_MATCH) "Kuch samajh nahi aaya, dobara bolein" else null,
            errorMessage = if (error != SpeechRecognizer.ERROR_NO_MATCH && error != SpeechRecognizer.ERROR_SPEECH_TIMEOUT) errorMessage else null,
            audioRms = 0f
        )

        // If continuous listening is enabled and not manually cancelled, resume listening after brief timeout
        if (isContinuousListening && !isManuallyStopped && (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT)) {
            mainHandler.postDelayed({
                if (isContinuousListening && !isManuallyStopped) {
                    startListening(currentVoiceLanguage, true)
                }
            }, 1000)
        }
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val text = matches?.firstOrNull()?.trim() ?: ""

        if (text.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                state = VoiceListeningState.SUCCESS,
                finalTranscript = text,
                partialTranscript = text,
                statusMessage = "Command: \"$text\"",
                audioRms = 0f
            )

            // Trigger action handler
            onCommandRecognized(text)

            // If continuous listening is enabled, resume listening after short delay
            if (isContinuousListening && !isManuallyStopped) {
                mainHandler.postDelayed({
                    if (isContinuousListening && !isManuallyStopped) {
                        startListening(currentVoiceLanguage, true)
                    }
                }, 1200)
            }
        } else {
            _uiState.value = _uiState.value.copy(
                state = VoiceListeningState.IDLE,
                statusMessage = "Kuch samajh nahi aaya, dobara bolein",
                audioRms = 0f
            )
            if (isContinuousListening && !isManuallyStopped) {
                mainHandler.postDelayed({
                    if (isContinuousListening && !isManuallyStopped) {
                        startListening(currentVoiceLanguage, true)
                    }
                }, 1000)
            }
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val partial = matches?.firstOrNull() ?: ""
        if (partial.isNotBlank()) {
            _uiState.value = _uiState.value.copy(
                state = VoiceListeningState.LISTENING,
                partialTranscript = partial
            )
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}
}
