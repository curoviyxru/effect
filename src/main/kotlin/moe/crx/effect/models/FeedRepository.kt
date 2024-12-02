package moe.crx.effect.models

import kotlinx.serialization.Serializable
import moe.crx.effect.database.FeedEntity

@Serializable
data class Feed(
    var user: User,
    var post: Post,
)

fun FeedEntity.toModel() = Feed(
    user = user.toModel(),
    post = post.toModel()
)