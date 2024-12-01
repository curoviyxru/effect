package moe.crx.effect.database

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption.CASCADE
import org.jetbrains.exposed.sql.ReferenceOption.SET_NULL
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object CommentsTable : LongIdTable() {
    val userId = reference("user_id", UsersTable, CASCADE, CASCADE)
    val text = varchar("text", 4_000).nullable()
    val imageId = reference("image_id", ImagesTable, SET_NULL, CASCADE).nullable()
    val postId = reference("post_id", PostsTable, CASCADE, CASCADE)
    val creationDate = timestamp("creation_date")
}

class CommentEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<CommentEntity>(CommentsTable)

    var user by UserEntity referencedOn CommentsTable.userId
    var text by CommentsTable.text
    var image by ImageEntity optionalReferencedOn CommentsTable.imageId
    var post by PostEntity referencedOn CommentsTable.postId
    var creationDate by CommentsTable.creationDate
}