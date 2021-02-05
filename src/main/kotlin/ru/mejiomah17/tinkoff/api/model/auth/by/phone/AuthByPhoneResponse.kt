package ru.mejiomah17.tinkoff.api.model.auth.by.phone

import kotlinx.serialization.Serializable

@Serializable
data class AuthByPhoneResponse(val operationTicket:String)