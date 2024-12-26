package moe.crx.effect.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.plugins.statuspages.StatusPagesConfig
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import io.ktor.server.thymeleaf.ThymeleafContent
import moe.crx.effect.frontend.handleToken
import moe.crx.effect.models.TokenRepository

fun Application.configureStatusPages(tokenRepository: TokenRepository) {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            renderPage(call, cause, tokenRepository)
        }
        status(HttpStatusCode.NotFound) {
            renderPage(call, Exception("Page not found."), tokenRepository)
        }
        status(HttpStatusCode.InternalServerError) {
            renderPage(call, Exception("Internal server error."), tokenRepository)
        }
        status(HttpStatusCode.Unauthorized) {
            renderPage(call, Exception("Unauthorized."), tokenRepository)
        }
        status(HttpStatusCode.TooManyRequests) { call, status ->
            val retryAfter = call.response.headers["Retry-After"]
            call.respond(mapOf("call" to call.request.uri, "error" to "timeout", "retry_after" to (retryAfter ?: "60")))
        }
    }
}

suspend fun StatusPagesConfig.renderPage(call: ApplicationCall, cause: Throwable, tokenRepository: TokenRepository) {
    val map = mutableMapOf<String, Any>(
        "call" to call.request.uri,
        "error" to cause.javaClass.simpleName,
        "return_message" to (cause.message ?: "")
    )

    try {
        handleToken(call, tokenRepository)?.let { map["current_user"] = it }
        call.respond(ThymeleafContent("pages/error_page", map))
    } catch (_: Throwable) {
        call.respond(map)
    }
}

