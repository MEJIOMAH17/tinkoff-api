package ru.mejiomah17.tinkoff.api.model.personal


import kotlinx.serialization.Serializable

@Serializable
data class PersonalInfoResponse(
    val payload: Payload? = null,
    val resultCode: String? = null,
    val trackingId: String? = null
)