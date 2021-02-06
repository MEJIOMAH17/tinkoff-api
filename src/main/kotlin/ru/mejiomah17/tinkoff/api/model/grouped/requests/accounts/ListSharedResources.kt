package ru.mejiomah17.tinkoff.api.model.grouped.requests.accounts

import kotlinx.serialization.Serializable

@Serializable

data class ListSharedResources(
    val details: Details? = null,
    val payload: List<String>,
    val resultCode: String
)