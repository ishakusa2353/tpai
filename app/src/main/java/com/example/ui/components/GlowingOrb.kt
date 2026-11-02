package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.AssistantState
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SkyBlueAccent

@Composable
fun GlowingOrb(
    state: AssistantState,
    modifier: Modifier = Modifier,
    size: Dp = 220.dp,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "OrbPulse")

    // Breathing pulse scale
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = when (state) {
            AssistantState.LISTENING -> 1.15f
            AssistantState.SPEAKING -> 1.10f
            AssistantState.THINKING -> 1.05f
            AssistantState.IDLE -> 1.02f
        },
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    AssistantState.LISTENING -> 600
                    AssistantState.SPEAKING -> 800
                    AssistantState.THINKING -> 1000
                    AssistantState.IDLE -> 1800
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    // Rotation angle
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    AssistantState.THINKING -> 2000
                    AssistantState.LISTENING -> 4000
                    AssistantState.SPEAKING -> 6000
                    AssistantState.IDLE -> 12000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "OrbRotation"
    )

    // Outer glow alpha
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = when (state) {
            AssistantState.LISTENING -> 0.85f
            AssistantState.SPEAKING -> 0.70f
            AssistantState.THINKING -> 0.60f
            AssistantState.IDLE -> 0.40f
        },
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowAlpha"
    )

    Box(
        modifier = modifier
            .size(size)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = (this.size.minDimension / 2f) * 0.72f * pulseScale

            // 1. Outermost soft ambient glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ElectricCyan.copy(alpha = glowAlpha * 0.45f),
                        NeonPurple.copy(alpha = glowAlpha * 0.25f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 1.55f
                ),
                radius = baseRadius * 1.55f,
                center = center
            )

            // 2. Animated outer orbital ring
            rotate(rotation, pivot = center) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            ElectricCyan,
                            ElectricBlue,
                            NeonPurple,
                            SkyBlueAccent,
                            ElectricCyan
                        ),
                        center = center
                    ),
                    radius = baseRadius * 1.12f,
                    center = center,
                    style = Stroke(width = 3.dp.toPx())
                )
            }

            // 3. Counter-rotating inner energy ring
            rotate(-rotation * 1.4f, pivot = center) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            NeonPurple.copy(alpha = 0.8f),
                            Color.Transparent,
                            ElectricCyan.copy(alpha = 0.9f),
                            Color.Transparent
                        ),
                        center = center
                    ),
                    radius = baseRadius * 1.04f,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // 4. Main 3D Liquid Glass sphere body
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFE0F7FA),
                        ElectricCyan,
                        ElectricBlue,
                        Color(0xFF1E1B4B),
                        Color(0xFF030712)
                    ),
                    center = Offset(center.x - baseRadius * 0.25f, center.y - baseRadius * 0.25f),
                    radius = baseRadius
                ),
                radius = baseRadius,
                center = center
            )

            // 5. Specular highlight for authentic 3D sphere curvature
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.75f),
                        Color.White.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = Offset(center.x - baseRadius * 0.35f, center.y - baseRadius * 0.35f),
                    radius = baseRadius * 0.45f
                ),
                radius = baseRadius * 0.45f,
                center = Offset(center.x - baseRadius * 0.35f, center.y - baseRadius * 0.35f)
            )
        }
    }
}
