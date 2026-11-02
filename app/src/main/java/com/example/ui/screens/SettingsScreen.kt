package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.LicenseStorage
import com.example.ui.components.GlassCard
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.MidnightBlue
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SkyBlueAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    licenseStorage: LicenseStorage,
    onNavigateToDeveloperInfo: () -> Unit,
    onNavigateToLicenseInfo: () -> Unit,
    onNavigateToAboutApp: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var speechRate by remember { mutableFloatStateOf(licenseStorage.speechRate) }
    var speechPitch by remember { mutableFloatStateOf(licenseStorage.speechPitch) }
    var autoVoiceReply by remember { mutableStateOf(licenseStorage.autoVoiceReply) }
    var showVoiceControls by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DeepNavy,
                        MidnightBlue,
                        DeepNavy
                    )
                )
            )
            .testTag("settings_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(GlassBorder.copy(alpha = 0.2f))
                        .testTag("settings_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            // Menu Items Section
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // 1. Voice Settings
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    onClick = { showVoiceControls = !showVoiceControls }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(ElectricCyan.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RecordVoiceOver,
                                        contentDescription = null,
                                        tint = ElectricCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = "Voice Settings",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Speech speed, pitch & speech replies",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = TextMuted
                            )
                        }

                        if (showVoiceControls) {
                            Spacer(modifier = Modifier.height(16.dp))
                            // Speech Rate Slider
                            Text(
                                text = "Speech Speed: ${String.format("%.1fx", speechRate)}",
                                fontSize = 12.sp,
                                color = SkyBlueAccent
                            )
                            Slider(
                                value = speechRate,
                                onValueChange = {
                                    speechRate = it
                                    licenseStorage.speechRate = it
                                },
                                valueRange = 0.6f..1.6f,
                                colors = SliderDefaults.colors(
                                    thumbColor = ElectricCyan,
                                    activeTrackColor = ElectricCyan,
                                    inactiveTrackColor = GlassBorder
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Speech Pitch Slider
                            Text(
                                text = "Voice Pitch: ${String.format("%.1fx", speechPitch)}",
                                fontSize = 12.sp,
                                color = SkyBlueAccent
                            )
                            Slider(
                                value = speechPitch,
                                onValueChange = {
                                    speechPitch = it
                                    licenseStorage.speechPitch = it
                                },
                                valueRange = 0.6f..1.5f,
                                colors = SliderDefaults.colors(
                                    thumbColor = NeonPurple,
                                    activeTrackColor = NeonPurple,
                                    inactiveTrackColor = GlassBorder
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Auto Voice Responses",
                                    fontSize = 13.sp,
                                    color = TextPrimary
                                )
                                Switch(
                                    checked = autoVoiceReply,
                                    onCheckedChange = {
                                        autoVoiceReply = it
                                        licenseStorage.autoVoiceReply = it
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = DeepNavy,
                                        checkedTrackColor = ElectricCyan
                                    )
                                )
                            }
                        }
                    }
                }

                // 2. Developer Information
                SettingsMenuItem(
                    icon = Icons.Default.Person,
                    iconTint = NeonPurple,
                    title = "Developer Information",
                    subtitle = "Ishak Nayeem (Ishak Bhai) • Creator Profile",
                    onClick = onNavigateToDeveloperInfo,
                    testTag = "menu_developer_info"
                )

                // 3. License Information
                SettingsMenuItem(
                    icon = Icons.Default.VpnKey,
                    iconTint = ElectricCyan,
                    title = "License Information",
                    subtitle = "Plan: ${licenseStorage.licenseTier} • Devices & Status",
                    onClick = onNavigateToLicenseInfo,
                    testTag = "menu_license_info"
                )

                // 4. About App
                SettingsMenuItem(
                    icon = Icons.Default.Info,
                    iconTint = SkyBlueAccent,
                    title = "About Application",
                    subtitle = "Version 1.0.0 • TradePilot Voice AI Engine",
                    onClick = onNavigateToAboutApp,
                    testTag = "menu_about_app"
                )
            }
        }
    }
}

@Composable
private fun SettingsMenuItem(
    icon: ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TextMuted
            )
        }
    }
}
