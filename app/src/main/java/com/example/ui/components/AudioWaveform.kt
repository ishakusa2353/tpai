package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonPurple
import kotlin.math.sin

@Composable
fun AudioWaveform(
    modifier: Modifier = Modifier,
    height: Dp = 60.dp,
    barCount: Int = 28,
    isAnimating: Boolean = true,
    intensity: Float = 1.0f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "Waveform")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WavePhase"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val totalSpacing = canvasWidth / barCount
        val barWidth = totalSpacing * 0.55f

        for (i in 0 until barCount) {
            val progress = i.toFloat() / barCount
            val wave = if (isAnimating) {
                val wave1 = sin(progress * 6.0 + phase).toFloat()
                val wave2 = sin(progress * 12.0 - phase * 1.5).toFloat()
                val combined = (wave1 * 0.6f + wave2 * 0.4f).coerceIn(-1f, 1f)
                (0.2f + (combined + 1f) * 0.4f * intensity.coerceIn(0.5f, 2.5f)).coerceIn(0.12f, 1.0f)
            } else {
                0.15f
            }

            val currentBarHeight = canvasHeight * wave
            val startX = i * totalSpacing + (totalSpacing - barWidth) / 2f
            val startY = (canvasHeight - currentBarHeight) / 2f

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        ElectricCyan,
                        ElectricBlue,
                        NeonPurple
                    )
                ),
                topLeft = Offset(startX, startY),
                size = Size(barWidth, currentBarHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}
