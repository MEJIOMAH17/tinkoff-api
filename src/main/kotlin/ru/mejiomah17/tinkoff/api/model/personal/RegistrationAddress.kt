package ru.mejiomah17.tinkoff.api.model.personal


import kotlinx.serialization.Serializable

@Serializable
data class RegistrationAddress(
    val apartmentNumber: String? = null,
    val buildingNumber: String? = null,
    val city: String? = null,
    val cityId: String? = null,
    val constructionNumber: String? = null,
    val country: String? = null,
    val district: String? = null,
    val houseNumber: String? = null,
    val phoneNumber: PhoneNumber? = null,
    val settlement: String? = null,
    val state: String? = null,
    val streetAddress: String? = null,
    val typeName: String? = null,
    val zipCode: ZipCode? = null
)