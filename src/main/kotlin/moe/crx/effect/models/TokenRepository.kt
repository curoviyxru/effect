package moe.crx.effect.models

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import moe.crx.effect.database.TokenEntity
import moe.crx.effect.database.TokensTable
import moe.crx.effect.database.UserEntity
import moe.crx.effect.database.UsersTable
import moe.crx.effect.utils.generateToken
import moe.crx.effect.utils.hashPassword
import moe.crx.effect.utils.suspendTransaction

@Serializable
data class Token(
    @SerialName("user")
    var user: User = User(),
    @SerialName("access_token")
    var accessToken: String = "",
)

fun TokenEntity.toModel() = Token(
    user = user.toModel(),
    accessToken = accessToken
)

interface TokenRepository : BaseReadOnlyRepository<Token> {
    suspend fun authorize(username: String, password: String, expireDate: Instant? = null): Token
    suspend fun authorize(accessToken: String): Token?
}

class DatabaseTokenRepository : TokenRepository {
    override suspend fun all(): List<Token> {
        return suspendTransaction {
            TokenEntity
                .all()
                .map(TokenEntity::toModel)
                .toList()
        }
    }

    override suspend fun authorize(accessToken: String): Token? {
        return suspendTransaction {
            TokenEntity
                .find { TokensTable.accessToken eq accessToken }
                .limit(1)
                .map(TokenEntity::toModel)
                .firstOrNull()
        }
    }

    override suspend fun authorize(username: String, password: String, expireDate: Instant?): Token {
        val user = suspendTransaction {
            UserEntity
                .find { UsersTable.username eq username }
                .limit(1)
                .firstOrNull()
        }

        if (user == null) {
            throw IllegalArgumentException("Username is not registered.")
        }

        if (user.passwordHash != hashPassword(username, password)) {
            throw IllegalArgumentException("Wrong password.")
        }

        return suspendTransaction {
            TokenEntity
                .new {
                    this.user = user
                    creationDate = Clock.System.now()
                    accessToken = generateToken(user.id.value)
                }
                .toModel()
        }
    }
}