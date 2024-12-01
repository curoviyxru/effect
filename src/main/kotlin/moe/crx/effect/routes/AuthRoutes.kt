package moe.crx.effect.routes

import io.ktor.server.application.Application
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Route.authLogin() {
    get("/auth.login") {

    }
}

fun Application.authRoutes() {
    routing {
        authLogin()
    }
}