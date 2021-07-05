package ru.mejiomah17.tinkoff.api.model.personal


import kotlinx.serialization.Serializable

@Serializable
data class PersonalInfo(
    val citizenship: String? = null,
    val email: Email? = null,
    val fullName: FullName? = null,
    val homeAddress: HomeAddress? = null,
    val inn: String? = null,
    val isResident: Boolean? = null,
    val mobilePhoneNumber: PhoneNumber? = null,
    val sex: String? = null,
    val snils: String? = null
)