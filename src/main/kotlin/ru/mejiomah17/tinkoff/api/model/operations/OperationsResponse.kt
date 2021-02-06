package ru.mejiomah17.tinkoff.api.model.operations


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OperationsResponse(
    val details: Details? = null,
    val payload: List<Payload>? = null,
    val resultCode: String? = null,
    val trackingId: String? = null
)