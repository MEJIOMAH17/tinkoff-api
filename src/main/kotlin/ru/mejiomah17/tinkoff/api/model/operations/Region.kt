package ru.mejiomah17.tinkoff.api.model.operations


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Region(
    val address: String? = null,
    val city: String? = null,
    val country: String? = null,
    val zip: String? = null
)