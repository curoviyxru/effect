package moe.crx.effect.frontend

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receiveParameters
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.thymeleaf.ThymeleafContent
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import moe.crx.effect.models.Comment
import moe.crx.effect.models.CommentRepository
import moe.crx.effect.models.FeedRepository
import moe.crx.effect.models.Image
import moe.crx.effect.models.ImageRepository
import moe.crx.effect.models.Post
import moe.crx.effect.models.PostRepository
import moe.crx.effect.models.TokenRepository
import moe.crx.effect.models.User
import moe.crx.effect.models.UserRepository

fun Route.usersTable(userRepository: UserRepository, tokenRepository: TokenRepository) {
    get("/users") {
        val u = handleToken(tokenRepository)
        if (!isAdmin(u)) {
            call.respond(HttpStatusCode.Unauthorized)
            return@get
        }

        val users = userRepository.all()
        call.respond(ThymeleafContent("tables/users_table", mapOf("users" to users)))
    }
    post("/users") {
        val u = handleToken(tokenRepository)
        if (!isAdmin(u)) {
            call.respond(HttpStatusCode.Unauthorized)
            return@post
        }

        val form = call.receiveParameters()
        val delete = form["delete"].toBoolean()
        val id = form["id"]?.toLongOrNull()
        val user = User(
            id = id ?: 0,
            fullName = form["full_name"],
            username = form["username"] ?: "",
            creationDate = form["creation_date"]
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
        call.respond(ThymeleafContent("tables/users_table", mapOf("users" to users)))
    }
}

fun Route.tokensTable(tokenRepository: TokenRepository) {
    get("/tokens") {
        val u = handleToken(tokenRepository)
        if (!isAdmin(u)) {
            call.respond(HttpStatusCode.Unauthorized)
            return@get
        }

        val tokens = tokenRepository.all()
        call.respond(ThymeleafContent("tables/tokens_table", mapOf("tokens" to tokens)))
    }
}

fun Route.feedsTable(feedRepository: FeedRepository, tokenRepository: TokenRepository) {
    get("/feeds") {
        val u = handleToken(tokenRepository)
        if (!isAdmin(u)) {
            call.respond(HttpStatusCode.Unauthorized)
            return@get
        }

        val feeds = feedRepository.all()
        call.respond(ThymeleafContent("tables/feeds_table", mapOf("feeds" to feeds)))
    }
}

fun Route.commentsTable(commentRepository: CommentRepository, tokenRepository: TokenRepository) {
    get("/comments") {
        val u = handleToken(tokenRepository)
        if (!isAdmin(u)) {
            call.respond(HttpStatusCode.Unauthorized)
            return@get
        }

        val comments = commentRepository.all()
        call.respond(ThymeleafContent("tables/comments_table", mapOf("comments" to comments)))
    }
    post("/comments") {
        val u = handleToken(tokenRepository)
        if (!isAdmin(u)) {
            call.respond(HttpStatusCode.Unauthorized)
            return@post
        }

        val form = call.receiveParameters()
        val delete = form["delete"].toBoolean()
        val id = form["id"]?.toLongOrNull()
        val comment = Comment(
            id = id ?: 0,
            user = User(form["user_id"]?.toLongOrNull() ?: 0),
            text = form["text"],
            image = form["image_id"]?.toLongOrNull()?.let { Image(it) },
            post = Post(form["post_id"]?.toLongOrNull() ?: 0),
            creationDate = form["creation_date"]
                .runCatching { Instant.parse(this ?: "") }
                .getOrDefault(Clock.System.now()),
        )

        if (delete) {
            commentRepository.delete(comment.id)
        } else if (id != null) {
            commentRepository.update(comment)
        } else {
            commentRepository.create(comment)
        }

        val comments = commentRepository.all()
        call.respond(ThymeleafContent("tables/comments_table", mapOf("comments" to comments)))
    }
}

fun Route.imagesTable(imageRepository: ImageRepository, tokenRepository: TokenRepository) {
    get("/images") {
        val u = handleToken(tokenRepository)
        if (!isAdmin(u)) {
            call.respond(HttpStatusCode.Unauthorized)
            return@get
        }

        val images = imageRepository.all()
        call.respond(ThymeleafContent("tables/images_table", mapOf("images" to images)))
    }
    post("/images") {
        val u = handleToken(tokenRepository)
        if (!isAdmin(u)) {
            call.respond(HttpStatusCode.Unauthorized)
            return@post
        }

        val form = call.receiveParameters()
        val delete = form["delete"].toBoolean()
        val id = form["id"]?.toLongOrNull()
        val image = Image(
            id = id ?: 0,
            url = form["url"] ?: "",
            width = form["width"]?.toIntOrNull() ?: 0,
            height = form["height"]?.toIntOrNull() ?: 0,
            fileSize = form["file_size"]?.toLongOrNull() ?: 0
        )

        if (delete) {
            imageRepository.delete(image.id)
        } else if (id != null) {
            imageRepository.update(image)
        } else {
            imageRepository.create(image)
        }

        val images = imageRepository.all()
        call.respond(ThymeleafContent("tables/images_table", mapOf("images" to images)))
    }
}

fun Route.postsTable(postRepository: PostRepository, tokenRepository: TokenRepository) {
    get("/posts") {
        val u = handleToken(tokenRepository)
        if (!isAdmin(u)) {
            call.respond(HttpStatusCode.Unauthorized)
            return@get
        }

        val posts = postRepository.all()
        call.respond(ThymeleafContent("tables/posts_table", mapOf("posts" to posts)))
    }
    post("/posts") {
        val u = handleToken(tokenRepository)
        if (!isAdmin(u)) {
            call.respond(HttpStatusCode.Unauthorized)
            return@post
        }

        val form = call.receiveParameters()
        val delete = form["delete"].toBoolean()
        val id = form["id"]?.toLongOrNull()
        val post = Post(
            id = id ?: 0,
            title = form["title"] ?: "",
            previewText = form["preview_text"],
            fullText = form["full_text"] ?: "",
            image = form["image_id"]?.toLongOrNull()?.let { Image(it) },
            creationDate = form["creation_date"]
                .runCatching { Instant.parse(this ?: "") }
                .getOrDefault(Clock.System.now()),
            viewCount = form["view_count"]?.toLongOrNull() ?: 0,
            category = form["category"]
        )

        if (delete) {
            postRepository.delete(post.id)
        } else if (id != null) {
            postRepository.update(post)
        } else {
            postRepository.create(post)
        }

        val posts = postRepository.all()
        call.respond(ThymeleafContent("tables/posts_table", mapOf("posts" to posts)))
    }
}