package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.LicenseStorage
import com.example.ui.components.GlassCard
import com.example.ui.theme.CoralRed
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.MidnightBlue
import com.example.ui.theme.SkyBlueAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LicenseInfoScreen(
    licenseStorage: LicenseStorage,
    onSwitchKey: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val key = licenseStorage.savedLicenseKey ?: "N/A"
    val tier = licenseStorage.licenseTier
    val expiryMs = licenseStorage.licenseExpiry
    val duration = licenseStorage.licenseDuration
    val deviceId = licenseStorage.deviceId

    val isExpired = expiryMs != -1L && expiryMs < System.currentTimeMillis()
    val expiryText = if (expiryMs == -1L || duration.equals("lifetime", ignoreCase = true)) {
        "Lifetime Validity"
    } else {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        sdf.format(Date(expiryMs))
    }

    val remainingDaysText = if (expiryMs == -1L || duration.equals("lifetime", ignoreCase = true)) {
        "Unlimited"
    } else {
        val diffMs = expiryMs - System.currentTimeMillis()
        val days = (diffMs / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
        "$days Days remaining"
    }

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
            .testTag("license_info_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
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
                        .testTag("license_info_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "License Information",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            // Status Indicator Circle
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(
                        if (isExpired) CoralRed.copy(alpha = 0.15f)
                        else EmeraldSuccess.copy(alpha = 0.15f)
                    )
                    .border(
                        2.5.dp,
                        if (isExpired) CoralRed else EmeraldSuccess,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isExpired) Icons.Default.Info else Icons.Default.Check,
                    contentDescription = null,
                    tint = if (isExpired) CoralRed else EmeraldSuccess,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isExpired) "License Expired" else "License Active",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (isExpired) CoralRed else EmeraldSuccess
            )

            Spacer(modifier = Modifier.height(24.dp))

            // License Details Glass Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    DetailRow(title = "License Key", value = key)
                    DetailRow(title = "Plan Tier", value = tier)
                    DetailRow(title = "Duration", value = duration)
                    DetailRow(title = "Expiry Date", value = expiryText)
                    DetailRow(title = "Remaining Time", value = remainingDaysText)
                    DetailRow(title = "Device Limit", value = "1 / 1 Device")
                    DetailRow(title = "Current Device", value = deviceId)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action Buttons
            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(50.dp)
                    .testTag("renew_extend_button"),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricCyan,
                    contentColor = DeepNavy
                )
            ) {
                Text(
                    text = "Renew / Extend License",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = {
                    licenseStorage.clearLicense()
                    onSwitchKey()
                },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(50.dp)
                    .testTag("switch_license_key_button"),
                shape = RoundedCornerShape(25.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CoralRed)
            ) {
                Text(
                    text = "Deactivate / Switch Key",
                    fontSize = 14.sp,
                    color = CoralRed,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun DetailRow(title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            color = TextSecondary
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}
