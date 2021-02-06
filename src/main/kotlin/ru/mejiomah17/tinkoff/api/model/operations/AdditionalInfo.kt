package ru.mejiomah17.tinkoff.api.model.operations


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdditionalInfo(
    val fieldName: String? = null,
    val fieldValue: String? = null
)