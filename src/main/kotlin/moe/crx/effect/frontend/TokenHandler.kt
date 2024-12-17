package moe.crx.effect.frontend

import io.ktor.server.application.ApplicationCall
import io.ktor.server.routing.RoutingContext
import moe.crx.effect.models.TokenRepository
import moe.crx.effect.models.User

suspend fun RoutingContext.handleToken(tokenRepository: TokenRepository): User? {
    return handleToken(call, tokenRepository)
}

suspend fun handleToken(call: ApplicationCall, tokenRepository: TokenRepository): User? {
    val token = call.request.cookies["token"] ?: return null
    return tokenRepository.authorize(token)?.user;
}