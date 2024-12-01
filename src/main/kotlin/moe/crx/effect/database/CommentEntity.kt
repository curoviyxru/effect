package moe.crx.effect.database

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.timestamp

object CommentsTable : LongIdTable() {
    val userId = reference("user_id", UsersTable, ReferenceOption.CASCADE, ReferenceOption.CASCADE)
    val text = varchar("text", 4000).nullable()
    val imageId = reference("image_id", ImagesTable, ReferenceOption.SET_NULL, ReferenceOption.CASCADE).nullable()
    val postId = reference("post_id", PostsTable, ReferenceOption.CASCADE, ReferenceOption.CASCADE)
    val creationDate = timestamp("creation_date")
}

class CommentEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<CommentEntity>(CommentsTable)

    var userId by UserEntity referencedOn CommentsTable.userId
    var text by CommentsTable.text
    var imageId by ImageEntity optionalReferencedOn CommentsTable.imageId
    var postId by PostEntity referencedOn CommentsTable.postId
    var creationDate by CommentsTable.creationDate
}