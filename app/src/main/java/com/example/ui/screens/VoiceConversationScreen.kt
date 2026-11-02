package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssistantState
import com.example.data.model.ChatMessage
import com.example.ui.components.AudioWaveform
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowingOrb
import com.example.ui.theme.CoralRed
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.MidnightBlue
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SkyBlueAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun VoiceConversationScreen(
    state: AssistantState,
    recognizedText: String,
    messages: List<ChatMessage>,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onEndSession: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler {
        onEndSession()
        onBack()
    }

    LaunchedEffect(Unit) {
        if (state == AssistantState.IDLE) {
            onStartListening()
        }
    }

    val statusText = when (state) {
        AssistantState.LISTENING -> "Listening..."
        AssistantState.THINKING -> "Thinking..."
        AssistantState.SPEAKING -> "Speaking..."
        AssistantState.IDLE -> "Tap Mic to Talk"
    }

    val statusColor = when (state) {
        AssistantState.LISTENING -> ElectricCyan
        AssistantState.THINKING -> NeonPurple
        AssistantState.SPEAKING -> EmeraldSuccess
        AssistantState.IDLE -> TextSecondary
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DeepNavy,
                        MidnightBlue,
                        DeepNavy
                    )
                )
            )
            .testTag("voice_conversation_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Navigation & Status Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        onEndSession()
                        onBack()
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(GlassBorder.copy(alpha = 0.2f))
                        .testTag("voice_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }

                Text(
                    text = statusText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (state) {
                            AssistantState.SPEAKING -> Icons.Default.VolumeUp
                            AssistantState.LISTENING -> Icons.Default.Mic
                            else -> Icons.Default.MicOff
                        },
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Center Dynamic Sphere and Audio Waveform
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                GlowingOrb(
                    state = state,
                    size = 180.dp,
                    onClick = {
                        if (state == AssistantState.LISTENING) {
                            onStopListening()
                        } else {
                            onStartListening()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                AudioWaveform(
                    isAnimating = state == AssistantState.LISTENING || state == AssistantState.SPEAKING,
                    intensity = if (state == AssistantState.SPEAKING) 1.5f else 1.0f,
                    height = 50.dp,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Transcript Card / Live Recognized Text
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp)
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.Center
                    ) {
                        val latestMsg = messages.lastOrNull()
                        if (state == AssistantState.LISTENING && recognizedText.isNotBlank()) {
                            Text(
                                text = "\"$recognizedText\"",
                                fontSize = 14.sp,
                                color = ElectricCyan,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else if (latestMsg != null) {
                            Text(
                                text = if (latestMsg.isUser) "You: ${latestMsg.text}" else "TradePilot: ${latestMsg.text}",
                                fontSize = 13.sp,
                                color = if (latestMsg.isUser) SkyBlueAccent else TextPrimary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Text(
                                text = "Speak naturally. TradePilot AI is listening...",
                                fontSize = 13.sp,
                                color = TextMuted,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Bottom Control Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Status pill chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    listOf("Listening...", "Thinking...", "Speaking...").forEach { chip ->
                        val isCurrent = chip.startsWith(statusText.take(5))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isCurrent) ElectricCyan.copy(alpha = 0.25f)
                                    else GlassBorder.copy(alpha = 0.15f)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = chip,
                                fontSize = 11.sp,
                                color = if (isCurrent) ElectricCyan else TextMuted,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                // Red "End" Button
                Button(
                    onClick = {
                        onEndSession()
                        onBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(50.dp)
                        .testTag("end_voice_conversation_button"),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CoralRed,
                        contentColor = TextPrimary
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "End",
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "End Conversation",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
