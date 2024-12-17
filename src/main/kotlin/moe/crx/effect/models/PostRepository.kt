package moe.crx.effect.models

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import moe.crx.effect.database.FeedsTable
import moe.crx.effect.database.ImageEntity
import moe.crx.effect.database.PostEntity
import moe.crx.effect.database.PostsTable
import moe.crx.effect.database.UsersTable
import moe.crx.effect.utils.compareDate
import moe.crx.effect.utils.suspendTransaction
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import kotlin.Long

@Serializable
data class Post(
    var id: Long = 0,
    var title: String = "",
    @SerialName("preview_text")
    var previewText: String? = null,
    @SerialName("full_text")
    var fullText: String = "",
    var image: Image? = null,
    @SerialName("creation_date")
    var creationDate: Instant = Clock.System.now(),
    @SerialName("view_count")
    var viewCount: Long = 0,
    var category: String? = null,
    @SerialName("last_edit_date")
    var lastEditDate: Instant? = null,
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

interface PostRepository : BaseRepository<Post> {
    suspend fun count(date: Instant? = null): Long
    suspend fun getById(id: Long): Post?
    suspend fun view(id: Long): Post?
}

class DatabasePostRepository : PostRepository {
    override suspend fun count(date: Instant?): Long {
        return suspendTransaction {
            val query = (PostsTable).selectAll()

            date?.let {
                query.andWhere {
                    compareDate(PostsTable.creationDate, date)
                }
            }

            query
                .count()
        }
    }

    override suspend fun all(): List<Post> {
        return suspendTransaction {
            PostEntity
                .all()
                .map(PostEntity::toModel)
                .toList()
        }
    }

    override suspend fun update(value: Post): Post? {
        return suspendTransaction {
            PostEntity
                .findByIdAndUpdate(value.id) {
                    it.title = value.title
                    it.previewText = value.previewText
                    it.fullText = value.fullText
                    it.image = value.image?.run { ImageEntity[id] }
                    it.creationDate = value.creationDate
                    it.viewCount = value.viewCount
                    it.category = value.category
                    it.lastEditDate = value.lastEditDate
                }
                ?.toModel()
        }
    }

    override suspend fun create(value: Post): Post {
        return suspendTransaction {
            PostEntity
                .new {
                    title = value.title
                    previewText = value.previewText
                    fullText = value.fullText
                    image = value.image?.run { ImageEntity[id] }
                    creationDate = value.creationDate
                    viewCount = value.viewCount
                    category = value.category
                    lastEditDate = value.lastEditDate
                }
                .toModel()
        }
    }

    override suspend fun delete(id: Long): Post? {
        var value: Post? = null

        suspendTransaction {
            PostEntity
                .findById(id)
                .also { value = it?.toModel() }
                ?.delete()
        }

        return value
    }

    override suspend fun getById(id: Long): Post? {
        return suspendTransaction {
            PostEntity
                .findById(id)
                ?.toModel()
        }
    }

    override suspend fun view(id: Long): Post? {
        return suspendTransaction {
            PostEntity
                .findByIdAndUpdate(id) {
                    it.viewCount = it.viewCount + 1
                }
                ?.toModel()
        }
    }
}