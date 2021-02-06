package ru.mejiomah17.tinkoff.api.model.grouped.requests.accounts

import kotlinx.serialization.Serializable

@Serializable
data class TariffInfo(
    val highRateAmount: HighRateAmount? = null,
    val interestRate: Double? = null,
    val lowRate: Double? = null,
    val purchaseSumForHighInterest: PurchaseSumForHighInterest? = null,
    val purchaseSumForInterest: PurchaseSumForInterest? = null
)