package moe.crx.effect.models

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import moe.crx.effect.database.CommentEntity
import moe.crx.effect.database.ImageEntity
import moe.crx.effect.database.PostEntity
import moe.crx.effect.database.UserEntity
import moe.crx.effect.utils.suspendTransaction

@Serializable
data class Comment(
    var id: Long = 0,
    var user: User = User(),
    var text: String? = null,
    var image: Image? = null,
    var post: Post = Post(),
    @SerialName("creation_date")
    var creationDate: Instant = Clock.System.now(),
    @SerialName("last_edit_date")
    var lastEditDate: Instant? = null,
)

fun CommentEntity.toModel() = Comment(
    id = id.value,
    user = user.toModel(),
    text = text,
    image = image?.toModel(),
    post = post.toModel(),
    creationDate = creationDate,
    lastEditDate = lastEditDate,
)

interface CommentRepository : BaseRepository<Comment>

class DatabaseCommentRepository : CommentRepository {
    override suspend fun all(): List<Comment> {
        return suspendTransaction {
            CommentEntity
                .all()
                .map(CommentEntity::toModel)
                .toList()
        }
    }

    override suspend fun update(value: Comment): Comment? {
        return suspendTransaction {
            CommentEntity
                .findByIdAndUpdate(value.id) {
                    it.user = UserEntity[value.user.id]
                    it.text = value.text
                    it.image = value.image?.run { ImageEntity[id] }
                    it.post = PostEntity[value.post.id]
                    it.creationDate = value.creationDate
                    it.lastEditDate = value.lastEditDate
                }
                ?.toModel()
        }
    }

    override suspend fun create(value: Comment): Comment {
        return suspendTransaction {
            CommentEntity
                .new {
                    user = UserEntity[value.user.id]
                    text = value.text
                    image = value.image?.run { ImageEntity[id] }
                    post = PostEntity[value.post.id]
                    creationDate = value.creationDate
                    lastEditDate = value.lastEditDate
                }
                .toModel()
        }
    }

    override suspend fun delete(id: Long): Comment? {
        var value: Comment? = null

        suspendTransaction {
            CommentEntity
                .findById(id)
                .also { value = it?.toModel() }
                ?.delete()
        }

        return value
    }
}
