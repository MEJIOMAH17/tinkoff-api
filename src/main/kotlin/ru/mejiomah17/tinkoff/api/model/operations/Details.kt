package ru.mejiomah17.tinkoff.api.model.operations


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Details(
    val failures: Failures? = null,
    val hasNext: Boolean? = null
)