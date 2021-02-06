package ru.mejiomah17.tinkoff.api.model.grouped.requests.accounts

import kotlinx.serialization.Serializable

@Serializable

data class Accounts(
    val details: Details? = null,
    val payload: List<AccountPayload>,
    val resultCode: String
)