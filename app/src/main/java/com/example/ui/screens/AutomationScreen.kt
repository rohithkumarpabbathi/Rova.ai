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
fun AutomationScreen(viewModel: AssistantViewModel) {
    val rules by viewModel.automationRules.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var ruleTitle by remember { mutableStateOf("") }
    var ruleTrigger by remember { mutableStateOf("") }
    var ruleAction by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Smart Automation",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Trigger actions automatically",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }

            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = NeonCyan,
                contentColor = DarkBackground,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("add_automation_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Rule")
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(rules) { rule ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = rule.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "⚡ WHEN: ${rule.triggerText}",
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "➡️ DO: ${rule.actionText}",
                                fontSize = 13.sp,
                                color = ElectricTeal
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Switch(
                                checked = rule.isEnabled,
                                onCheckedChange = { enabled ->
                                    viewModel.toggleAutomationRule(rule.id, enabled)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = DarkBackground,
                                    checkedTrackColor = NeonCyan
                                ),
                                modifier = Modifier.testTag("rule_switch_${rule.id}")
                            )

                            IconButton(
                                onClick = { viewModel.deleteAutomationRule(rule.id) },
                                modifier = Modifier.size(28.dp).testTag("delete_rule_${rule.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Create Automation Rule", color = NeonCyan, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = ruleTitle,
                        onValueChange = { ruleTitle = it },
                        label = { Text("Rule Title") }
                    )
                    OutlinedTextField(
                        value = ruleTrigger,
                        onValueChange = { ruleTrigger = it },
                        label = { Text("Trigger (e.g. Battery < 15%)") }
                    )
                    OutlinedTextField(
                        value = ruleAction,
                        onValueChange = { ruleAction = it },
                        label = { Text("Action (e.g. Enable Battery Saver)") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (ruleTitle.isNotBlank()) {
                            viewModel.addAutomationRule(ruleTitle, ruleTrigger, ruleAction)
                            ruleTitle = ""
                            ruleTrigger = ""
                            ruleAction = ""
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DarkBackground)
                ) {
                    Text("Save Rule")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }
}
