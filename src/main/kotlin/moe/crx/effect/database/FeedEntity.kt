package moe.crx.effect.database

import org.jetbrains.exposed.dao.CompositeEntity
import org.jetbrains.exposed.dao.CompositeEntityClass
import org.jetbrains.exposed.dao.id.CompositeID
import org.jetbrains.exposed.dao.id.CompositeIdTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ReferenceOption.CASCADE

object FeedsTable : CompositeIdTable() {
    val userId = reference("user_id", UsersTable, CASCADE, CASCADE)
    val postId = reference("post_id", PostsTable, CASCADE, CASCADE)

    init {
        addIdColumn(userId)
        addIdColumn(postId)
    }

    override val primaryKey = PrimaryKey(userId, postId)
}

class FeedEntity(id: EntityID<CompositeID>) : CompositeEntity(id) {
    companion object : CompositeEntityClass<FeedEntity>(FeedsTable)

    var user by UserEntity referencedOn FeedsTable.userId
    var post by PostEntity referencedOn FeedsTable.postId
}