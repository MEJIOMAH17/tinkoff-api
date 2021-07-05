package ru.mejiomah17.tinkoff.api.model.personal


import kotlinx.serialization.Serializable

@Serializable
data class Employer(
    val address: Address? = null,
    val name: String? = null
)