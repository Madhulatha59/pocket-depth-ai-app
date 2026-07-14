package com.periodontal.ai

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.periodontal.ai.ui.navigation.Screen
import com.periodontal.ai.ui.screens.DashboardScreen
import com.periodontal.ai.ui.screens.PatientsListScreen
import com.periodontal.ai.ui.screens.ProbingSessionScreen
import com.periodontal.ai.ui.screens.ReportDetailScreen
import com.periodontal.ai.ui.screens.SplashScreen
import com.periodontal.ai.ui.theme.PeriodontalTheme
import com.periodontal.ai.viewmodel.PatientViewModel
import com.periodontal.ai.viewmodel.ProbingViewModel
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val patientViewModel: PatientViewModel by viewModels()
    private val probingViewModel: ProbingViewModel by viewModels()

    private var speechRecognizer: SpeechRecognizer? = null
    private var speechRecognizerIntent: Intent? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            initSpeechRecognizer()
        } else {
            Toast.makeText(this, "Microphone permission required for voice probing.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request audio recording permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            initSpeechRecognizer()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        setContent {
            PeriodontalTheme {
                val navController = rememberNavController()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Splash.route
                    ) {
                        composable(Screen.Splash.route) {
                            SplashScreen(
                                onSplashComplete = {
                                    navController.navigate(Screen.Dashboard.route) {
                                        popUpTo(Screen.Splash.route) { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable(Screen.Dashboard.route) {
                            DashboardScreen(
                                onNavigateToProbing = { navController.navigate(Screen.ProbingSession.route) },
                                onNavigateToPatients = { navController.navigate(Screen.PatientsList.route) },
                                onNavigateToReports = { navController.navigate(Screen.ReportDetail.route) }
                            )
                        }
                        composable(Screen.PatientsList.route) {
                            PatientsListScreen(
                                viewModel = patientViewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onPatientSelectedForProbing = { navController.navigate(Screen.ProbingSession.route) }
                            )
                        }
                        composable(Screen.ProbingSession.route) {
                            ProbingSessionScreen(
                                viewModel = probingViewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToReport = { navController.navigate(Screen.ReportDetail.route) }
                            )
                        }
                        composable(Screen.ReportDetail.route) {
                            ReportDetailScreen(
                                viewModel = probingViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
            speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }

            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    probingViewModel.setVoiceListening(true)
                    probingViewModel.setVoiceFeedback("Listening... Speak tooth number or depths")
                }

                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    probingViewModel.setVoiceListening(false)
                }

                override fun onError(error: Int) {
                    probingViewModel.setVoiceListening(false)
                    val message = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                        SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                        SpeechRecognizer.ERROR_NETWORK -> "Network error"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Try again"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer busy"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                        else -> "Voice parsing error. Try again"
                    }
                    probingViewModel.setVoiceFeedback(message)
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        probingViewModel.processVoiceInput(matches[0])
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        probingViewModel.setVoiceTranscript(matches[0])
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
    }

    fun startListening() {
        runOnUiThread {
            try {
                speechRecognizerIntent?.let {
                    speechRecognizer?.startListening(it)
                } ?: run {
                    probingViewModel.setVoiceFeedback("Speech recognizer not initialized")
                }
            } catch (e: Exception) {
                probingViewModel.setVoiceFeedback("Voice recognizer failed to start")
            }
        }
    }

    fun stopListening() {
        runOnUiThread {
            try {
                speechRecognizer?.stopListening()
                probingViewModel.setVoiceListening(false)
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer?.destroy()
    }
}
