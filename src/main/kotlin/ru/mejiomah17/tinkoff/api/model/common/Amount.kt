package ru.mejiomah17.tinkoff.api.model.common

import kotlinx.serialization.Serializable

@Serializable
data class Amount(
    val currency: Currency? = null,
    val value: Double? = null
)
