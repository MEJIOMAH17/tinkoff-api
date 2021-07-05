package ru.mejiomah17.tinkoff.api.model.personal


import kotlinx.serialization.Serializable

@Serializable
data class Payload(
    val employer: Employer? = null,
    val isVIP: Boolean? = null,
    val passport: Passport? = null,
    val personalInfo: PersonalInfo? = null,
    val securityQuestionnaire: SecurityQuestionnaire? = null,
    val subscriptionDestination: String? = null
)