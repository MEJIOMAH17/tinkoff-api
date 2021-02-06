package ru.mejiomah17.tinkoff.api.model.operations


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FieldsValues(
    val bankContract: String? = null,
    val bankMemberId: String? = null,
    val igBillId: String? = null,
    val insurance: String? = null,
    val maskedFIO: String? = null,
    val message: String? = null,
    val payerCode: String? = null,
    val period: String? = null,
    val phone: String? = null,
    val pointer: String? = null,
    val pointerLinkId: String? = null,
    val pointerType: String? = null,
    val receiverBankName: String? = null,
    val workflowType: String? = null
)