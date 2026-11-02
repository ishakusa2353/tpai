package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import java.util.UUID

class LicenseStorage(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("tradepilot_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_SAVED_LICENSE_KEY = "saved_license_key"
        private const val KEY_LICENSE_TIER = "license_tier"
        private const val KEY_LICENSE_EXPIRY = "license_expiry"
        private const val KEY_LICENSE_DURATION = "license_duration"
        private const val KEY_LICENSE_ACTIVE = "license_active"
        private const val KEY_FIRST_LOGIN_AT = "first_login_at"
        private const val KEY_SPEECH_RATE = "speech_rate"
        private const val KEY_SPEECH_PITCH = "speech_pitch"
        private const val KEY_AUTO_VOICE_REPLY = "auto_voice_reply"
    }

    /**
     * Generates or retrieves a stable, unique device ID formatted like:
     * DEV_12a34b5c_XYZ89 to conform with the existing ishak_licenses conventions.
     */
    val deviceId: String
        get() {
            var id = prefs.getString(KEY_DEVICE_ID, null)
            if (id.isNullOrEmpty()) {
                val hexPart = UUID.randomUUID().toString().replace("-", "").take(8)
                val suffix = ('A'..'Z').shuffled().take(5).joinToString("")
                id = "DEV_${hexPart}_$suffix"
                prefs.edit().putString(KEY_DEVICE_ID, id).apply()
            }
            return id
        }

    fun saveLicense(
        key: String,
        tier: String?,
        expiry: Long?,
        duration: String?,
        firstLoginAt: Long?
    ) {
        prefs.edit()
            .putString(KEY_SAVED_LICENSE_KEY, key.trim())
            .putString(KEY_LICENSE_TIER, tier ?: "VIP")
            .putLong(KEY_LICENSE_EXPIRY, expiry ?: -1L)
            .putString(KEY_LICENSE_DURATION, duration ?: "30d")
            .putLong(KEY_FIRST_LOGIN_AT, firstLoginAt ?: System.currentTimeMillis())
            .putBoolean(KEY_LICENSE_ACTIVE, true)
            .apply()
    }

    val savedLicenseKey: String?
        get() = prefs.getString(KEY_SAVED_LICENSE_KEY, null)

    val licenseTier: String
        get() = prefs.getString(KEY_LICENSE_TIER, "VIP") ?: "VIP"

    val licenseExpiry: Long
        get() = prefs.getLong(KEY_LICENSE_EXPIRY, -1L)

    val licenseDuration: String
        get() = prefs.getString(KEY_LICENSE_DURATION, "30d") ?: "30d"

    val firstLoginAt: Long
        get() = prefs.getLong(KEY_FIRST_LOGIN_AT, 0L)

    val isLicenseSaved: Boolean
        get() = !savedLicenseKey.isNullOrBlank()

    fun clearLicense() {
        prefs.edit()
            .remove(KEY_SAVED_LICENSE_KEY)
            .remove(KEY_LICENSE_TIER)
            .remove(KEY_LICENSE_EXPIRY)
            .remove(KEY_LICENSE_DURATION)
            .remove(KEY_LICENSE_ACTIVE)
            .remove(KEY_FIRST_LOGIN_AT)
            .apply()
    }

    var speechRate: Float
        get() = prefs.getFloat(KEY_SPEECH_RATE, 1.0f)
        set(value) = prefs.edit().putFloat(KEY_SPEECH_RATE, value).apply()

    var speechPitch: Float
        get() = prefs.getFloat(KEY_SPEECH_PITCH, 1.0f)
        set(value) = prefs.edit().putFloat(KEY_SPEECH_PITCH, value).apply()

    var autoVoiceReply: Boolean
        get() = prefs.getBoolean(KEY_AUTO_VOICE_REPLY, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_VOICE_REPLY, value).apply()
}
