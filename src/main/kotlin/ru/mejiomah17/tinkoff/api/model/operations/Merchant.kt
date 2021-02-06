package ru.mejiomah17.tinkoff.api.model.operations


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Merchant(
    val name: String? = null,
    val region: Region? = null
)