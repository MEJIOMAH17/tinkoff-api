package ru.mejiomah17.tinkoff.api.model.grouped.requests.accounts

import kotlinx.serialization.Serializable

@Serializable

data class Currency(
    val code: Int? = null,
    val name: String? = null,
    val strCode: String
)