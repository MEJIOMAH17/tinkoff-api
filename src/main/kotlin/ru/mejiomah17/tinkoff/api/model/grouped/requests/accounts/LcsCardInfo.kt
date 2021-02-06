package ru.mejiomah17.tinkoff.api.model.grouped.requests.accounts

import kotlinx.serialization.Serializable

@Serializable

data class LcsCardInfo(
    val bankLogo: String? = null,
    val bankName: String? = null,
    val baseColor: String? = null,
    val country: String? = null,
    val paymentSystem: String? = null,
    val rusBankName: String
)