package moe.crx.effect.frontend

import io.ktor.server.request.receiveParameters
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.thymeleaf.ThymeleafContent
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import moe.crx.effect.models.FeedRepository
import moe.crx.effect.models.Image
import moe.crx.effect.models.TokenRepository
import moe.crx.effect.models.User
import moe.crx.effect.models.UserRepository

fun Route.usersTable(userRepository: UserRepository) {
    get("/users") {
        val users = userRepository.all()
        call.respond(ThymeleafContent("users_table", mapOf("users" to users)))
    }
    post("/users") {
        val form = call.receiveParameters()
        val delete = form["delete"].toBoolean()
        val id = form["id"]?.toLongOrNull()
        val user = User(
            id = id ?: 0,
            fullName = form["full_name"],
            username = form["username"] ?: "",
            registerDate = form["register_date"]
                .runCatching { Instant.parse(this ?: "") }
                .getOrDefault(Clock.System.now()),
            about = form["about"],
            image = form["image_id"]?.toLongOrNull()?.let { Image(it) },
        )

        if (delete) {
            userRepository.delete(user.id)
        } else if (id != null) {
            userRepository.update(user)
        } else {
            userRepository.create(user)
        }

        val users = userRepository.all()
        call.respond(ThymeleafContent("users_table", mapOf("users" to users)))
    }
}

fun Route.tokensTable(tokenRepository: TokenRepository) {
    get("/tokens") {
        val tokens = tokenRepository.all()
        call.respond(ThymeleafContent("tokens_table", mapOf("tokens" to tokens)))
    }
}

fun Route.feedsTable(feedRepository: FeedRepository) {
    get("/feeds") {
        val feeds = feedRepository.all()
        call.respond(ThymeleafContent("feeds_table", mapOf("feeds" to feeds)))
    }
}
