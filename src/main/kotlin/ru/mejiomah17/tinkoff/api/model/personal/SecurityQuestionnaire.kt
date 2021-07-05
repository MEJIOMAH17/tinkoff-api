package ru.mejiomah17.tinkoff.api.model.personal


import kotlinx.serialization.Serializable

@Serializable
data class SecurityQuestionnaire(
    val lastDate: LastDate? = null,
    val nextStatus: String? = null
)