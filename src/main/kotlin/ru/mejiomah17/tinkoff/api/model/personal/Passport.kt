package ru.mejiomah17.tinkoff.api.model.personal


import kotlinx.serialization.Serializable

@Serializable
data class Passport(
    val registrationAddress: RegistrationAddress? = null
)