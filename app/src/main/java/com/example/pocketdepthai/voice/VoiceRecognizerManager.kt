package com.example.pocketdepthai.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

class VoiceRecognizerManager(private val context: Context) {

    private val mainHandler = Handler(Looper.getMainLooper())

    // SpeechRecognizer MUST be created and used on the Main Thread only
    private var speechRecognizer: SpeechRecognizer? = null

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var onResult: ((String) -> Unit)? = null
    private var isAvailable: Boolean = false

    init {
        mainHandler.post {
            isAvailable = SpeechRecognizer.isRecognitionAvailable(context)
            if (!isAvailable) {
                _error.value = "Speech recognition is not available. Please install Google app."
                Log.e("VoiceManager", "SpeechRecognizer not available on this device")
            } else {
                createRecognizer()
            }
        }
    }

    private fun createRecognizer() {
        // Must be called on main thread
        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(buildListener())
            Log.d("VoiceManager", "SpeechRecognizer created successfully")
        } catch (e: Exception) {
            Log.e("VoiceManager", "Failed to create SpeechRecognizer: ${e.message}")
            _error.value = "Failed to initialize microphone: ${e.message}"
        }
    }

    private fun buildIntent(): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH.toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-US")
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 500L)
        }
    }

    private fun buildListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d("VoiceManager", "Ready for speech")
                _isListening.value = true
                _error.value = null
            }

            override fun onBeginningOfSpeech() {
                Log.d("VoiceManager", "Speech started")
                _isListening.value = true
            }

            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                Log.d("VoiceManager", "Speech ended")
                _isListening.value = false
            }

            override fun onError(error: Int) {
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error. Check microphone."
                    SpeechRecognizer.ERROR_CLIENT -> "Client error. Retrying..."
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission denied. Please allow in Settings."
                    SpeechRecognizer.ERROR_NETWORK -> "No internet. Use offline speech engine."
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout. Check connection."
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected. Tap mic and speak clearly."
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Microphone busy. Resetting..."
                    SpeechRecognizer.ERROR_SERVER -> "Server error. Try again."
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected. Tap mic and speak."
                    else -> "Unknown error ($error)"
                }
                Log.e("VoiceManager", "onError: $error - $errorMessage")
                _error.value = errorMessage
                _isListening.value = false

                // For ERROR_RECOGNIZER_BUSY: destroy and recreate the recognizer on main thread
                if (error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY ||
                    error == SpeechRecognizer.ERROR_CLIENT) {
                    mainHandler.postDelayed({
                        Log.d("VoiceManager", "Recreating recognizer after error $error")
                        createRecognizer()
                    }, 300)
                }
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                Log.d("VoiceManager", "onResults: $matches")
                if (!matches.isNullOrEmpty()) {
                    val text = matches[0]
                    _recognizedText.value = text
                    onResult?.invoke(text)
                }
                _isListening.value = false
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    _recognizedText.value = matches[0]
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    fun startListening(callback: (String) -> Unit) {
        onResult = callback
        _recognizedText.value = ""
        _error.value = null

        mainHandler.post {
            if (!isAvailable) {
                _error.value = "Speech recognition not available on this device."
                return@post
            }
            try {
                // Always recreate to avoid stale/busy state
                createRecognizer()
                mainHandler.postDelayed({
                    try {
                        speechRecognizer?.startListening(buildIntent())
                        Log.d("VoiceManager", "startListening called")
                    } catch (e: Exception) {
                        Log.e("VoiceManager", "startListening exception: ${e.message}")
                        _error.value = "Could not start mic: ${e.message}"
                        _isListening.value = false
                    }
                }, 200) // Small delay to ensure recognizer is fully ready after creation
            } catch (e: Exception) {
                Log.e("VoiceManager", "startListening outer exception: ${e.message}")
                _error.value = "Microphone error: ${e.message}"
                _isListening.value = false
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
                Log.d("VoiceManager", "stopListening called")
            } catch (e: Exception) {
                Log.e("VoiceManager", "stopListening exception: ${e.message}")
            }
            _isListening.value = false
        }
    }

    fun destroy() {
        mainHandler.post {
            try {
                speechRecognizer?.destroy()
                speechRecognizer = null
                Log.d("VoiceManager", "VoiceRecognizerManager destroyed")
            } catch (e: Exception) {
                Log.e("VoiceManager", "destroy exception: ${e.message}")
            }
        }
    }
}
