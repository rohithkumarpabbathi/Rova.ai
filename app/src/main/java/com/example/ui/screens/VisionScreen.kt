package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AssistantViewModel
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassPill
import com.example.ui.theme.*

@Composable
fun VisionScreen(viewModel: AssistantViewModel) {
    var visionMode by remember { mutableStateOf("Object Identifier") }
    var scanResult by remember { mutableStateOf<String?>(null) }
    var isScanning by remember { mutableStateOf(false) }

    val isProcessing by viewModel.isProcessing.collectAsState()
    val scrollState = rememberScrollState()

    val visionModes = listOf(
        "Object Identifier",
        "OCR Text Reader",
        "Math Solver",
        "QR Code Scanner",
        "Screen Summarizer"
    )

    // Laser scan animation transition
    val infiniteTransition = rememberInfiniteTransition(label = "laser_scan")
    val laserYRatio by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "AI Vision & Multimodal",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Analyze objects, solve math & read text with live AI camera HUD",
            fontSize = 13.sp,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Camera Preview / Animated Laser AI HUD Frame
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(GlassCardBg)
                .border(1.5.dp, if (isScanning || isProcessing) GlowingPurple else NeonCyan, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Live scanning laser overlay
            if (isScanning || isProcessing) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val laserY = size.height * laserYRatio
                    drawLine(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, NeonCyan, GlowingPurple, NeonCyan, Color.Transparent)
                        ),
                        start = Offset(0f, laserY),
                        end = Offset(size.width, laserY),
                        strokeWidth = 6f
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = when (visionMode) {
                        "OCR Text Reader" -> Icons.Default.DocumentScanner
                        "Math Solver" -> Icons.Default.Calculate
                        "QR Code Scanner" -> Icons.Default.QrCodeScanner
                        "Screen Summarizer" -> Icons.Default.ScreenSearchDesktop
                        else -> Icons.Default.Camera
                    },
                    contentDescription = "Camera Stream",
                    tint = if (isScanning || isProcessing) GlowingPurple else NeonCyan,
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (isScanning || isProcessing) "AI Analyzing Optical Feed..." else "AI Scanner Ready • $visionMode",
                    fontSize = 14.sp,
                    color = if (isScanning || isProcessing) GlowingPurple else NeonCyan,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // All Vision Modes Horizontal Selector
        Text(
            text = "Select Vision Mode",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(visionModes) { mode ->
                val isSelected = visionMode == mode
                GlassPill(
                    backgroundColor = if (isSelected) NeonCyan.copy(alpha = 0.25f) else Color(0x11FFFFFF),
                    borderColor = if (isSelected) NeonCyan else GlassBorder,
                    modifier = Modifier
                        .clickable {
                            visionMode = mode
                        }
                        .testTag("vision_mode_$mode")
                ) {
                    Text(
                        text = mode,
                        fontSize = 12.sp,
                        color = if (isSelected) NeonCyan else TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Trigger Scan Button
        Button(
            onClick = {
                isScanning = true
                viewModel.runVisionAnalysis(visionMode) { result ->
                    scanResult = result
                    isScanning = false
                }
            },
            enabled = !isScanning && !isProcessing,
            colors = ButtonDefaults.buttonColors(
                containerColor = NeonCyan,
                contentColor = DarkBackground,
                disabledContainerColor = DarkSurface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("run_vision_scan_button")
        ) {
            if (isScanning || isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = NeonCyan,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Analyzing Image Stream...", fontWeight = FontWeight.Bold)
            } else {
                Icon(imageVector = Icons.Default.CenterFocusWeak, contentDescription = "Scan")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Analyze View ($visionMode)", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Display Scan Result
        if (scanResult != null) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Vision Analysis Output",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )

                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = ElectricTeal,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = scanResult!!,
                    fontSize = 14.sp,
                    color = TextPrimary,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassPill(
                        modifier = Modifier.clickable {
                            viewModel.sendTextQuery("Explain more about this: $scanResult")
                        }
                    ) {
                        Icon(imageVector = Icons.Default.QuestionAnswer, contentDescription = "Ask", tint = NeonCyan, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Ask Followup", fontSize = 11.sp, color = TextPrimary)
                    }
                }
            }
        }
    }
}
