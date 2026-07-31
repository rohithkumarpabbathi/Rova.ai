package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AssistantViewModel
import com.example.ui.screens.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {

    private val viewModel: AssistantViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            HeyBuddyTheme {
                val activeTab by viewModel.activeTab.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (activeTab != "Settings") {
                            GlassBottomBar(
                                activeTab = activeTab,
                                onTabSelected = { tab -> viewModel.activeTab.value = tab }
                            )
                        }
                    },
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(DarkBackground)
                    ) {
                        when (activeTab) {
                            "Voice" -> VoiceAssistantScreen(
                                viewModel = viewModel,
                                onNavigateToSettings = { viewModel.activeTab.value = "Settings" }
                            )
                            "Chat" -> ChatScreen(viewModel = viewModel)
                            "Automation" -> AutomationScreen(viewModel = viewModel)
                            "Vision" -> VisionScreen(viewModel = viewModel)
                            "Productivity" -> ProductivityScreen(viewModel = viewModel)
                            "Settings" -> SettingsScreen(
                                viewModel = viewModel,
                                onBack = { viewModel.activeTab.value = "Voice" }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GlassBottomBar(
    activeTab: String,
    onTabSelected: (String) -> Unit
) {
    val navItems = listOf(
        NavItem("Voice", Icons.Default.GraphicEq),
        NavItem("Chat", Icons.Default.ChatBubbleOutline),
        NavItem("Automation", Icons.Default.AutoFixHigh),
        NavItem("Vision", Icons.Default.RemoveRedEye),
        NavItem("Productivity", Icons.Default.TaskAlt)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(30.dp))
                .background(Color(0xEE0B0E17))
                .border(1.dp, GlassBorder, RoundedCornerShape(30.dp))
                .padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { item ->
                val isSelected = activeTab == item.route
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onTabSelected(item.route) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("nav_tab_${item.route}")
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.route,
                        tint = if (isSelected) NeonCyan else TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.route,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) NeonCyan else TextSecondary
                    )
                }
            }
        }
    }
}

data class NavItem(val route: String, val icon: ImageVector)
