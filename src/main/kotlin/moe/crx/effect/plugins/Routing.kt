package moe.crx.effect.plugins

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.routing.*
import moe.crx.effect.frontend.loginPage
import moe.crx.effect.frontend.commentsTable
import moe.crx.effect.frontend.createPage
import moe.crx.effect.frontend.deleteComment
import moe.crx.effect.frontend.deletePost
import moe.crx.effect.frontend.editProfilePage
import moe.crx.effect.frontend.feedsTable
import moe.crx.effect.frontend.imagesTable
import moe.crx.effect.frontend.logoutPage
import moe.crx.effect.frontend.mainPage
import moe.crx.effect.frontend.postPage
import moe.crx.effect.frontend.postsTable
import moe.crx.effect.frontend.profilePage
import moe.crx.effect.frontend.statsPage
import moe.crx.effect.frontend.tokensTable
import moe.crx.effect.frontend.usersTable
import moe.crx.effect.models.CommentRepository
import moe.crx.effect.models.FeedRepository
import moe.crx.effect.models.ImageRepository
import moe.crx.effect.models.PostRepository
import moe.crx.effect.models.TokenRepository
import moe.crx.effect.models.UserRepository
import moe.crx.effect.routes.authRoutes
import moe.crx.effect.routes.imageRoute
import moe.crx.effect.routes.userRoutes

fun Application.configureStaticRouting() {
    routing {
        staticResources("/static", "static_content")
    }
}

fun Application.configureImageRouting() {
    routing {
        imageRoute()
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
            mainPage(tokenRepository, feedRepository)
            loginPage(tokenRepository, userRepository)
            logoutPage()
            profilePage(tokenRepository, feedRepository)
            createPage(tokenRepository, postRepository, imageRepository, feedRepository)
            postPage(tokenRepository, postRepository, feedRepository, commentRepository, imageRepository)
            statsPage(tokenRepository, postRepository, userRepository, commentRepository)
            editProfilePage(tokenRepository, userRepository, imageRepository)
            deletePost(tokenRepository, postRepository, feedRepository)
            deleteComment(tokenRepository, commentRepository)

            route("/tables") {
                usersTable(userRepository, tokenRepository)
                tokensTable(tokenRepository)
                feedsTable(feedRepository, tokenRepository)
                postsTable(postRepository, tokenRepository)
                commentsTable(commentRepository, tokenRepository)
                imagesTable(imageRepository, tokenRepository)
            }
        }
    }
}