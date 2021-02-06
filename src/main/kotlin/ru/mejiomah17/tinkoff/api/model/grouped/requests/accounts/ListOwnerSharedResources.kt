package ru.mejiomah17.tinkoff.api.model.grouped.requests.accounts

import kotlinx.serialization.Serializable

@Serializable

data class ListOwnerSharedResources(
    val details: Details? = null,
    val payload: List<OwnerSharedResources>,
    val resultCode: String
)