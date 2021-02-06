package ru.mejiomah17.tinkoff.api.model.grouped.requests.accounts

import kotlinx.serialization.Serializable

@Serializable

data class Payload(
    val accounts: Accounts? = null,
    val list_owner_shared_resources: ListOwnerSharedResources? = null,
    val list_shared_resources: ListSharedResources
)