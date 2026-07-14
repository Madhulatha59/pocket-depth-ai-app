package com.periodontal.ai.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.periodontal.ai.MainActivity
import com.periodontal.ai.data.model.ToothMeasurement
import com.periodontal.ai.data.voice.ProbingSite
import com.periodontal.ai.ui.theme.*
import com.periodontal.ai.viewmodel.ProbingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProbingSessionScreen(
    viewModel: ProbingViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToReport: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? MainActivity

    // Beautiful pulse animation for speech recording
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (state.isVoiceListening) 1.2f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Periodontal AI Charting", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = onNavigateToReport,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("AI Report", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // AI Badge Indicator Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Selected: Tooth ${state.selectedToothNumber}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                // Classification Badge
                val badgeColor = when (state.classification.category) {
                    "Healthy Gums" -> HealthyGreen
                    "Gingivitis" -> GingivitisOrange
                    else -> PeriodontitisRed
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeColor.copy(alpha = 0.15f))
                        .border(1.dp, badgeColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        state.classification.category,
                        color = badgeColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            // Tooth Selection Grid (Upper/Lower teeth)
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.weight(1.3f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Tooth Map (1 - 32)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(8),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(32) { index ->
                            val toothNum = index + 1
                            val isSelected = state.selectedToothNumber == toothNum
                            val toothRecord = state.toothMeasurements.find { it.toothNumber == toothNum }
                            val maxPocket = toothRecord?.let {
                                listOf(it.distalFacial, it.facial, it.mesialFacial, it.distalLingual, it.lingual, it.mesialLingual).maxOrNull()
                            } ?: 0

                            val toothColor = when {
                                isSelected -> MaterialTheme.colorScheme.primary
                                maxPocket >= 5 -> PeriodontitisRed.copy(alpha = 0.8f)
                                maxPocket == 4 -> GingivitisOrange.copy(alpha = 0.8f)
                                else -> BorderColor
                            }

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                                    .border(1.dp, toothColor, RoundedCornerShape(6.dp))
                                    .clickable { viewModel.selectTooth(toothNum) }
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        toothNum.toString(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (maxPocket > 0) {
                                        Text(
                                            "${maxPocket}m",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isSelected) Color.White.copy(alpha = 0.8f) else TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Selected Tooth 6 Sites Control Card
            val activeRecord = state.toothMeasurements.find { it.toothNumber == state.selectedToothNumber } ?: ToothMeasurement(toothNumber = state.selectedToothNumber)
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.weight(1.5f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "6-Site Probing Grid (Facial / Lingual)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    // Sites Row (DF, F, MF, DL, L, ML)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple(ProbingSite.DISTAL_FACIAL, "DF", activeRecord.distalFacial),
                            Triple(ProbingSite.FACIAL, "F", activeRecord.facial),
                            Triple(ProbingSite.MESIAL_FACIAL, "MF", activeRecord.mesialFacial),
                            Triple(ProbingSite.DISTAL_LINGUAL, "DL", activeRecord.distalLingual),
                            Triple(ProbingSite.LINGUAL, "L", activeRecord.lingual),
                            Triple(ProbingSite.MESIAL_LINGUAL, "ML", activeRecord.mesialLingual)
                        ).forEach { (site, label, value) ->
                            val isActive = state.selectedSite == site
                            val activeSiteColor = if (isActive) MaterialTheme.colorScheme.primary else BorderColor
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent)
                                    .border(1.dp, activeSiteColor, RoundedCornerShape(8.dp))
                                    .clickable { viewModel.selectSite(site) }
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "${value}mm",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (value >= 5) PeriodontitisRed else if (value == 4) GingivitisOrange else TextPrimary
                                )
                            }
                        }
                    }

                    // Site Bleeding Toggle and Manual Depth Input Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isBopActive = when (state.selectedSite) {
                            ProbingSite.DISTAL_FACIAL -> activeRecord.bleedingDF
                            ProbingSite.FACIAL -> activeRecord.bleedingF
                            ProbingSite.MESIAL_FACIAL -> activeRecord.bleedingMF
                            ProbingSite.DISTAL_LINGUAL -> activeRecord.bleedingDL
                            ProbingSite.LINGUAL -> activeRecord.bleedingL
                            ProbingSite.MESIAL_LINGUAL -> activeRecord.bleedingML
                        }

                        Text("Bleeding on Probing (BOP):", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Switch(
                            checked = isBopActive,
                            onCheckedChange = { viewModel.toggleCurrentSiteBleeding() },
                            colors = SwitchDefaults.colors(checkedThumbColor = PeriodontitisRed, checkedTrackColor = PeriodontitisRed.copy(alpha = 0.4f))
                        )
                    }

                    // Keypad depths
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        (1..6).forEach { value ->
                            Button(
                                onClick = { viewModel.updateCurrentSiteDepth(value) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), contentColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.size(45.dp),
                                contentPadding = PaddingValues(0.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(value.toString(), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }

            // Glowing Speech Recognition Console Panel
            val micContainerColor by animateColorAsState(
                targetValue = if (state.isVoiceListening) PrimaryGradientStart.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                label = "color"
            )
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = micContainerColor),
                border = BorderStroke(1.dp, if (state.isVoiceListening) PrimaryGradientStart else BorderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Micro Pulsing Button
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .scale(pulseScale)
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(PrimaryGradientStart, PrimaryGradientEnd)
                                )
                            )
                            .clickable {
                                if (state.isVoiceListening) {
                                    activity?.stopListening()
                                } else {
                                    activity?.startListening()
                                }
                            }
                    ) {
                        Icon(
                            imageVector = if (state.isVoiceListening) Icons.Default.Mic else Icons.Default.MicOff,
                            contentDescription = "Voice Probing Toggle",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            if (state.isVoiceListening) "AI ENGINE: LISTENING..." else "Voice Probing Console",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (state.isVoiceListening) PrimaryGradientStart else TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            if (state.voiceTranscript.isNotEmpty()) "\"${state.voiceTranscript}\"" else state.voiceFeedbackMessage,
                            fontSize = 13.sp,
                            color = TextSecondary,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}
