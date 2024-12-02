package moe.crx.effect.frontend

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

fun Route.commentsTable(commentRepository: CommentRepository) {
    get("/comments") {
        val comments = commentRepository.all()
        call.respond(ThymeleafContent("comments_table", mapOf("comments" to comments)))
    }
    post("/comments") {
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
            lastEditDate = form["last_edit_date"]
                .runCatching { Instant.parse(this ?: "") }
                .getOrNull(),
        )

        if (delete) {
            commentRepository.delete(comment.id)
        } else if (id != null) {
            commentRepository.update(comment)
        } else {
            commentRepository.create(comment)
        }

        val comments = commentRepository.all()
        call.respond(ThymeleafContent("comments_table", mapOf("comments" to comments)))
    }
}

fun Route.imagesTable(imageRepository: ImageRepository) {
    get("/images") {
        val images = imageRepository.all()
        call.respond(ThymeleafContent("images_table", mapOf("images" to images)))
    }
    post("/images") {
        val form = call.receiveParameters()
        val delete = form["delete"].toBoolean()
        val id = form["id"]?.toLongOrNull()
        val image = Image(
            id = id ?: 0,
            url = form["url"] ?: "",
            width = form["width"]?.toIntOrNull() ?: 0,
            height = form["height"]?.toIntOrNull() ?: 0,
            fileSize = form["file_size"]?.toLongOrNull() ?: 0,
            creationDate = form["creation_date"]
                .runCatching { Instant.parse(this ?: "") }
                .getOrDefault(Clock.System.now())
        )

        if (delete) {
            imageRepository.delete(image.id)
        } else if (id != null) {
            imageRepository.update(image)
        } else {
            imageRepository.create(image)
        }

        val images = imageRepository.all()
        call.respond(ThymeleafContent("images_table", mapOf("images" to images)))
    }
}

fun Route.postsTable(postRepository: PostRepository) {
    get("/posts") {
        val posts = postRepository.all()
        call.respond(ThymeleafContent("posts_table", mapOf("posts" to posts)))
    }
    post("/posts") {
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
            category = form["category"],
            lastEditDate = form["last_edit_date"]
                .runCatching { Instant.parse(this ?: "") }
                .getOrNull(),
        )

        if (delete) {
            postRepository.delete(post.id)
        } else if (id != null) {
            postRepository.update(post)
        } else {
            postRepository.create(post)
        }

        val posts = postRepository.all()
        call.respond(ThymeleafContent("posts_table", mapOf("posts" to posts)))
    }
}