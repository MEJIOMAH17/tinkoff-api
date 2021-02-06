package ru.mejiomah17.tinkoff.api.model.operations


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SpendingCategory(
    val icon: String? = null,
    val id: String? = null,
    val name: String? = null,
    val parentId: String? = null
)