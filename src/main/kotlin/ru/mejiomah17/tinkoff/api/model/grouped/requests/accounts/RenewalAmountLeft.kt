package ru.mejiomah17.tinkoff.api.model.grouped.requests.accounts

import kotlinx.serialization.Serializable

@Serializable

data class RenewalAmountLeft(
    val currency: Currency? = null,
    val value: Double
)