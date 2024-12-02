package moe.crx.effect.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.ratelimit.RateLimit
import kotlin.time.Duration.Companion.seconds

fun Application.configureLimits() {
    install(RateLimit) {
        register {
            rateLimiter(limit = 100, refillPeriod = 60.seconds)
        }
    }
}