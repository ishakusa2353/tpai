package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.data.local.LicenseStorage
import com.example.data.model.AssistantState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceAssistantManager(
    private val context: Context,
    private val licenseStorage: LicenseStorage,
    private val onSpeechRecognized: (String) -> Unit
) : RecognitionListener, TextToSpeech.OnInitListener {

    private val _assistantState = MutableStateFlow(AssistantState.IDLE)
    val assistantState: StateFlow<AssistantState> = _assistantState.asStateFlow()

    private val _lastRecognizedText = MutableStateFlow("")
    val lastRecognizedText: StateFlow<String> = _lastRecognizedText.asStateFlow()

    private val _audioRmsLevel = MutableStateFlow(0f)
    val audioRmsLevel: StateFlow<Float> = _audioRmsLevel.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false

    init {
        initSpeechRecognizer()
        textToSpeech = TextToSpeech(context, this)
    }

    private fun initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(this@VoiceAssistantManager)
            }
        }
    }

    fun startListening() {
        stopSpeaking()
        if (speechRecognizer == null) {
            initSpeechRecognizer()
        }

        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            _assistantState.value = AssistantState.LISTENING
            speechRecognizer?.startListening(intent)
        } catch (_: Exception) {
            _assistantState.value = AssistantState.IDLE
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) {}
        if (_assistantState.value == AssistantState.LISTENING) {
            _assistantState.value = AssistantState.IDLE
        }
    }

    fun setThinkingState() {
        _assistantState.value = AssistantState.THINKING
    }

    fun speak(text: String, onComplete: (() -> Unit)? = null) {
        stopListening()
        if (!isTtsInitialized || text.isBlank()) {
            _assistantState.value = AssistantState.IDLE
            onComplete?.invoke()
            return
        }

        _assistantState.value = AssistantState.SPEAKING
        textToSpeech?.setSpeechRate(licenseStorage.speechRate)
        textToSpeech?.setPitch(licenseStorage.speechPitch)

        val utteranceId = "TradePilot_Utterance_${System.currentTimeMillis()}"
        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _assistantState.value = AssistantState.SPEAKING
            }

            override fun onDone(utteranceId: String?) {
                _assistantState.value = AssistantState.IDLE
                onComplete?.invoke()
            }

            override fun onError(utteranceId: String?) {
                _assistantState.value = AssistantState.IDLE
                onComplete?.invoke()
            }
        })

        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stopSpeaking() {
        try {
            textToSpeech?.stop()
        } catch (_: Exception) {}
        if (_assistantState.value == AssistantState.SPEAKING) {
            _assistantState.value = AssistantState.IDLE
        }
    }

    fun destroy() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
            textToSpeech?.stop()
            textToSpeech?.shutdown()
            textToSpeech = null
        } catch (_: Exception) {}
    }

    // TextToSpeech.OnInitListener
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech?.setLanguage(Locale.US)
            isTtsInitialized = result != TextToSpeech.LANG_MISSING_DATA &&
                    result != TextToSpeech.LANG_NOT_SUPPORTED
        }
    }

    // RecognitionListener
    override fun onReadyForSpeech(params: Bundle?) {
        _assistantState.value = AssistantState.LISTENING
    }

    override fun onBeginningOfSpeech() {}

    override fun onRmsChanged(rmsdB: Float) {
        _audioRmsLevel.value = (rmsdB + 2f).coerceIn(0f, 10f)
    }

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {
        _assistantState.value = AssistantState.THINKING
    }

    override fun onError(error: Int) {
        _assistantState.value = AssistantState.IDLE
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            val spokenText = matches[0]
            _lastRecognizedText.value = spokenText
            onSpeechRecognized(spokenText)
        } else {
            _assistantState.value = AssistantState.IDLE
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            _lastRecognizedText.value = matches[0]
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}
}
