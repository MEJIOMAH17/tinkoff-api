package ru.mejiomah17.tinkoff.api

import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class AuthInformation(
    val sessionId: String,
    val deviceId: String,
    val authTypeSafeDate: String,
    val pinHash: String
)