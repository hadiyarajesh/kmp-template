package com.hadiyarajesh.kmptemplate.data.network

import com.hadiyarajesh.kmptemplate.di.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private const val API_BASE_URL = "https://picsum.photos/"

private val NetworkJson = Json {
    ignoreUnknownKeys = true
}

@SingleIn(AppScope::class)
internal class AppHttpClient private constructor(val client: HttpClient) {
    @Inject
    constructor() : this(createHttpClient())

    /** Lets tests supply a mock engine. */
    internal constructor(engine: HttpClientEngine) : this(createHttpClient(engine))
}

private fun createHttpClient(engine: HttpClientEngine? = null): HttpClient {
    val configure: HttpClientConfig<*>.() -> Unit = {
        expectSuccess = true

        install(DefaultRequest) {
            url(API_BASE_URL)
            contentType(ContentType.Application.Json)
        }

        install(ContentNegotiation) {
            json(NetworkJson)
        }

        install(Logging) {
            level = LogLevel.ALL
            logger = object : Logger {
                override fun log(message: String) {
                    println("Ktor: $message")
                }
            }
        }

        install(HttpTimeout) {
            val timeout = 30_000L
            connectTimeoutMillis = timeout
            requestTimeoutMillis = timeout
            socketTimeoutMillis = timeout
        }
    }
    return if (engine == null) HttpClient(configure) else HttpClient(engine, configure)
}
