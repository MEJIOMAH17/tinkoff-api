package ru.mejiomah17.tinkoff.api

import io.github.rybalkinsd.kohttp.dsl.context.HttpPostContext
import io.github.rybalkinsd.kohttp.dsl.httpGet
import io.github.rybalkinsd.kohttp.dsl.httpPost
import io.github.rybalkinsd.kohttp.ext.url
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import ru.mejiomah17.tinkoff.api.model.auth.by.phone.AuthByPhoneResponse
import ru.mejiomah17.tinkoff.api.model.auth.session.AuthResponse
import ru.mejiomah17.tinkoff.api.model.grouped.requests.accounts.AccountsResponse
import ru.mejiomah17.tinkoff.api.model.operations.OperationsResponse
import java.io.File
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


fun main() {
    val authFile = File("auth.json")
    val logging = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger {
        println(it)
    }).setLevel(HttpLoggingInterceptor.Level.BODY)
    val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()
    if (!authFile.exists()) {
        val tinkoff = Tinkoff.create(
            phoneNumber = System.getenv("phone"),
            httpClient = client,
            confirmationCodeProvider = { println("password sms:");readLine()!! },
            password = System.getenv("password")
        )
        authFile.writeText(Json.encodeToString(tinkoff.authInformation))
    }


    val secondTinkoff = Tinkoff.create(
        authInformation = Json.decodeFromString(authFile.readText()),
        httpClient = client
    )
    authFile.writeText(Json.encodeToString(secondTinkoff.authInformation))
    val accountId = secondTinkoff.getAccounts().payload.accounts?.payload?.first { it.accountType == "Current" }?.id
    if (accountId != null) {
        secondTinkoff.getOperations(
            account = accountId,
            startDate = Instant.now().minus(Duration.ofDays(30)),
            endDate = Instant.now()
        )
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

        /**
         * [phoneNumber] - format +79161234567
         * [confirmationCodeProvider] - should return four digit code from sms
         */
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
            val authByPhoneResponse = authByPhone(
                authResponse = authResponse,
                deviceId = deviceId,
                phoneNumber = phoneNumber,
                client = httpClient
            )
            confirmSession(
                authResponse = authResponse,
                deviceId = deviceId,
                authByPhoneResponse = authByPhoneResponse,
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

        private fun authByPhone(
            authResponse: AuthResponse,
            deviceId: String,
            phoneNumber: String,
            client: OkHttpClient
        ): AuthByPhoneResponse {
            return post(client) {
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
        }

        private fun confirmSession(
            authResponse: AuthResponse,
            deviceId: String,
            authByPhoneResponse: AuthByPhoneResponse,
            confirmationCode: String,
            client: OkHttpClient
        ) {
            httpPost(client) {
                url("https://api.tinkoff.ru/v1/confirm?sessionid=${authResponse.payload.sessionid}")
                body {
                    form {
                        "initialOperationTicket" to authByPhoneResponse.operationTicket
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
            httpPost(client) {
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

        private inline fun <reified T> get(client: OkHttpClient, noinline block: HttpPostContext.() -> Unit): T {
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
        return get {
            url("https://api.tinkoff.ru/v1/operations?account=$account&end=${endDate.toEpochMilli()}&start=${startDate.toEpochMilli()}&sessionid=$sessionId&appVersion=5.8.1&platform=android")
        }
    }

    private inline fun <reified T> get(noinline block: HttpPostContext.() -> Unit): T {
        return Companion.get(client, block)
    }

    private inline fun <reified T> post(noinline block: HttpPostContext.() -> Unit): T {
        return Companion.post(client, block)
    }
}