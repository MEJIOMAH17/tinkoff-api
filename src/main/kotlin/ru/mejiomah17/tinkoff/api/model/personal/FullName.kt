package ru.mejiomah17.tinkoff.api.model.personal


import kotlinx.serialization.Serializable

@Serializable
data class FullName(
    val firstName: String? = null,
    val lastName: String? = null,
    val patronymic: String? = null
)