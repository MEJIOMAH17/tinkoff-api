package ru.mejiomah17.tinkoff.api.model.auth.by.phone

import kotlinx.serialization.Serializable

@Serializable
class AuthByPhoneResponse(
    val resultCode: String,
    val operationTicket: String? = null
)