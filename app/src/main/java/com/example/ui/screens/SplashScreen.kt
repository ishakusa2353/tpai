package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.LicenseStorage
import com.example.data.model.AssistantState
import com.example.data.remote.SupabaseClient
import com.example.ui.components.GlowingOrb
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MidnightBlue
import com.example.ui.theme.SkyBlueAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    licenseStorage: LicenseStorage,
    supabaseClient: SupabaseClient,
    onNavigateToMain: () -> Unit,
    onNavigateToActivation: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "SplashGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowAlpha"
    )

    LaunchedEffect(Unit) {
        delay(1800) // Smooth entrance delay
        val savedKey = licenseStorage.savedLicenseKey
        if (!savedKey.isNullOrBlank()) {
            // Validate saved license in background
            try {
                val result = supabaseClient.verifyAndActivateLicense(
                    keyInput = savedKey,
                    currentDeviceId = licenseStorage.deviceId
                )
                if (result is com.example.data.model.ActivationResult.Success) {
                    onNavigateToMain()
                    return@LaunchedEffect
                }
            } catch (_: Exception) {
                // If offline or network glitch but local license is not expired, allow access
                val expiry = licenseStorage.licenseExpiry
                if (expiry == -1L || expiry > System.currentTimeMillis()) {
                    onNavigateToMain()
                    return@LaunchedEffect
                }
            }
        }
        onNavigateToActivation()
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
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // TradePilot 3D Logo
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                ElectricCyan.copy(alpha = glowAlpha * 0.4f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.tradepilot_logo),
                    contentDescription = "TradePilot Logo",
                    modifier = Modifier.size(110.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "TradePilot",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                letterSpacing = 1.sp
            )

            Text(
                text = "Voice AI Assistant",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = ElectricCyan,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Trade Smarter with AI",
                fontSize = 14.sp,
                color = SkyBlueAccent.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Animated Floating AI Orb
            GlowingOrb(
                state = AssistantState.IDLE,
                size = 130.dp
            )

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Loading system...",
                fontSize = 13.sp,
                color = TextMuted,
                letterSpacing = 0.5.sp
            )
        }
    }
}
