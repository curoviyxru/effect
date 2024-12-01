package moe.crx.effect.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.uri
import io.ktor.server.response.respond

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(mapOf("call" to call.request.uri, "error" to cause.javaClass.simpleName, "message" to (cause.message ?: "")))
        }
        status(HttpStatusCode.TooManyRequests) { call, status ->
            val retryAfter = call.response.headers["Retry-After"]
            call.respond(mapOf("call" to call.request.uri, "error" to "timeout", "retry_after" to (retryAfter ?: "60")))
        }
    }
}