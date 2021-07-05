package ru.mejiomah17.tinkoff.api.model.personal


import kotlinx.serialization.Serializable

@Serializable
data class Email(
    val emailAddress: String? = null
)