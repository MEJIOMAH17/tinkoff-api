package ru.mejiomah17.tinkoff.api.model.operations


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Location(
    val latitude: Double? = null,
    val longitude: Double? = null
)