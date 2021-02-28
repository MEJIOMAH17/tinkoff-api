package ru.mejiomah17.tinkoff.api

import io.github.rybalkinsd.kohttp.dsl.context.HttpGetContext
import io.github.rybalkinsd.kohttp.dsl.context.HttpPostContext
import io.github.rybalkinsd.kohttp.dsl.httpGet
import io.github.rybalkinsd.kohttp.dsl.httpPost
import io.github.rybalkinsd.kohttp.ext.url
import java.io.File
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging.logger
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import ru.mejiomah17.tinkoff.api.model.auth.by.password.AuthByPasswordResponse
import ru.mejiomah17.tinkoff.api.model.auth.by.phone.AuthByPhoneResponse
import ru.mejiomah17.tinkoff.api.model.auth.session.AuthResponse
import ru.mejiomah17.tinkoff.api.model.confirm.ConfirmSessionIdResponse
import ru.mejiomah17.tinkoff.api.model.grouped.requests.accounts.AccountsResponse
import ru.mejiomah17.tinkoff.api.model.operations.OperationsResponse


fun main() {
    val authFile = File("auth.json")
    val logging = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger {
        println(it)
    }).setLevel(HttpLoggingInterceptor.Level.BODY)
    val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()
//    if (!authFile.exists()) {
    val tinkoff = try {
        Tinkoff.create(
            phoneNumber = System.getenv("phone"),
            httpClient = client,
            confirmationCodeProvider = { println("password sms:");readLine()!! },
            password = System.getenv("password")
        )
    } catch (e: Exception) {
        println(e)
        throw e
    }



    authFile.writeText(Json.encodeToString(tinkoff.authInformation))
//    }
//
//
//    val tinkoff = Tinkoff.create(
//        authInformation = Json.decodeFromString(authFile.readText()),
//        httpClient = client
//    )
    authFile.writeText(Json.encodeToString(tinkoff.authInformation))
    tinkoff.getAccountsRaw()
    val accountId = tinkoff.getAccounts().payload.accounts?.payload?.first { it.accountType == "Current" }?.id
    if (accountId != null) {
        val operations = tinkoff.getOperations(
            account = accountId,
            startDate = Instant.now().minus(Duration.ofDays(900)),
            endDate = Instant.now()
        ).payload ?: emptyList()
        val categories =
            operations.map { it.mccString to it.spendingCategory?.id to it.spendingCategory?.name }.distinct()
    }

}

class Tinkoff internal constructor(
    private val client: OkHttpClient,
    val authInformation: AuthInformation
) {
    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
        }
        private val log = logger {}

        /**
         * [phoneNumber] - format +79161234567
         * [confirmationCodeProvider] - should return four digit code from sms
         */
        @Throws(WrongPasswordException::class, WrongSmsCodeException::class, WrongPhoneNumberException::class)
        fun create(
            phoneNumber: String,
            confirmationCodeProvider: () -> String,
            password: String,
            httpClient: OkHttpClient = OkHttpClient()
        ): Tinkoff {
            val deviceId = generateDeviceId()
            val authResponse = auth(deviceId, httpClient)
            warmupCache(
                authResponse = authResponse,
                deviceId = deviceId,
                phoneNumber = phoneNumber,
                client = httpClient
            )
            val operationTicket = authByPhone(
                authResponse = authResponse,
                deviceId = deviceId,
                phoneNumber = phoneNumber,
                client = httpClient
            )
            confirmSession(
                authResponse = authResponse,
                deviceId = deviceId,
                operationTicket = operationTicket,
                confirmationCode = confirmationCodeProvider(),
                client = httpClient
            )
            ping(
                authResponse = authResponse,
                deviceId = deviceId,
                client = httpClient
            )
            authByPassword(
                authResponse = authResponse,
                deviceId = deviceId,
                password = password,
                client = httpClient
            )

            val pinHash = generatePinHash()
            val authDate = LocalDateTime.now()
            setPin(
                authResponse = authResponse,
                deviceId = deviceId,
                pinHash = pinHash,
                client = httpClient,
                authDate = authDate
            )

            return Tinkoff(
                client = httpClient,
                authInformation = AuthInformation(
                    sessionId = authResponse.payload.sessionid,
                    deviceId = deviceId,
                    authTypeSafeDate = authDate.formatToAuthTypeSetDate(),
                    pinHash = pinHash
                )
            )
        }

        fun create(
            authInformation: AuthInformation,
            httpClient: OkHttpClient = OkHttpClient(),
        ): Tinkoff {
            val authResponse = auth(deviceId = authInformation.deviceId, httpClient = httpClient)
            val newSessionId = authResponse.payload.sessionid

            val code = httpPost {
                url("https://api.tinkoff.ru/v1/auth/by/pin?sessionid=$newSessionId")
                body {
                    form {
                        "pinHash" to authInformation.pinHash
                        "auth_type" to "pin"
                        "auth_type_set_date" to authInformation.authTypeSafeDate
                        "oldSessionId" to authInformation.sessionId
                        "mobile_device_model" to "MAR-LX1M"
                        "mobile_device_os" to "android"
                        "appVersion" to "5.8.1"
                        "screen_width" to "1080"
                        "root_flag" to "false"
                        "appName" to "mobile"
                        "origin" to "mobile%2Cib5%2Cloyalty%2Cplatform"
                        "connectionType" to "WiFi"
                        "platform" to "android"
                        "screen_dpi" to "480"
                        "mobile_device_os_version" to "10"
                        "screen_height" to "2107"
                        "appsflyer_uid" to "1610221902223-5226330507306162632"
                        "deviceId" to authInformation.deviceId
                        "oldDeviceId" to authInformation.deviceId
                    }
                }
            }.code

            check(code == 200) {
                "code is not 200"
            }
            return Tinkoff(
                client = httpClient,
                authInformation = AuthInformation(
                    sessionId = newSessionId,
                    deviceId = authInformation.deviceId,
                    authTypeSafeDate = authInformation.authTypeSafeDate,
                    pinHash = authInformation.pinHash
                )
            )

        }

        private fun auth(deviceId: String, httpClient: OkHttpClient): AuthResponse {
            return post(client = httpClient) {
                url("https://api.tinkoff.ru/v1/auth/session")
                body {
                    form {
                        "mobile_device_model" to "MAR-LX1M"
                        "mobile_device_os" to "android"
                        "appVersion" to "5.8.1"
                        "screen_width" to "1080"
                        "root_flag" to "false"
                        "appName" to "mobile"
                        "origin" to "mobile,ib5,loyalty,platform"
                        "connectionType" to "WiFi"
                        "platform" to "android"
                        "screen_dpi" to "480"
                        "mobile_device_os_version" to "10"
                        "screen_height" to "2107"
                        "appsflyer_uid" to "1610221902223-9226330507306162632"
                        "fingerprint" to "HUAWEI MAR-LX1M/android: 10/TCSMB/5.8.1###1080x2312x32###180###false###false###"
                        "deviceId" to deviceId
                        "oldDeviceId" to deviceId
                    }
                }
                header {
                    "user-agent" to "HUAWEI MAR-LX1M/android: 10/TCSMB/5.8.1"
                }
            }
        }

        private fun warmupCache(
            authResponse: AuthResponse,
            deviceId: String,
            phoneNumber: String,
            oldSessionId: String? = null,
            client: OkHttpClient
        ) {
            httpPost(client) {
                url("https://api.tinkoff.ru/v1/warmup_cache?sessionid=${authResponse.payload.sessionid}")
                body {
                    form {
                        "phone" to phoneNumber
                        if (oldSessionId != null) {
                            "old_session_id" to oldSessionId
                        }
                        "mobile_device_model" to "MAR-LX1M"
                        "mobile_device_os" to "android"
                        "appVersion" to "5.8.1"
                        "screen_width" to "1080"
                        "root_flag" to "false"
                        "appName" to "mobile"
                        "origin" to "mobile,ib5,loyalty,platform"
                        "connectionType" to "WiFi"
                        "platform" to "android"
                        "screen_dpi" to "480"
                        "mobile_device_os_version" to "10"
                        "screen_height" to "2107"
                        "appsflyer_uid" to "1610221902223-9226330507306162632"
                        "fingerprint" to "HUAWEI MAR-LX1M/android: 10/TCSMB/5.8.1###1080x2312x32###180###false###false###"
                        "deviceId" to deviceId
                        "oldDeviceId" to deviceId
                    }
                }
                header {
                    "user-agent" to "HUAWEI MAR-LX1M/android: 10/TCSMB/5.8.1"
                }
            }
        }

        /**
         * Returns operation ticket
         */
        private fun authByPhone(
            authResponse: AuthResponse,
            deviceId: String,
            phoneNumber: String,
            client: OkHttpClient
        ): String {
            val rs = post<AuthByPhoneResponse>(client) {
                url("https://api.tinkoff.ru/v1/auth/by/phone?sessionid=${authResponse.payload.sessionid}")
                body {
                    form {
                        "phone" to phoneNumber
                        "mobile_device_model" to "MAR-LX1M"
                        "mobile_device_os" to "android"
                        "appVersion" to "5.8.1"
                        "screen_width" to "1080"
                        "root_flag" to "false"
                        "appName" to "mobile"
                        "origin" to "mobile,ib5,loyalty,platform"
                        "connectionType" to "WiFi"
                        "platform" to "android"
                        "screen_dpi" to "480"
                        "mobile_device_os_version" to "10"
                        "screen_height" to "2107"
                        "appsflyer_uid" to "1610221902223-9226330507306162632"
                        "fingerprint" to "HUAWEI MAR-LX1M/android: 10/TCSMB/5.8.1###1080x2312x32###180###false###false###"
                        "deviceId" to deviceId
                        "oldDeviceId" to deviceId
                    }
                }
                header {
                    "user-agent" to "HUAWEI MAR-LX1M/android: 10/TCSMB/5.8.1"
                }
            }
            return when (rs.resultCode) {
                "WAITING_CONFIRMATION",
                "OK" -> rs.operationTicket ?: throw error("OK rs does not contain operation ticket")
                "INVALID_REQUEST_DATA" -> throw WrongPhoneNumberException()
                else -> {
                    error("UNKNOWN CODE ${rs.resultCode}")
                }
            }

        }

        private fun confirmSession(
            authResponse: AuthResponse,
            deviceId: String,
            operationTicket: String,
            confirmationCode: String,
            client: OkHttpClient
        ) {
            val rs = post<ConfirmSessionIdResponse>(client) {
                url("https://api.tinkoff.ru/v1/confirm?sessionid=${authResponse.payload.sessionid}")
                body {
                    form {
                        "initialOperationTicket" to operationTicket
                        "initialOperation" to "auth/by/phone"
                        "confirmationData" to "{\"SMSBYID\":\"$confirmationCode\"}"
                        "mobile_device_model" to "MAR-LX1M"
                        "mobile_device_os" to "android"
                        "appVersion" to "5.8.1"
                        "screen_width" to "1080"
                        "root_flag" to "false"
                        "appName" to "mobile"
                        "origin" to "mobile,ib5,loyalty,platform"
                        "connectionType" to "WiFi"
                        "platform" to "android"
                        "screen_dpi" to "480"
                        "mobile_device_os_version" to "10"
                        "screen_height" to "2107"
                        "appsflyer_uid" to "1610221902223-9226330507306162632"
                        "fingerprint" to "HUAWEI MAR-LX1M/android: 10/TCSMB/5.8.1###1080x2312x32###180###false###false###"
                        "deviceId" to deviceId
                        "oldDeviceId" to deviceId
                    }
                }
                header {
                    "user-agent" to "HUAWEI MAR-LX1M/android: 10/TCSMB/5.8.1"
                }
            }
            when (rs.resultCode.toUpperCase()) {
                "OK" -> Unit
                "CONFIRMATION_FAILED" -> throw WrongSmsCodeException()
                else -> {
                    log.error {
                        "UNKNOWN result code ${rs.resultCode.toUpperCase()}"
                    }
                }
            }
        }

        private fun ping(
            authResponse: AuthResponse,
            deviceId: String,
            client: OkHttpClient
        ) {
            httpPost(client) {
                url("https://api.tinkoff.ru/v1/ping?sessionid=${authResponse.payload.sessionid}")
                body {
                    form {
                        "mobile_device_model" to "MAR-LX1M"
                        "mobile_device_os" to "android"
                        "appVersion" to "5.8.1"
                        "screen_width" to "1080"
                        "root_flag" to "false"
                        "appName" to "mobile"
                        "origin" to "mobile,ib5,loyalty,platform"
                        "connectionType" to "WiFi"
                        "platform" to "android"
                        "screen_dpi" to "480"
                        "mobile_device_os_version" to "10"
                        "screen_height" to "2107"
                        "appsflyer_uid" to "1610221902223-9226330507306162632"
                        "fingerprint" to "HUAWEI MAR-LX1M/android: 10/TCSMB/5.8.1###1080x2312x32###180###false###false###"
                        "deviceId" to deviceId
                        "oldDeviceId" to deviceId
                    }
                }
                header {
                    "user-agent" to "HUAWEI MAR-LX1M/android: 10/TCSMB/5.8.1"
                }
            }
        }

        private fun authByPassword(
            authResponse: AuthResponse,
            deviceId: String,
            password: String,
            client: OkHttpClient,
        ) {
            val result = post<AuthByPasswordResponse>(client) {
                url("https://api.tinkoff.ru/v1/auth/by/password?sessionid=${authResponse.payload.sessionid}")
                body {
                    form {
                        "password" to password
                        "mobile_device_model" to "MAR-LX1M"
                        "mobile_device_os" to "android"
                        "appVersion" to "5.8.1"
                        "screen_width" to "1080"
                        "root_flag" to "false"
                        "appName" to "mobile"
                        "origin" to "mobile,ib5,loyalty,platform"
                        "connectionType" to "WiFi"
                        "platform" to "android"
                        "screen_dpi" to "480"
                        "mobile_device_os_version" to "10"
                        "screen_height" to "2107"
                        "appsflyer_uid" to "1610221902223-9226330507306162632"
                        "fingerprint" to "HUAWEI MAR-LX1M/android: 10/TCSMB/5.8.1###1080x2312x32###180###false###false###"
                        "deviceId" to deviceId
                        "oldDeviceId" to deviceId
                    }
                }
                header {
                    "user-agent" to "HUAWEI MAR-LX1M/android: 10/TCSMB/5.8.1"
                }
            }
            when (result.resultCode.toUpperCase()) {
                "OK" -> Unit
                "INVALID_PASSWORD" -> throw WrongPasswordException()
                else -> {
                    log.error {
                        "UNKNOWN result code ${result.resultCode.toUpperCase()}"
                    }
                }
            }
        }

        private fun setPin(
            authResponse: AuthResponse,
            deviceId: String,
            pinHash: String,
            client: OkHttpClient,
            authDate: LocalDateTime
        ) {
            println(
                httpPost(client) {
                    url("https://api.tinkoff.ru/v1/auth/pin/set?sessionid=${authResponse.payload.sessionid}")
                    body {
                        form {
                            "pinHash" to pinHash
                            "auth_type_set_date" to authDate.formatToAuthTypeSetDate()
                            "mobile_device_model" to "MAR-LX1M"
                            "mobile_device_os" to "android"
                            "appVersion" to "5.8.1"
                            "screen_width" to "1080"
                            "root_flag" to "false"
                            "appName" to "mobile"
                            "origin" to "mobile,ib5,loyalty,platform"
                            "connectionType" to "WiFi"
                            "platform" to "android"
                            "screen_dpi" to "480"
                            "mobile_device_os_version" to "10"
                            "screen_height" to "2107"
                            "appsflyer_uid" to "1610221902223-9226330507306162632"
                            "fingerprint" to "HUAWEI MAR-LX1M/android: 10/TCSMB/5.8.1###1080x2312x32###180###false###false###"
                            "deviceId" to deviceId
                            "oldDeviceId" to deviceId
                        }
                    }
                }.body?.string()
            )
        }

        private fun LocalDateTime.formatToAuthTypeSetDate(): String =
            format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

        private fun generateDeviceId(): String {
            return UUID.randomUUID().toString().replace("-", "").take(16)
        }

        private fun generatePinHash(): String {
            return UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "")
        }

        private inline fun <reified T> post(client: OkHttpClient, noinline block: HttpPostContext.() -> Unit): T {
            return json.decodeFromString(httpPost(client, block).body?.string()!!)
        }
    }

    private val sessionId = authInformation.sessionId
    private val deviceId = authInformation.deviceId

    fun personalInfo(
    ): String {
        return httpGet(client) {
            url("https://api.tinkoff.ru/v1/personal_info?sessionid=$sessionId")
        }.body?.string()!!
    }

    fun getAccounts(): AccountsResponse {
        return json.decodeFromString(getAccountsRaw())
    }

    fun getAccountsRaw(): String {
        return post {
            url("https://api.tinkoff.ru/v1/grouped_requests?sessionid=$sessionId&deviceId=$deviceId&appVersion=5.8.1&platform=android")
            body {
                form {
                    "requestsData" to "[{\"key\":\"accounts\",\"operation\":\"accounts_flat\",\"params\":{}},{\"key\":\"list_shared_resources\",\"operation\":\"list_shared_resources\",\"params\":{}},{\"key\":\"list_owner_shared_resources\",\"operation\":\"list_owner_shared_resources\",\"params\":{}}]"
                }
            }
        }
    }

    fun getOperations(
        account: String,
        startDate: Instant,
        endDate: Instant
    ): OperationsResponse {
        return json.decodeFromString(
            getOperationsRaw(
                account = account,
                startDate = startDate,
                endDate = endDate
            )
        )
    }

    fun getOperationsRaw(
        account: String,
        startDate: Instant,
        endDate: Instant
    ): String {
        return get {
            url("https://api.tinkoff.ru/v1/operations?account=$account&end=${endDate.toEpochMilli()}&start=${startDate.toEpochMilli()}&sessionid=$sessionId&appVersion=5.8.1&platform=android")
        }
    }

    private fun get(block: HttpGetContext.() -> Unit): String {
        return httpGet(client, block).body!!.string()
    }

    private fun post(block: HttpPostContext.() -> Unit): String {
        return httpPost(client, block).body!!.string()
    }
}