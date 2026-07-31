package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AssistantViewModel
import com.example.ui.components.GlassCard
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    viewModel: AssistantViewModel,
    onBack: () -> Unit
) {
    val wakeWordEnabled by viewModel.wakeWordEnabled.collectAsState()
    val voiceStyle by viewModel.voiceStyle.collectAsState()
    val speechSpeed by viewModel.speechSpeed.collectAsState()
    val preferredLanguage by viewModel.preferredLanguage.collectAsState()
    val assistantName by viewModel.assistantName.collectAsState()

    val languages = listOf("English", "Telugu", "Hindi", "Tamil")
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("settings_back_button")
            ) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = NeonCyan)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Assistant Settings",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        // Assistant Identity Card
        GlassCard(modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)) {
            Text(text = "Assistant Identity", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = assistantName,
                onValueChange = { viewModel.assistantName.value = it },
                label = { Text("Assistant Call Name") },
                modifier = Modifier.fillMaxWidth().testTag("assistant_name_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
        }

        // Voice & Wake Word Settings
        GlassCard(modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)) {
            Text(text = "Voice & Activation", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GlowingPurple)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Wake Word ('Hey $assistantName')", fontSize = 15.sp, color = TextPrimary)
                    Text(text = "Automatically start listening on wake phrase", fontSize = 12.sp, color = TextSecondary)
                }
                Switch(
                    checked = wakeWordEnabled,
                    onCheckedChange = { viewModel.wakeWordEnabled.value = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = DarkBackground, checkedTrackColor = NeonCyan),
                    modifier = Modifier.testTag("wake_word_switch")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(text = "Voice Type & Timbre", fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
            val voiceStyles = listOf("Spruce Male", "Deep Baritone", "Studio Warm", "Soft Melody", "Spruce")
            
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                voiceStyles.chunked(2).forEach { rowStyles ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowStyles.forEach { style ->
                            val selected = voiceStyle == style
                            Button(
                                onClick = { viewModel.voiceStyle.value = style },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selected) NeonCyan else GlassCardBg,
                                    contentColor = if (selected) DarkBackground else TextPrimary
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("voice_style_$style")
                            ) {
                                Text(
                                    text = if (style == "Spruce Male") "🌲 $style (Default)" else style,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        if (rowStyles.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(text = "Speech Rate: ${String.format("%.1f", speechSpeed)}x", fontSize = 14.sp, color = TextPrimary)
            Slider(
                value = speechSpeed,
                onValueChange = { viewModel.speechSpeed.value = it },
                valueRange = 0.7f..1.5f,
                colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan),
                modifier = Modifier.testTag("speech_speed_slider")
            )
        }

        // Language Options
        GlassCard(modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)) {
            Text(text = "Preferred Speech Language", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ElectricTeal)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                languages.forEach { lang ->
                    val selected = preferredLanguage == lang
                    Button(
                        onClick = { viewModel.preferredLanguage.value = lang },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selected) ElectricTeal else GlassCardBg,
                            contentColor = if (selected) DarkBackground else TextPrimary
                        ),
                        modifier = Modifier.weight(1f).testTag("language_button_$lang")
                    ) {
                        Text(lang, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Security & Privacy
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Security & Encrypted Memory", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SoftPink)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "All chat logs, memory, and automation routines are stored locally with AES-256 encryption.", fontSize = 13.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { viewModel.clearChatHistory() },
                colors = ButtonDefaults.buttonColors(containerColor = SoftPink, contentColor = Color.White),
                modifier = Modifier.fillMaxWidth().testTag("clear_all_memory_button")
            ) {
                Icon(imageVector = Icons.Default.Security, contentDescription = "Security")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Wipe Conversation History")
            }
        }
    }
}
