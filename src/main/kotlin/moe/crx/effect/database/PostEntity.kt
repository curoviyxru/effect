package moe.crx.effect.database

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.timestamp

object PostsTable : LongIdTable() {
    val title = varchar("title", 200)
    val previewText = varchar("preview_text", 1000).nullable()
    val fullText = varchar("full_text", 2000000)
    val imageId = reference("image_id", ImagesTable, ReferenceOption.SET_NULL, ReferenceOption.CASCADE).nullable()
    val creationDate = timestamp("creation_date")
    val viewCount = long("view_count")
    val category = varchar("category", 100).nullable()
    val lastEditDate = timestamp("last_edit_date").nullable()
}

class PostEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<PostEntity>(PostsTable)

    var title by PostsTable.title
    var previewText by PostsTable.previewText
    var fullText by PostsTable.fullText
    var imageId by ImageEntity optionalReferencedOn PostsTable.imageId
    var creationDate by PostsTable.creationDate
    var viewCount by PostsTable.viewCount
    var category by PostsTable.category
    var lastEditDate by PostsTable.lastEditDate
}