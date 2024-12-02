package moe.crx.effect.models

import kotlinx.serialization.Serializable
import moe.crx.effect.database.FeedEntity
import moe.crx.effect.utils.suspendTransaction

@Serializable
data class Feed(
    var user: User,
    var post: Post,
)

fun FeedEntity.toModel() = Feed(
    user = user.toModel(),
    post = post.toModel()
)

interface FeedRepository {
    suspend fun all(): List<Feed>
}

class DatabaseFeedRepository : FeedRepository {
    override suspend fun all(): List<Feed> {
        return suspendTransaction {
            FeedEntity
                .all()
                .map(FeedEntity::toModel)
                .toList()
        }
    }
}
