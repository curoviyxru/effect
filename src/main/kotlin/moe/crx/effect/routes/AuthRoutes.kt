package moe.crx.effect.routes

import io.ktor.server.application.Application
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import moe.crx.effect.models.TokenRepository
import moe.crx.effect.models.UserRepository
import java.lang.IllegalArgumentException
import io.ktor.server.response.respond

fun Route.authRegister(userRepository: UserRepository) {
    get("/register") {
        val username = call.queryParameters["username"]
        val password = call.queryParameters["password"]

        if (username == null) {
            throw IllegalArgumentException("username is null")
        }

        if (password == null) {
            throw IllegalArgumentException("password is null")
        }

        val registeredUser = userRepository.register(username, password)

        call.respond(registeredUser)
    }
}

fun Route.authLogin(userRepository: UserRepository, tokenRepository: TokenRepository) {
    get("/login") {
        val username = call.queryParameters["username"]
        val password = call.queryParameters["password"]

        if (username == null) {
            throw IllegalArgumentException("username is null")
        }

        if (password == null) {
            throw IllegalArgumentException("password is null")
        }

        val token = tokenRepository.authorize(username, password)

        call.respond(token)
    }
}

fun Application.authRoutes(userRepository: UserRepository, tokenRepository: TokenRepository) {
    routing {
        route("/auth") {
            authRegister(userRepository)
            authLogin(userRepository, tokenRepository)
        }
    }
}