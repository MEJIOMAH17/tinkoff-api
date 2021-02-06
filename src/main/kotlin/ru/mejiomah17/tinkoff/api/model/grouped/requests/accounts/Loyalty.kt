package ru.mejiomah17.tinkoff.api.model.grouped.requests.accounts

import kotlinx.serialization.Serializable

@Serializable

data class Loyalty(
    val amount: Double? = null,
    val amountPartial: Double? = null,
    val bonusLimit: Double? = null,
    val bonusLimitReached: Boolean? = null,
    val creditLimit: Double? = null,
    val currentAmount: Double? = null,
    val currentAmountPartial: Double? = null,
    val loyalty: String? = null,
    val loyaltyImagine: Boolean? = null,
    val loyaltyPointsId: Int? = null,
    val loyaltyPointsName: String? = null,
    val loyaltySteps: Int? = null,
    val name: String? = null,
    val partialCompensation: Boolean? = null,
    val pendingBalance: Double? = null,
    val pendingBalancePartial: Double? = null,
    val primeLoyaltyGroupId: Int? = null,
    val primeLoyaltyId: String? = null,
    val programId: String? = null,
    val usedCreditLimit: Double? = null,
    val yearRedeemSum: Double
)