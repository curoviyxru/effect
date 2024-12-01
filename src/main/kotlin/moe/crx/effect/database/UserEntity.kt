package moe.crx.effect.database

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.timestamp

object UsersTable : LongIdTable() {
    val fullName = varchar("full_name", 100).nullable()
    val username = varchar("username", 40).uniqueIndex()
    val registerDate = timestamp("register_date")
    val about = varchar("about", 1000).nullable()
    val imageId = reference("image_id", ImagesTable, ReferenceOption.SET_NULL, ReferenceOption.CASCADE).nullable()
    val passwordHash = varchar("password_hash", 100)
}

class UserEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<UserEntity>(UsersTable)

    var fullName by UsersTable.fullName
    var username by UsersTable.username
    var registerDate by UsersTable.registerDate
    var about by UsersTable.about
    var imageId by ImageEntity optionalReferencedOn UsersTable.imageId
    var passwordHash by UsersTable.passwordHash
}