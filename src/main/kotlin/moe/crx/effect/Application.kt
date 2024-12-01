package moe.crx.effect

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import moe.crx.effect.plugins.configureApiRouting
import moe.crx.effect.plugins.configureDatabases
import moe.crx.effect.plugins.configureLimits
import moe.crx.effect.plugins.configureLogging
import moe.crx.effect.plugins.configureRouting
import moe.crx.effect.plugins.configureSerialization
import moe.crx.effect.plugins.configureStatusPages

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureDatabases()
    configureSerialization()
    configureLogging()
    configureLimits()
    configureStatusPages()
    configureRouting()
    configureApiRouting()
}