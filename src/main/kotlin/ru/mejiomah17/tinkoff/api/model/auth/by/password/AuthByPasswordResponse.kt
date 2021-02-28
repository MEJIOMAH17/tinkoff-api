package ru.mejiomah17.tinkoff.api.model.auth.by.password

import kotlinx.serialization.Serializable

@Serializable
data class AuthByPasswordResponse(
    val resultCode: String
)