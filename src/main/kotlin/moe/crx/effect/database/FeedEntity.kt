package moe.crx.effect.database

import org.jetbrains.exposed.dao.CompositeEntity
import org.jetbrains.exposed.dao.CompositeEntityClass
import org.jetbrains.exposed.dao.id.CompositeID
import org.jetbrains.exposed.dao.id.CompositeIdTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ReferenceOption

object FeedsTable : CompositeIdTable() {
    val userId = reference("user_id", UsersTable, ReferenceOption.CASCADE, ReferenceOption.CASCADE)
    val postId = reference("post_id", PostsTable, ReferenceOption.CASCADE, ReferenceOption.CASCADE)

    init {
        addIdColumn(userId)
        addIdColumn(postId)
    }

    override val primaryKey = PrimaryKey(userId, postId)
}

class FeedEntity(id: EntityID<CompositeID>) : CompositeEntity(id) {
    companion object : CompositeEntityClass<FeedEntity>(FeedsTable)

    var userId by UserEntity referencedOn FeedsTable.userId
    var postId by PostEntity referencedOn FeedsTable.postId
}