package moe.crx.effect.frontend

import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.request.receiveMultipart
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.thymeleaf.ThymeleafContent
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.format.char
import moe.crx.effect.database.PostEntity
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
import moe.crx.effect.utils.generatePages
import moe.crx.effect.utils.parseSearchQuery
import java.lang.Math.clamp
import kotlin.collections.set
import kotlin.text.toLongOrNull

fun Route.mainPage(tokenRepository: TokenRepository, feedRepository: FeedRepository) {
    get("/") {
        renderMainPage(tokenRepository, feedRepository)
    }
}

suspend fun RoutingContext.servePostsData(feedRepository: FeedRepository, parsedQuery: Map<String, String>, map: MutableMap<String, Any>, dropError: Boolean = true) {
    val pageSize = 20
    var count = feedRepository.count(
        parsedQuery["username"],
        parsedQuery["title"],
        parsedQuery["category"],
        parsedQuery["minViews"]?.toLongOrNull(),
        parsedQuery["maxViews"]?.toLongOrNull(),
        parsedQuery["date"]
            .runCatching {
                LocalDate.parse(this ?: "", LocalDate.Format {
                    dayOfMonth()
                    char('.')
                    monthNumber()
                    char('.')
                    year()
                }).atStartOfDayIn(TimeZone.currentSystemDefault())
            }
            .getOrNull(),
        parsedQuery["text"]
    )

    var pagesCount = (count / pageSize) + (if (count % pageSize > 0) 1 else 0)

    if (pagesCount == 0L) {
        map["first_page"] = true
        map["last_page"] = true
        if (dropError) {
            map["return_message"] = "No posts found"
            call.respond(ThymeleafContent("pages/main_feed", map))
        }
        return
    }

    var page = clamp(call.request.queryParameters["page"]?.toLongOrNull() ?: 1, 1, pagesCount)
    var feed = feedRepository.query((page - 1) * pageSize, pageSize,
        parsedQuery["username"],
        parsedQuery["title"],
        parsedQuery["category"],
        parsedQuery["minViews"]?.toLongOrNull(),
        parsedQuery["maxViews"]?.toLongOrNull(),
        parsedQuery["date"]
            .runCatching {
                LocalDate.parse(this ?: "", LocalDate.Format {
                    dayOfMonth()
                    char('.')
                    monthNumber()
                    char('.')
                    year()
                }).atStartOfDayIn(TimeZone.currentSystemDefault())
            }
            .getOrNull(),
        parsedQuery["text"]
    )

    map["feed_list"] = feed
    map["page_list"] = generatePages(page, pagesCount)
    map["first_page"] = page == 1L
    map["last_page"] = page == pagesCount
    map["current_page"] = page
}

suspend fun RoutingContext.renderMainPage(tokenRepository: TokenRepository, feedRepository: FeedRepository) {
    val map = mutableMapOf<String, Any>()
    handleToken(tokenRepository)?.let { map["current_user"] = it }

    val query = call.request.queryParameters["query"] ?: ""
    val parsedQuery = parseSearchQuery(query)

    map["query_url"] = if (!query.isBlank()) "&query=$query" else ""

    servePostsData(feedRepository, parsedQuery, map)

    map["query"] = query
    call.respond(ThymeleafContent("pages/main_feed", map))
}

fun Route.logoutPage() {
    get("/logout") {
        call.response.cookies.append(
            name = "token",
            value = "",
            maxAge = 0
        )
        call.respondRedirect("/")
    }
}

fun Route.loginPage(tokenRepository: TokenRepository, userRepository: UserRepository) {
    get("/login") {
        renderLoginPage(tokenRepository)
    }
    post("/login") {
        try {
            val form = call.receiveParameters()

            val username = form["username"]
            val password = form["password"]

            if (username == null) {
                throw IllegalArgumentException("Username is empty.")
            }

            if (password == null) {
                throw IllegalArgumentException("Password is empty.")
            }

            try {
                userRepository.create(User(username = username), password)
            } catch (_: Exception) { }

            val token = tokenRepository.authorize(username, password)

            call.response.cookies.append(name = "token", value = token.accessToken)

            call.respondRedirect("/profile")
        } catch (e: Exception) {
            renderLoginPage(tokenRepository, e.message)
        }
    }
}

suspend fun RoutingContext.renderLoginPage(tokenRepository: TokenRepository, returnMessage: String? = null) {
    val user = handleToken(tokenRepository)

    if (user != null) {
        call.respondRedirect("/profile")
        return
    }

    val map = mutableMapOf<String, Any>()
    returnMessage?.let { map["return_message"] = it }

    handleToken(tokenRepository)?.let { map["current_user"] = it }
    call.respond(ThymeleafContent("pages/login_page", map))
}

fun Route.profilePage(tokenRepository: TokenRepository, feedRepository: FeedRepository) {
    get("/profile") {
        val user = handleToken(tokenRepository)
        val map = mutableMapOf<String, Any>()
        handleToken(tokenRepository)?.let { map["current_user"] = it }

        if (user == null) {
            call.respondRedirect("/login")
            return@get
        }

        servePostsData(feedRepository, mapOf("username" to user.username), map)

        map["query_url"] = ""
        map["user"] = user
        call.respond(ThymeleafContent("pages/profile_page", map))
    }
}

fun Route.createPage(tokenRepository: TokenRepository, postRepository: PostRepository, imageRepository: ImageRepository, feedRepository: FeedRepository) {
    get("/create") {
        val user = handleToken(tokenRepository)
        val map = mutableMapOf<String, Any>()
        handleToken(tokenRepository)?.let { map["current_user"] = it }

        if (user == null) {
            call.respondRedirect("/login")
            return@get
        }

        val postId = call.request.queryParameters["id"]?.toLongOrNull()
        val post = if (postId != null) postRepository.view(postId) else null

        if (post != null) {
            map["title"] = post.title
            map["category"] = post.category ?: ""
            map["fullText"] = post.fullText
            map["previewText"] = post.previewText ?: ""
            map["isUpdate"] = true
        }

        call.respond(ThymeleafContent("pages/create_page", map))
    }
    post("/create") {
        val user = handleToken(tokenRepository)
        val map = mutableMapOf<String, Any>()
        handleToken(tokenRepository)?.let { map["current_user"] = it }

        if (user == null) {
            call.respondRedirect("/login")
            return@post
        }

        val postId = call.request.queryParameters["id"]?.toLongOrNull()
        var post = if (postId != null) postRepository.view(postId) else null

        val multipart = call.receiveMultipart()
        val form = mutableMapOf<String, String>()
        var image: Image? = null

        multipart.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    part.name?.let { form.put(it, part.value) }
                }
                is PartData.FileItem -> {
                    image = imageRepository.upload(part)
                }
                else -> {}
            }
            part.dispose()
        }

        val title = form["title"]
        val category = form["category"]
        val fullText = form["full_text"]
        val previewText = form["preview_text"]

        map["title"] = title ?: ""
        map["category"] = category ?: ""
        map["fullText"] = fullText ?: ""
        map["previewText"] = previewText ?: ""
        map["isUpdate"] = post != null

        try {
            if (title == null || title.isBlank()) {
                throw IllegalArgumentException("Title can't empty")
            }
            if (fullText == null || fullText.isBlank()) {
                throw IllegalArgumentException("Article text can't empty")
            }

            if (post == null) {
                post = postRepository.create(Post(
                    title = title,
                    category = category,
                    fullText = fullText,
                    previewText = previewText,
                    image = image
                ))

                feedRepository.create(user, post)
            } else {
                post.title = title;
                post.category = category;
                post.fullText = fullText;
                post.previewText = previewText;
                post = postRepository.update(post)
            }

            if (post == null) {
                throw Exception("Post was null")
            }

            call.respondRedirect("/post/${post.id}")
        } catch (e: Exception) {
            e.message?.let { map["return_message"] = it }
            call.respond(ThymeleafContent("pages/create_page", map))
        }
    }
}

suspend fun RoutingContext.renderPostPage(tokenRepository: TokenRepository, postRepository: PostRepository, feedRepository: FeedRepository, commentRepository: CommentRepository, returnMessage: String? = null) {
    val map = mutableMapOf<String, Any>()
    handleToken(tokenRepository)?.let { map["current_user"] = it }
    returnMessage?.let { map["return_message"] = it }

    val postId = call.parameters["id"]?.toLongOrNull()

    if (postId == null) {
        call.respond(HttpStatusCode.NotFound)
        return
    }

    val post = postRepository.view(postId)

    if (post == null) {
        call.respond(HttpStatusCode.NotFound)
        return
    }

    val feed = feedRepository.get(post)
    val comments = commentRepository.get(post)

    map["feed"] = feed
    map["comment_list"] = comments
    call.respond(ThymeleafContent("pages/post_page", map))
}

fun Route.postPage(tokenRepository: TokenRepository, postRepository: PostRepository, feedRepository: FeedRepository, commentRepository: CommentRepository, imageRepository: ImageRepository) {
    get("/post/{id}") {
        renderPostPage(tokenRepository, postRepository, feedRepository, commentRepository)
    }
    post("/post/{id}") {
        val user = handleToken(tokenRepository)
        val map = mutableMapOf<String, Any>()
        handleToken(tokenRepository)?.let { map["current_user"] = it }

        val postId = call.parameters["id"]?.toLongOrNull()

        if (postId == null) {
            call.respond(HttpStatusCode.NotFound)
            return@post
        }

        val post = postRepository.view(postId)

        if (post == null) {
            call.respond(HttpStatusCode.NotFound)
            return@post
        }

        if (user == null) {
            renderPostPage(tokenRepository, postRepository, feedRepository, commentRepository)
            return@post
        }

        val multipart = call.receiveMultipart()
        val form = mutableMapOf<String, String>()
        var image: Image? = null

        multipart.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    part.name?.let { form.put(it, part.value) }
                }
                is PartData.FileItem -> {
                    image = imageRepository.upload(part)
                }
                else -> {}
            }
            part.dispose()
        }

        val text = form["text"]

        map["text"] = text ?: ""

        try {
            if ((text == null || text.isBlank()) && image == null) {
                throw IllegalArgumentException("Comment text and image can't be both empty")
            }

            commentRepository.create(Comment(
                text = text,
                image = image,
                user = user,
                post = post
            ))

            call.respondRedirect("/post/${postId}")
        } catch (e: Exception) {
            renderPostPage(tokenRepository, postRepository, feedRepository, commentRepository, e.message)
        }
    }
}

fun Route.statsPage(tokenRepository: TokenRepository, postRepository: PostRepository, userRepository: UserRepository, commentRepository: CommentRepository) {
    get("/stats") {
        val map = mutableMapOf<String, Any>()
        handleToken(tokenRepository)?.let { map["current_user"] = it }

        map["posts_day"]    = postRepository.count(Clock.System.now())
        map["posts"]        = postRepository.count()
        map["comments_day"] = commentRepository.count(Clock.System.now())
        map["comments"]     = commentRepository.count()
        map["users_day"]    = userRepository.count(Clock.System.now())
        map["users"]        = userRepository.count()

        call.respond(ThymeleafContent("pages/stats_page", map))
    }
}

fun Route.editProfilePage(tokenRepository: TokenRepository, userRepository: UserRepository, imageRepository: ImageRepository) {
    get("/profile/edit") {
        val user = handleToken(tokenRepository)
        val map = mutableMapOf<String, Any>()
        handleToken(tokenRepository)?.let { map["current_user"] = it }

        if (user == null) {
            call.respondRedirect("/login")
            return@get
        }

        map["full_name"] = user.fullName ?: ""
        map["about"] = user.about ?: ""

        call.respond(ThymeleafContent("pages/edit_profile_page", map))
    }
    post("/profile/edit") {
        val user = handleToken(tokenRepository)
        val map = mutableMapOf<String, Any>()
        handleToken(tokenRepository)?.let { map["current_user"] = it }

        if (user == null) {
            call.respondRedirect("/login")
            return@post
        }

        val multipart = call.receiveMultipart()
        val form = mutableMapOf<String, String>()
        var image: Image? = null

        multipart.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    part.name?.let { form.put(it, part.value) }
                }
                is PartData.FileItem -> {
                    image = imageRepository.upload(part)
                }
                else -> {}
            }
            part.dispose()
        }

        var fullName = form["full_name"]
        var about = form["about"]
        var password = form["password"]

        fullName?.let { if (it.isBlank()) fullName = null }
        about?.let { if (it.isBlank()) about = null }
        password?.let { if (it.isBlank()) password = null }

        map["full_name"] = fullName ?: ""
        map["about"] = about ?: ""

        try {
            user.fullName = fullName
            user.about = about
            image?.let { user.image = it }
            userRepository.update(user, password)

            map["return_message"] = "Profile was edited."
            call.respond(ThymeleafContent("pages/edit_profile_page", map))
        } catch (e: Exception) {
            e.message?.let { map["return_message"] = it }
            call.respond(ThymeleafContent("pages/edit_profile_page", map))
        }
    }
}

fun Route.deletePost(tokenRepository: TokenRepository, postRepository: PostRepository, feedRepository: FeedRepository) {
    get("/deletePost") {
        val user = handleToken(tokenRepository)
        val map = mutableMapOf<String, Any>()
        handleToken(tokenRepository)?.let { map["current_user"] = it }

        if (user == null) {
            call.respond(HttpStatusCode.Unauthorized)
            return@get
        }

        val postId = call.parameters["id"]?.toLongOrNull()

        if (postId == null) {
            call.respond(HttpStatusCode.NotFound)
            return@get
        }

        val post = postRepository.view(postId)

        if (post == null) {
            call.respond(HttpStatusCode.NotFound)
            return@get
        }

        try {
            feedRepository.delete(post)
            postRepository.delete(post.id)
        } catch (e: Exception) {
            e.message?.let { map["return_message"] = it }
        }

        call.respondRedirect("/profile")
    }
}