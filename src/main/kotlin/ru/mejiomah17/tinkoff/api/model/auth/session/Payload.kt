package ru.mejiomah17.tinkoff.api.model.auth.session

import kotlinx.serialization.Serializable

@Serializable
data class Payload(val sessionid: String)