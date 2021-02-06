package ru.mejiomah17.tinkoff.api.model.grouped.requests.accounts

import kotlinx.serialization.Serializable

@Serializable
data class OwnerSharedResources(
    val resourceId: String? = null,
    val resourceType: String? = null,
    val status: String? = null
)