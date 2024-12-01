package moe.crx.effect.models

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import moe.crx.effect.database.UserEntity
import moe.crx.effect.database.UsersTable
import moe.crx.effect.utils.hashPassword
import moe.crx.effect.utils.suspendTransaction

@Serializable
data class User(
    var id: Long,
    @SerialName("full_name")
    var fullName: String?,
    var username: String,
    @SerialName("register_date")
    var registerDate: Instant,
    var about: String?,
    var image: Image?,
)

fun UserEntity.toModel() = User(
    id.value,
    fullName,
    username,
    registerDate,
    about,
    image?.toModel()
)

interface UserRepository {
    suspend fun getByUsername(username: String): User?
    suspend fun register(username: String, password: String): User
}

class DatabaseUserRepository : UserRepository {
    override suspend fun getByUsername(username: String): User? {
        return suspendTransaction {
            UserEntity
                .find { UsersTable.username eq username }
                .limit(1)
                .map(UserEntity::toModel)
                .firstOrNull()
        }
    }

    override suspend fun register(username: String, password: String): User {
        if (getByUsername(username) != null) {
            throw IllegalArgumentException("username already registered")
        }

        return suspendTransaction {
            UserEntity
                .new {
                    this.username = username
                    registerDate = Clock.System.now()
                    passwordHash = hashPassword(username, password)
                }
                .toModel()
        }
    }
}