package moe.crx.effect.plugins

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.routing.*
import moe.crx.effect.frontend.commentsTable
import moe.crx.effect.frontend.feedsTable
import moe.crx.effect.frontend.imagesTable
import moe.crx.effect.frontend.postsTable
import moe.crx.effect.frontend.tokensTable
import moe.crx.effect.frontend.usersTable
import moe.crx.effect.models.CommentRepository
import moe.crx.effect.models.FeedRepository
import moe.crx.effect.models.ImageRepository
import moe.crx.effect.models.PostRepository
import moe.crx.effect.models.TokenRepository
import moe.crx.effect.models.UserRepository
import moe.crx.effect.routes.authRoutes
import moe.crx.effect.routes.userRoutes

fun Application.configureRouting() {
    routing {
        staticResources("/static", "static_content")
    }
}

fun Application.configureApiRouting(userRepository: UserRepository, tokenRepository: TokenRepository) {
    routing {
        rateLimit {
            route("/api") {
                authRoutes(userRepository, tokenRepository)
                userRoutes()
            }
        }
    }
}

fun Application.configureFrontendRouting(userRepository: UserRepository, tokenRepository: TokenRepository, feedRepository: FeedRepository, postRepository: PostRepository, commentRepository: CommentRepository, imageRepository: ImageRepository) {
    routing {
        rateLimit {
            route("/tables") {
                usersTable(userRepository)
                tokensTable(tokenRepository)
                feedsTable(feedRepository)
                postsTable(postRepository)
                commentsTable(commentRepository)
                imagesTable(imageRepository)
            }
        }
    }
}