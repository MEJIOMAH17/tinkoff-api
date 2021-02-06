package ru.mejiomah17.tinkoff.api.model.grouped.requests.accounts

import kotlinx.serialization.Serializable

@Serializable

data class Brand(
    val altName: String? = null,
    val baseColor: String? = null,
    val baseTextColor: String? = null,
    val id: String? = null,
    val link: String? = null,
    val logoFile: String? = null,
    val name: String? = null,
    val roundedLogo: Boolean
)