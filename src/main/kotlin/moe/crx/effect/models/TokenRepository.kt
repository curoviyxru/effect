package moe.crx.effect.models

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import moe.crx.effect.database.TokenEntity
import moe.crx.effect.database.UserEntity
import moe.crx.effect.database.UsersTable
import moe.crx.effect.utils.generateToken
import moe.crx.effect.utils.hashPassword
import moe.crx.effect.utils.suspendTransaction

@Serializable
data class Token(
    @SerialName("user")
    var user: User,
    @SerialName("expire_date")
    var expireDate: Instant?,
    @SerialName("access_token")
    var accessToken: String,
)

fun TokenEntity.toModel() = Token(
    user = user.toModel(),
    expireDate = expireDate,
    accessToken = accessToken
)

interface TokenRepository {
    suspend fun authorize(username: String, password: String, expireDate: Instant? = null): Token
    suspend fun all(): List<Token>
}

class DatabaseTokenRepository : TokenRepository {
    override suspend fun authorize(username: String, password: String, expireDate: Instant?): Token {
        val user = suspendTransaction {
            UserEntity
                .find { UsersTable.username eq username }
                .limit(1)
                .firstOrNull()
        }

        if (user == null) {
            throw IllegalArgumentException("username is not registered")
        }

        if (user.passwordHash != hashPassword(username, password)) {
            throw IllegalArgumentException("wrong password")
        }

        return suspendTransaction {
            TokenEntity
                .new {
                    this.user = user
                    creationDate = Clock.System.now()
                    this.expireDate = expireDate
                    accessToken = generateToken(user.id.value)
                }
                .toModel()
        }
    }

    override suspend fun all(): List<Token> {
        return suspendTransaction {
            TokenEntity
                .all()
                .map(TokenEntity::toModel)
                .toList()
        }
    }
}