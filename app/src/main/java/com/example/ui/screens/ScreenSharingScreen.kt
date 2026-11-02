package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.GeminiService
import com.example.ui.components.GlassCard
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
import kotlinx.coroutines.launch

@Composable
fun ScreenSharingScreen(
    geminiService: GeminiService,
    onSpeak: (String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var aiAnalysisText by remember {
        mutableStateOf("Analyzing screen stream... Bullish structure detected on 15m chart. Price is respecting the ascending trendline with volume expansion.")
    }
    var isAnalyzing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val infiniteTransition = rememberInfiniteTransition(label = "RadarPulse")
    val radarPulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "RadarAlpha"
    )

    fun requestScreenInsight() {
        isAnalyzing = true
        coroutineScope.launch {
            val response = geminiService.generateResponse(
                "Analyze current market chart setup: ascending triangle, EMA 20 bounce, increasing buyer volume."
            )
            aiAnalysisText = response
            isAnalyzing = false
            onSpeak(response)
        }
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
            .testTag("screen_sharing_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(GlassBorder.copy(alpha = 0.2f))
                        .testTag("screen_share_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(CoralRed.copy(alpha = radarPulse))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Screen Sharing Active",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                IconButton(
                    onClick = { requestScreenInsight() },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(ElectricCyan.copy(alpha = 0.2f))
                        .testTag("ai_screen_insight_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Insight",
                        tint = ElectricCyan
                    )
                }
            }

            // Real Interactive Candlestick Chart Simulation
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(20.dp),
                borderColor = ElectricCyan.copy(alpha = 0.35f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "EUR/USD • 15M",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "1.08450 (+0.42%)",
                                fontSize = 12.sp,
                                color = EmeraldSuccess,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(ElectricCyan.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "LIVE AI VISION",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricCyan
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Candlestick Canvas
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        val w = size.width
                        val h = size.height

                        // Draw Grid lines
                        for (i in 1..4) {
                            val y = h * (i / 5f)
                            drawLine(
                                color = GlassBorder.copy(alpha = 0.25f),
                                start = Offset(0f, y),
                                end = Offset(w, y),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        // Simulated Candlesticks (high, low, open, close)
                        val candles = listOf(
                            Triple(0.7f, 0.45f, false), // green
                            Triple(0.6f, 0.5f, false),
                            Triple(0.52f, 0.65f, true), // red
                            Triple(0.62f, 0.40f, false),
                            Triple(0.48f, 0.58f, true),
                            Triple(0.55f, 0.35f, false),
                            Triple(0.38f, 0.30f, false),
                            Triple(0.32f, 0.42f, true),
                            Triple(0.39f, 0.25f, false),
                            Triple(0.28f, 0.20f, false),
                            Triple(0.22f, 0.15f, false)
                        )

                        val candleWidth = (w / (candles.size + 1)) * 0.55f
                        val stepX = w / (candles.size + 1)

                        candles.forEachIndexed { index, (openRatio, closeRatio, isRed) ->
                            val cx = stepX * (index + 1)
                            val topY = h * minOf(openRatio, closeRatio)
                            val botY = h * maxOf(openRatio, closeRatio)
                            val wickTop = topY - h * 0.06f
                            val wickBot = botY + h * 0.06f
                            val color = if (isRed) CoralRed else EmeraldSuccess

                            // Wick
                            drawLine(
                                color = color,
                                start = Offset(cx, wickTop),
                                end = Offset(cx, wickBot),
                                strokeWidth = 1.5.dp.toPx()
                            )

                            // Body
                            drawRect(
                                color = color,
                                topLeft = Offset(cx - candleWidth / 2f, topY),
                                size = Size(candleWidth, (botY - topY).coerceAtLeast(4f))
                            )
                        }

                        // Moving average line (EMA)
                        val emaPath = Path().apply {
                            moveTo(stepX, h * 0.65f)
                            cubicTo(
                                w * 0.3f, h * 0.58f,
                                w * 0.6f, h * 0.38f,
                                w, h * 0.20f
                            )
                        }
                        drawPath(
                            path = emaPath,
                            color = ElectricCyan,
                            style = Stroke(width = 2.5.dp.toPx())
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // AI Commentary Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DeepNavy.copy(alpha = 0.6f))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "AI Screen Analysis",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricCyan
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = aiAnalysisText,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // Bottom Stop Sharing Button
            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(50.dp)
                    .testTag("stop_screen_sharing_button"),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CoralRed,
                    contentColor = TextPrimary
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop",
                        tint = TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Stop Sharing",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
