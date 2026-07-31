package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ElectricTeal
import com.example.ui.theme.GlowingPurple
import com.example.ui.theme.NeonCyan
import kotlin.math.sin

@Composable
fun AudioWaveformVisualizer(
    isListening: Boolean,
    isSpeaking: Boolean,
    audioRmsLevel: Float,
    modifier: Modifier = Modifier.fillMaxWidth().height(100.dp)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_transition")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    val amplitudeMultiplier = when {
        isListening -> (audioRmsLevel * 45f).coerceIn(15f, 60f)
        isSpeaking -> 35f
        else -> 8f
    }

    val waveGradient = Brush.horizontalGradient(
        colors = listOf(NeonCyan, GlowingPurple, ElectricTeal)
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f

        // Draw primary wave
        val path1 = Path()
        path1.moveTo(0f, centerY)

        val frequency = 0.02f
        for (x in 0..width.toInt() step 5) {
            val y = centerY + sin(x * frequency + phase) * amplitudeMultiplier
            path1.lineTo(x.toFloat(), y)
        }
        drawPath(
            path = path1,
            brush = waveGradient,
            style = Stroke(width = 4f)
        )

        // Draw secondary harmonic wave
        val path2 = Path()
        path2.moveTo(0f, centerY)
        for (x in 0..width.toInt() step 5) {
            val y = centerY + sin(x * (frequency * 1.5f) - phase * 0.8f) * (amplitudeMultiplier * 0.6f)
            path2.lineTo(x.toFloat(), y)
        }
        drawPath(
            path = path2,
            brush = waveGradient,
            style = Stroke(width = 2.5f)
        )
    }
}
