package ru.mejiomah17.tinkoff.api.model.grouped.requests.accounts

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable

data class ListSharedResources(
    val details: Details? = null,
    val payload: JsonElement,
    val resultCode: String
)