package moe.crx.effect.database

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption.CASCADE
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object TokensTable : LongIdTable("tokens") {
    val userId = reference("user_id", UsersTable, CASCADE, CASCADE)
    val creationDate = timestamp("creation_date")
    val expireDate = timestamp("expire_date").nullable()
    val accessToken = varchar("access_token", 400).uniqueIndex()
}

class TokenEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<TokenEntity>(TokensTable)

    var user by UserEntity referencedOn TokensTable.userId
    var creationDate by TokensTable.creationDate
    var expireDate by TokensTable.expireDate
    var accessToken by TokensTable.accessToken
}