package moe.crx.effect.plugins

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.routing.*
import moe.crx.effect.models.DatabaseTokenRepository
import moe.crx.effect.models.DatabaseUserRepository
import moe.crx.effect.routes.authRoutes
import moe.crx.effect.routes.userRoutes

fun Application.configureRouting() {
    routing {
        staticResources("/static", "static_content")
    }
}

fun Application.configureApiRouting() {
    val userRepository = DatabaseUserRepository()
    val tokenRepository = DatabaseTokenRepository()

    routing {
        rateLimit {
            authRoutes(userRepository, tokenRepository)
            userRoutes()
        }
    }
}