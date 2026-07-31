package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AssistantViewModel
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassPill
import com.example.ui.components.GlowingMicButton
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.ElectricTeal
import com.example.ui.theme.GlowingPurple
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun VoiceAssistantScreen(
    viewModel: AssistantViewModel,
    onNavigateToSettings: () -> Unit
) {
    val isListening by viewModel.isListening.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val audioRmsLevel by viewModel.audioRmsLevel.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val assistantName by viewModel.assistantName.collectAsState()

    val lastMessage = messages.lastOrNull()
    val scrollState = rememberScrollState()

    val quickCommands = listOf(
        "Open Camera",
        "Search YouTube for Lofi Beats",
        "Turn Flashlight On",
        "Set Alarm for 7:00 AM",
        "Navigate to City Center",
        "Emergency SOS"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Hey $assistantName",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
                Text(
                    text = "Your AI Voice Companion",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }

            GlassPill(
                modifier = Modifier
                    .clickable { onNavigateToSettings() }
                    .testTag("settings_pill_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = NeonCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Settings", color = TextPrimary, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Center Assistant Status Display
        GlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (isListening || isSpeaking) NeonCyan else ElectricTeal)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when {
                        isListening -> "Listening for voice..."
                        isSpeaking -> "Speaking response..."
                        isProcessing -> "Processing with AI..."
                        else -> "Ready • Say 'Hey $assistantName'"
                    },
                    fontSize = 13.sp,
                    color = NeonCyan,
                    fontWeight = FontWeight.Medium
                )
            }

            if (lastMessage != null) {
                Text(
                    text = if (lastMessage.sender == "user") "You asked: \"${lastMessage.text}\""
                           else lastMessage.text,
                    fontSize = 16.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 22.sp
                )
            } else {
                Text(
                    text = "Tap the glowing mic or say 'Hey Rova.ai' to start.",
                    fontSize = 15.sp,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Animated Sine Wave Audio Visualizer
        AudioWaveformVisualizer(
            isListening = isListening,
            isSpeaking = isSpeaking,
            audioRmsLevel = audioRmsLevel
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Central Glowing Microphone Button
        GlowingMicButton(
            isListening = isListening,
            isSpeaking = isSpeaking,
            isProcessing = isProcessing,
            onClick = {
                if (isListening) {
                    viewModel.stopListening()
                } else if (isSpeaking) {
                    viewModel.stopSpeaking()
                } else {
                    viewModel.startListening()
                }
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Quick Command Chips
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Quick Voice Actions",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(quickCommands) { cmd ->
                    GlassPill(
                        modifier = Modifier
                            .clickable { viewModel.sendTextQuery(cmd) }
                            .testTag("quick_command_$cmd")
                    ) {
                        Icon(
                            imageVector = when {
                                cmd.contains("Camera") -> Icons.Default.CameraAlt
                                cmd.contains("YouTube") -> Icons.Default.PlayArrow
                                cmd.contains("Flashlight") -> Icons.Default.FlashOn
                                cmd.contains("Alarm") -> Icons.Default.Alarm
                                cmd.contains("Navigate") -> Icons.Default.Navigation
                                else -> Icons.Default.Warning
                            },
                            contentDescription = cmd,
                            tint = NeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = cmd, fontSize = 13.sp, color = TextPrimary)
                    }
                }
            }
        }
    }
}
