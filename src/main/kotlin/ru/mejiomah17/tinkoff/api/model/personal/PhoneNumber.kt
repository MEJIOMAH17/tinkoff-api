package ru.mejiomah17.tinkoff.api.model.personal


import kotlinx.serialization.Serializable

@Serializable
data class PhoneNumber(
    val countryCode: String? = null,
    val innerCode: String? = null,
    val number: String? = null
)