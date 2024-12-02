package moe.crx.effect.models

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import moe.crx.effect.database.CommentEntity

@Serializable
data class Comment(
    var id: Long,
    var user: User,
    var text: String?,
    var image: Image?,
    var post: Post,
    @SerialName("creation_date")
    var creationDate: Instant,
    @SerialName("last_edit_date")
    var lastEditDate: Instant?,
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