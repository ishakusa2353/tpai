package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.data.local.LicenseStorage
import com.example.data.model.AssistantState
import com.example.data.model.ChatMessage
import com.example.data.model.LicenseRecord
import com.example.data.remote.GeminiService
import com.example.data.remote.SupabaseClient
import com.example.ui.components.FloatingOrbOverlay
import com.example.ui.screens.AboutAppScreen
import com.example.ui.screens.ActivationSuccessScreen
import com.example.ui.screens.CameraModeScreen
import com.example.ui.screens.DeveloperInfoScreen
import com.example.ui.screens.LicenseActivationScreen
import com.example.ui.screens.LicenseInfoScreen
import com.example.ui.screens.MainAssistantScreen
import com.example.ui.screens.ScreenSharingScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.VoiceConversationScreen
import com.example.ui.theme.TradePilotTheme
import com.example.voice.VoiceAssistantManager
import kotlinx.coroutines.launch
import java.util.UUID

enum class Screen {
    SPLASH,
    LICENSE_ACTIVATION,
    ACTIVATION_SUCCESS,
    MAIN,
    VOICE_CONVERSATION,
    SCREEN_SHARING,
    CAMERA_MODE,
    SETTINGS,
    DEVELOPER_INFO,
    LICENSE_INFO,
    ABOUT_APP
}

class MainActivity : ComponentActivity() {

    private lateinit var licenseStorage: LicenseStorage
    private lateinit var supabaseClient: SupabaseClient
    private lateinit var geminiService: GeminiService
    private var voiceAssistantManager: VoiceAssistantManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        licenseStorage = LicenseStorage(this)
        supabaseClient = SupabaseClient()
        geminiService = GeminiService()

        setContent {
            TradePilotTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.systemBars
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        TradePilotAppRoot(
                            licenseStorage = licenseStorage,
                            supabaseClient = supabaseClient,
                            geminiService = geminiService,
                            onInitVoiceManager = { onSpeech ->
                                VoiceAssistantManager(
                                    context = this@MainActivity,
                                    licenseStorage = licenseStorage,
                                    onSpeechRecognized = onSpeech
                                ).also { voiceAssistantManager = it }
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceAssistantManager?.destroy()
    }
}

@Composable
fun TradePilotAppRoot(
    licenseStorage: LicenseStorage,
    supabaseClient: SupabaseClient,
    geminiService: GeminiService,
    onInitVoiceManager: ((String) -> Unit) -> VoiceAssistantManager
) {
    var currentScreen by remember { mutableStateOf(Screen.SPLASH) }
    var activeLicenseRecord by remember { mutableStateOf<LicenseRecord?>(null) }
    var isFloatingOrbOverlayVisible by remember { mutableStateOf(false) }

    val chatMessages = remember { mutableStateListOf<ChatMessage>() }
    val coroutineScope = rememberCoroutineScope()

    val context = androidx.compose.ui.platform.LocalContext.current

    // Voice Manager instance
    val voiceManager = remember {
        onInitVoiceManager { recognizedSpeech ->
            chatMessages.add(ChatMessage(UUID.randomUUID().toString(), recognizedSpeech, isUser = true))
            coroutineScope.launch {
                val aiResponse = geminiService.generateResponse(recognizedSpeech)
                chatMessages.add(ChatMessage(UUID.randomUUID().toString(), aiResponse, isUser = false))
                if (licenseStorage.autoVoiceReply) {
                    // voiceManager will speak
                }
            }
        }
    }

    DisposableEffect(voiceManager) {
        onDispose {
            voiceManager.destroy()
        }
    }

    val assistantState by voiceManager.assistantState.collectAsState()
    val recognizedText by voiceManager.lastRecognizedText.collectAsState()

    // Permission Launchers
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            currentScreen = Screen.VOICE_CONVERSATION
            voiceManager.startListening()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            currentScreen = Screen.CAMERA_MODE
        }
    }

    fun startVoiceFlow() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            currentScreen = Screen.VOICE_CONVERSATION
            voiceManager.startListening()
        } else {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    fun startCameraFlow() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            currentScreen = Screen.CAMERA_MODE
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentScreen) {
            Screen.SPLASH -> {
                SplashScreen(
                    licenseStorage = licenseStorage,
                    supabaseClient = supabaseClient,
                    onNavigateToMain = { currentScreen = Screen.MAIN },
                    onNavigateToActivation = { currentScreen = Screen.LICENSE_ACTIVATION }
                )
            }

            Screen.LICENSE_ACTIVATION -> {
                LicenseActivationScreen(
                    licenseStorage = licenseStorage,
                    supabaseClient = supabaseClient,
                    onActivationSuccess = { record ->
                        activeLicenseRecord = record
                        currentScreen = Screen.ACTIVATION_SUCCESS
                    }
                )
            }

            Screen.ACTIVATION_SUCCESS -> {
                ActivationSuccessScreen(
                    license = activeLicenseRecord ?: LicenseRecord(
                        key = licenseStorage.savedLicenseKey ?: "",
                        active = true,
                        tier = licenseStorage.licenseTier,
                        duration = licenseStorage.licenseDuration
                    ),
                    onContinue = { currentScreen = Screen.MAIN }
                )
            }

            Screen.MAIN -> {
                MainAssistantScreen(
                    assistantState = assistantState,
                    onStartVoice = { startVoiceFlow() },
                    onOpenScreenShare = { currentScreen = Screen.SCREEN_SHARING },
                    onOpenCamera = { startCameraFlow() },
                    onOpenSettings = { currentScreen = Screen.SETTINGS },
                    onToggleFloatingOrb = {
                        isFloatingOrbOverlayVisible = !isFloatingOrbOverlayVisible
                    },
                    isFloatingOrbActive = isFloatingOrbOverlayVisible
                )
            }

            Screen.VOICE_CONVERSATION -> {
                VoiceConversationScreen(
                    state = assistantState,
                    recognizedText = recognizedText,
                    messages = chatMessages,
                    onStartListening = { voiceManager.startListening() },
                    onStopListening = { voiceManager.stopListening() },
                    onEndSession = {
                        voiceManager.stopSpeaking()
                        voiceManager.stopListening()
                    },
                    onBack = { currentScreen = Screen.MAIN }
                )
            }

            Screen.SCREEN_SHARING -> {
                ScreenSharingScreen(
                    geminiService = geminiService,
                    onSpeak = { text ->
                        if (licenseStorage.autoVoiceReply) {
                            voiceManager.speak(text)
                        }
                    },
                    onBack = { currentScreen = Screen.MAIN }
                )
            }

            Screen.CAMERA_MODE -> {
                CameraModeScreen(
                    geminiService = geminiService,
                    onSpeak = { text ->
                        if (licenseStorage.autoVoiceReply) {
                            voiceManager.speak(text)
                        }
                    },
                    onBack = { currentScreen = Screen.MAIN }
                )
            }

            Screen.SETTINGS -> {
                SettingsScreen(
                    licenseStorage = licenseStorage,
                    onNavigateToDeveloperInfo = { currentScreen = Screen.DEVELOPER_INFO },
                    onNavigateToLicenseInfo = { currentScreen = Screen.LICENSE_INFO },
                    onNavigateToAboutApp = { currentScreen = Screen.ABOUT_APP },
                    onBack = { currentScreen = Screen.MAIN }
                )
            }

            Screen.DEVELOPER_INFO -> {
                DeveloperInfoScreen(onBack = { currentScreen = Screen.SETTINGS })
            }

            Screen.LICENSE_INFO -> {
                LicenseInfoScreen(
                    licenseStorage = licenseStorage,
                    onSwitchKey = { currentScreen = Screen.LICENSE_ACTIVATION },
                    onBack = { currentScreen = Screen.SETTINGS }
                )
            }

            Screen.ABOUT_APP -> {
                AboutAppScreen(onBack = { currentScreen = Screen.SETTINGS })
            }
        }

        // Floating AI Orb Multi-tasking Overlay
        FloatingOrbOverlay(
            isVisible = isFloatingOrbOverlayVisible,
            onClose = { isFloatingOrbOverlayVisible = false },
            onOpenScreenShare = {
                isFloatingOrbOverlayVisible = false
                currentScreen = Screen.SCREEN_SHARING
            },
            onReturnToApp = {
                isFloatingOrbOverlayVisible = false
                currentScreen = Screen.MAIN
            },
            onEndConversation = {
                isFloatingOrbOverlayVisible = false
                voiceManager.stopSpeaking()
                voiceManager.stopListening()
            }
        )
    }
}
