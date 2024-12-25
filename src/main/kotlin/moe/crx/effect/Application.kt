package moe.crx.effect

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import moe.crx.effect.models.DatabaseCommentRepository
import moe.crx.effect.models.DatabaseFeedRepository
import moe.crx.effect.models.DatabaseImageRepository
import moe.crx.effect.models.DatabasePostRepository
import moe.crx.effect.models.DatabaseTokenRepository
import moe.crx.effect.models.DatabaseUserRepository
import moe.crx.effect.plugins.configureApiRouting
import moe.crx.effect.plugins.configureDatabases
import moe.crx.effect.plugins.configureFrontendRouting
import moe.crx.effect.plugins.configureImageRouting
import moe.crx.effect.plugins.configureLimits
import moe.crx.effect.plugins.configureLogging
import moe.crx.effect.plugins.configureStaticRouting
import moe.crx.effect.plugins.configureSerialization
import moe.crx.effect.plugins.configureStatusPages
import moe.crx.effect.plugins.configureTemplating

fun main() {
    embeddedServer(Netty, port = 8080, host = "127.0.0.1", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val userRepository = DatabaseUserRepository()
    val tokenRepository = DatabaseTokenRepository()
    val feedRepository = DatabaseFeedRepository()
    val postRepository = DatabasePostRepository()
    val commentRepository = DatabaseCommentRepository()
    val imageRepository = DatabaseImageRepository()

    configureDatabases()
    configureSerialization()
    configureLogging()
    configureLimits()
    configureStatusPages(tokenRepository)
    configureTemplating()
    configureStaticRouting()
    configureImageRouting()
    configureApiRouting(userRepository, tokenRepository)
    configureFrontendRouting(userRepository, tokenRepository, feedRepository, postRepository, commentRepository, imageRepository)
}