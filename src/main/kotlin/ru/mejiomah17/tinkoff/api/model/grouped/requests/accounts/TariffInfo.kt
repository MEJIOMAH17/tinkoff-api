package ru.mejiomah17.tinkoff.api.model.grouped.requests.accounts

import kotlinx.serialization.Serializable
import ru.mejiomah17.tinkoff.api.model.common.Amount

@Serializable
data class TariffInfo(
    val highRateAmount: Amount? = null,
    val interestRate: Double? = null,
    val lowRate: Double? = null,
    val purchaseSumForHighInterest: Amount? = null,
    val purchaseSumForInterest: Amount? = null
)