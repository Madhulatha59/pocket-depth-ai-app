package com.periodontal.ai.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.periodontal.ai.ui.theme.*
import com.periodontal.ai.viewmodel.ProbingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    viewModel: ProbingViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val classification = state.classification

    val themeColor = when (classification.category) {
        "Healthy Gums" -> HealthyGreen
        "Gingivitis" -> GingivitisOrange
        else -> PeriodontitisRed
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clinical AI Diagnostic Report", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Share, contentDescription = "Share Report")
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main classification result header
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.5.dp, themeColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(themeColor.copy(alpha = 0.1f))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            classification.category.uppercase(),
                            color = themeColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Text(
                        "Diagnosis: ${classification.category}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        classification.description,
                        fontSize = 14.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }

            // Key clinical metrics row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Average Pocket Depth",
                    value = String.format("%.2f mm", classification.averageDepth),
                    icon = Icons.Default.VerticalAlignBottom,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Bleeding Index (BOP)",
                    value = String.format("%.1f %%", classification.bleedingPercentage),
                    icon = Icons.Default.WaterDrop,
                    tint = PeriodontitisRed,
                    modifier = Modifier.weight(1f)
                )
            }

            // Severe spots card list
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Active Deep Pockets", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(themeColor.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "${classification.severeSitesCount} Sites >= 4mm",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColor
                            )
                        }
                    }
                    Divider()

                    // Gather and list actual deep pockets
                    val deepSites = mutableListOf<String>()
                    state.toothMeasurements.forEach { tooth ->
                        if (tooth.distalFacial >= 4) deepSites.add("Tooth ${tooth.toothNumber} (DF: ${tooth.distalFacial}mm)")
                        if (tooth.facial >= 4) deepSites.add("Tooth ${tooth.toothNumber} (F: ${tooth.facial}mm)")
                        if (tooth.mesialFacial >= 4) deepSites.add("Tooth ${tooth.toothNumber} (MF: ${tooth.mesialFacial}mm)")
                        if (tooth.distalLingual >= 4) deepSites.add("Tooth ${tooth.toothNumber} (DL: ${tooth.distalLingual}mm)")
                        if (tooth.lingual >= 4) deepSites.add("Tooth ${tooth.toothNumber} (L: ${tooth.lingual}mm)")
                        if (tooth.mesialLingual >= 4) deepSites.add("Tooth ${tooth.toothNumber} (ML: ${tooth.mesialLingual}mm)")
                    }

                    if (deepSites.isEmpty()) {
                        Text(
                            "No deep pocket depth values >= 4mm recorded.",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    } else {
                        // Display top 4 deep pockets
                        deepSites.take(4).forEach { siteLog ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = "Pocket warning", tint = PeriodontitisRed, modifier = Modifier.size(16.dp))
                                Text(siteLog, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                        if (deepSites.size > 4) {
                            Text("+ ${deepSites.size - 4} more sites found", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // Recommendations
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("AI Recommended Treatments", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Divider()
                    classification.recommendations.forEach { recommendation ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Check",
                                tint = HealthyGreen,
                                modifier = Modifier
                                    .size(18.dp)
                                    .padding(top = 2.dp)
                            )
                            Text(recommendation, fontSize = 13.sp, lineHeight = 18.sp, color = TextPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = tint, modifier = Modifier.size(24.dp))
            Text(title, fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
    }
}
