package moe.crx.effect.models

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import moe.crx.effect.database.PostEntity
import kotlin.Long

@Serializable
data class Post(
    var id: Long,
    var title: String,
    @SerialName("preview_text")
    var previewText: String?,
    @SerialName("full_text")
    var fullText: String,
    var image: Image?,
    @SerialName("creation_date")
    var creationDate: Instant,
    @SerialName("view_count")
    var viewCount: Long,
    var category: String?,
    @SerialName("last_edit_date")
    var lastEditDate: Instant?,
)

fun PostEntity.toModel() = Post(
    id = id.value,
    title = title,
    previewText = previewText,
    fullText = fullText,
    image = image?.toModel(),
    creationDate = creationDate,
    viewCount = viewCount,
    category = category,
    lastEditDate = lastEditDate,
)