package ru.mejiomah17.tinkoff.api.model.confirm

import kotlinx.serialization.Serializable

@Serializable
data class ConfirmSessionIdResponse(
    val resultCode: String
)