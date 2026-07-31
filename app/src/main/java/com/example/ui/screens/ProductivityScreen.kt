package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AssistantViewModel
import com.example.ui.components.GlassCard
import com.example.ui.theme.*

@Composable
fun ProductivityScreen(viewModel: AssistantViewModel) {
    val reminders by viewModel.reminders.collectAsState()
    val notes by viewModel.notes.collectAsState()

    var activeSubTab by remember { mutableStateOf("Reminders") }
    var showAddReminderDialog by remember { mutableStateOf(false) }
    var showAddNoteDialog by remember { mutableStateOf(false) }

    var reminderTitle by remember { mutableStateOf("") }
    var reminderTime by remember { mutableStateOf("") }

    var noteTitle by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Productivity Hub",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            IconButton(
                onClick = {
                    if (activeSubTab == "Reminders") showAddReminderDialog = true
                    else showAddNoteDialog = true
                },
                modifier = Modifier.testTag("productivity_add_button")
            ) {
                Icon(imageVector = Icons.Default.AddCircle, contentDescription = "Add", tint = NeonCyan)
            }
        }

        // Sub Tabs
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf("Reminders", "Voice Notes", "Daily Briefing").forEach { tab ->
                val selected = activeSubTab == tab
                Button(
                    onClick = { activeSubTab = tab },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selected) NeonCyan else GlassCardBg,
                        contentColor = if (selected) DarkBackground else TextPrimary
                    ),
                    modifier = Modifier.weight(1f).testTag("subtab_$tab")
                ) {
                    Text(text = tab.split(" ")[0], fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        when (activeSubTab) {
            "Reminders" -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(reminders) { rem ->
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = rem.isCompleted,
                                        onCheckedChange = { chk -> viewModel.toggleReminder(rem.id, chk) },
                                        colors = CheckboxDefaults.colors(checkedColor = NeonCyan)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = rem.title,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "🕒 ${rem.dueTimeFormatted}",
                                            fontSize = 12.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { viewModel.deleteReminder(rem.id) },
                                    modifier = Modifier.testTag("delete_reminder_${rem.id}")
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = TextSecondary)
                                }
                            }
                        }
                    }
                }
            }

            "Voice Notes" -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(notes) { note ->
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = note.title,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NeonCyan
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = note.content,
                                        fontSize = 14.sp,
                                        color = TextPrimary
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.deleteNote(note.id) },
                                    modifier = Modifier.testTag("delete_note_${note.id}")
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = TextSecondary)
                                }
                            }
                        }
                    }
                }
            }

            else -> { // Daily Briefing
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "☀️ Weather Today", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                        Text(text = "Sunny • 26°C • High 28° / Low 19°", fontSize = 14.sp, color = TextPrimary)
                    }

                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "📅 Today's Agenda", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GlowingPurple)
                        Text(text = "• 10:30 AM: Team Sync Meeting\n• 03:00 PM: Project Review Call", fontSize = 14.sp, color = TextPrimary)
                    }

                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "💡 Daily Motivation", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ElectricTeal)
                        Text(text = "\"Efficiency is doing things right; effectiveness is doing the right things.\"", fontSize = 14.sp, color = TextPrimary)
                    }
                }
            }
        }
    }

    if (showAddReminderDialog) {
        AlertDialog(
            onDismissRequest = { showAddReminderDialog = false },
            title = { Text("Add Reminder", color = NeonCyan) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = reminderTitle, onValueChange = { reminderTitle = it }, label = { Text("Title") })
                    OutlinedTextField(value = reminderTime, onValueChange = { reminderTime = it }, label = { Text("Due Time (e.g. Today 5:00 PM)") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (reminderTitle.isNotBlank()) {
                            viewModel.addReminder(reminderTitle, if (reminderTime.isBlank()) "Today" else reminderTime)
                            reminderTitle = ""
                            reminderTime = ""
                            showAddReminderDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DarkBackground)
                ) {
                    Text("Save")
                }
            },
            containerColor = DarkSurface
        )
    }

    if (showAddNoteDialog) {
        AlertDialog(
            onDismissRequest = { showAddNoteDialog = false },
            title = { Text("Add Voice Note", color = NeonCyan) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = noteTitle, onValueChange = { noteTitle = it }, label = { Text("Title") })
                    OutlinedTextField(value = noteContent, onValueChange = { noteContent = it }, label = { Text("Content") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noteTitle.isNotBlank()) {
                            viewModel.addNote(noteTitle, noteContent)
                            noteTitle = ""
                            noteContent = ""
                            showAddNoteDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DarkBackground)
                ) {
                    Text("Save")
                }
            },
            containerColor = DarkSurface
        )
    }
}
