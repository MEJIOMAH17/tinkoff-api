package ru.mejiomah17.tinkoff.api.model.operations


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.mejiomah17.tinkoff.api.model.common.Amount

@Serializable
data class LoyaltyBonus(
    val amount: Amount? = null,
    val loyaltyType: String? = null,
    val status: String? = null
)