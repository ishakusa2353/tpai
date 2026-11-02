package com.example.data.model

data class LicenseRecord(
    val key: String,
    val active: Boolean,
    val tier: String? = null,
    val duration: String? = null,
    val duration_ms: Long? = null,
    val exp: Long? = null,
    val first_login_at: Long? = null,
    val device_id: String? = null,
    val trader_id: String? = null,
    val created_at: Long? = null,
    val last_used_at: Long? = null,
    val note: String? = null
)

sealed class ActivationResult {
    data class Success(val license: LicenseRecord) : ActivationResult()
    data class Error(val message: String) : ActivationResult()
}

enum class AssistantState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING
}

data class ChatMessage(
    val id: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
