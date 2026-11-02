package com.example.data.remote

import com.example.data.model.ActivationResult
import com.example.data.model.LicenseRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class SupabaseClient {
    companion object {
        private const val SUPABASE_URL = "https://qbazzarqiplrqqfytajz.supabase.co"
        private const val SUPABASE_ANON_KEY =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InFiYXp6YXJxaXBscnFxZnl0YWp6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODg3NDc4NDUsImV4cCI6MjEwNDMyMzg0NX0.7BPbYW6P50Nh3OrkQU_T1GOwib-iKNUhLFoc1GxiNZo"
        private const val TABLE_NAME = "ishak_licenses"
    }

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun verifyAndActivateLicense(
        keyInput: String,
        currentDeviceId: String
    ): ActivationResult = withContext(Dispatchers.IO) {
        val trimmedKey = keyInput.trim()
        if (trimmedKey.isEmpty()) {
            return@withContext ActivationResult.Error("Please enter a valid license key.")
        }

        try {
            // 1. Query Supabase for the license key
            val url = "$SUPABASE_URL/rest/v1/$TABLE_NAME?key=eq.$trimmedKey&select=*"
            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer $SUPABASE_ANON_KEY")
                .addHeader("Accept", "application/json")
                .get()
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext ActivationResult.Error(
                    "Network error connecting to verification server (HTTP ${response.code})."
                )
            }

            val bodyString = response.body?.string().orEmpty()
            val jsonArray = JSONArray(bodyString)
            if (jsonArray.length() == 0) {
                return@withContext ActivationResult.Error(
                    "Invalid License Key. No matching license found in system."
                )
            }

            val licenseJson = jsonArray.getJSONObject(0)
            val key = licenseJson.optString("key")
            val active = licenseJson.optBoolean("active", false)
            val tier = licenseJson.optString("tier", "VIP")
            val duration = licenseJson.optString("duration", "30d")
            val durationMs = if (licenseJson.has("duration_ms") && !licenseJson.isNull("duration_ms")) {
                licenseJson.optLong("duration_ms", 2592000000L)
            } else {
                2592000000L
            }
            val exp = if (licenseJson.has("exp") && !licenseJson.isNull("exp")) {
                licenseJson.optLong("exp")
            } else {
                null
            }
            val firstLoginAt = if (licenseJson.has("first_login_at") && !licenseJson.isNull("first_login_at")) {
                licenseJson.optLong("first_login_at")
            } else {
                null
            }
            val registeredDevicesStr = licenseJson.optString("device_id", "")
            val traderId = licenseJson.optString("trader_id", "")
            val createdAt = if (licenseJson.has("created_at") && !licenseJson.isNull("created_at")) {
                licenseJson.optLong("created_at")
            } else {
                null
            }
            val lastUsedAt = if (licenseJson.has("last_used_at") && !licenseJson.isNull("last_used_at")) {
                licenseJson.optLong("last_used_at")
            } else {
                null
            }
            val note = licenseJson.optString("note", "")

            // 2. Validate Active Status
            if (!active) {
                return@withContext ActivationResult.Error(
                    "This license is currently inactive or blocked by admin. Please contact support."
                )
            }

            // 3. Validate Expiration
            val currentTime = System.currentTimeMillis()
            var calculatedExp = exp
            var newFirstLoginAt = firstLoginAt

            if (exp != null && exp > 0 && currentTime > exp) {
                return@withContext ActivationResult.Error(
                    "This license expired on ${formatDate(exp)}. Please contact Ishak Bhai for renewal."
                )
            }

            // If first activation, start expiration calculation based on duration_ms
            if (newFirstLoginAt == null || newFirstLoginAt == 0L) {
                newFirstLoginAt = currentTime
                if (!duration.equals("lifetime", ignoreCase = true)) {
                    calculatedExp = currentTime + durationMs
                }
            }

            // 4. Validate Device Limit
            val deviceLimit = extractDeviceLimit(note)
            val deviceList = parseDeviceList(registeredDevicesStr)
            val isCurrentDeviceRegistered = deviceList.contains(currentDeviceId)

            var updatedDeviceString = registeredDevicesStr
            if (!isCurrentDeviceRegistered) {
                if (deviceList.size >= deviceLimit) {
                    return@withContext ActivationResult.Error(
                        "Device limit exceeded! This license is restricted to $deviceLimit device(s). Registered: ${deviceList.size}."
                    )
                }
                // Append current device
                updatedDeviceString = if (registeredDevicesStr.isBlank()) {
                    currentDeviceId
                } else {
                    "$registeredDevicesStr,$currentDeviceId"
                }
            }

            // 5. Update Supabase record asynchronously (first_login_at, exp, device_id, last_used_at)
            patchLicenseUpdate(
                key = trimmedKey,
                firstLoginAt = newFirstLoginAt,
                exp = calculatedExp,
                deviceId = updatedDeviceString,
                lastUsedAt = currentTime
            )

            val record = LicenseRecord(
                key = key,
                active = true,
                tier = tier,
                duration = duration,
                duration_ms = durationMs,
                exp = calculatedExp,
                first_login_at = newFirstLoginAt,
                device_id = updatedDeviceString,
                trader_id = traderId,
                created_at = createdAt,
                last_used_at = currentTime,
                note = note
            )

            return@withContext ActivationResult.Success(record)
        } catch (e: Exception) {
            return@withContext ActivationResult.Error(
                "Verification failed: ${e.localizedMessage ?: "Unknown network exception"}"
            )
        }
    }

    private fun patchLicenseUpdate(
        key: String,
        firstLoginAt: Long?,
        exp: Long?,
        deviceId: String,
        lastUsedAt: Long
    ) {
        try {
            val patchJson = JSONObject().apply {
                if (firstLoginAt != null) put("first_login_at", firstLoginAt)
                if (exp != null) put("exp", exp)
                put("device_id", deviceId)
                put("last_used_at", lastUsedAt)
            }

            val patchBody = patchJson.toString().toRequestBody("application/json".toMediaType())
            val patchUrl = "$SUPABASE_URL/rest/v1/$TABLE_NAME?key=eq.$key"
            val patchRequest = Request.Builder()
                .url(patchUrl)
                .addHeader("apikey", SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer $SUPABASE_ANON_KEY")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .patch(patchBody)
                .build()

            val patchResp = httpClient.newCall(patchRequest).execute()
            patchResp.close()
        } catch (_: Exception) {
            // Silently continue if patch fails due to RLS, since key was verified active
        }
    }

    private fun extractDeviceLimit(note: String): Int {
        val regex = Regex("""\[DEV_LIMIT:(\d+)\]""")
        val match = regex.find(note)
        return match?.groupValues?.get(1)?.toIntOrNull() ?: 1
    }

    private fun parseDeviceList(deviceStr: String): List<String> {
        if (deviceStr.isBlank()) return emptyList()
        return deviceStr.split(",", ";", " ")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }

    private fun formatDate(ms: Long): String {
        val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(ms))
    }
}
