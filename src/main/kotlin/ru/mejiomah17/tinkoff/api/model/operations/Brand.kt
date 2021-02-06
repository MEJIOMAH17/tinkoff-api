package ru.mejiomah17.tinkoff.api.model.operations


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Brand(
    val baseColor: String? = null,
    val baseTextColor: String? = null,
    val id: String? = null,
    val link: String? = null,
    val logo: String? = null,
    val logoFile: String? = null,
    val name: String? = null,
    val roundedLogo: Boolean? = null
)