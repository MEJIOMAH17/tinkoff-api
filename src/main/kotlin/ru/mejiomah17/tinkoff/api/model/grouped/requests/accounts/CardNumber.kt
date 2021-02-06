package ru.mejiomah17.tinkoff.api.model.grouped.requests.accounts

import kotlinx.serialization.Serializable

@Serializable

data class CardNumber(
    val activated: Boolean? = null,
    val availableBalance: AvailableBalance? = null,
    val brand: Brand? = null,
    val canBeRemoved: Boolean? = null,
    val cardDesign: String? = null,
    val cardIssueType: String? = null,
    val country: String? = null,
    val creationDate: CreationDate? = null,
    val cvcConfirmRequired: Boolean? = null,
    val expiration: Expiration? = null,
    val expirationStatus: String? = null,
    val frozenCard: Boolean? = null,
    val hasWrongPins: Boolean? = null,
    val hash: String? = null,
    val hce: Boolean? = null,
    val hceParentUcid: String? = null,
    val id: String? = null,
    val lcsCardInfo: LcsCardInfo? = null,
    val multiCardCluster: MultiCardCluster? = null,
    val name: String? = null,
    val payable: Boolean? = null,
    val paymentSystem: String? = null,
    val pinSet: Boolean? = null,
    val position: Int? = null,
    val primary: Boolean? = null,
    val recurringFlag: String? = null,
    val reissued: Boolean? = null,
    val status: String? = null,
    val statusCode: String? = null,
    val ucid: String? = null,
    val value: String
)