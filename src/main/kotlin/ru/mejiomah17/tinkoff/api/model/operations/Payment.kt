package ru.mejiomah17.tinkoff.api.model.operations


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.mejiomah17.tinkoff.api.model.common.Amount

@Serializable
data class Payment(
    val bankAccountId: String? = null,
    val cardNumber: String? = null,
    val comment: String? = null,
    val feeAmount: Amount? = null,
    val fieldsValues: FieldsValues? = null,
    val hasPaymentOrder: Boolean? = null,
    val paymentId: String? = null,
    val paymentType: String? = null,
    val providerGroupId: String? = null,
    val providerId: String? = null,
    val repeatable: Boolean? = null,
    val sourceIsQr: Boolean? = null,
    val templateId: String? = null,
    val templateIsFavorite: Boolean? = null
)