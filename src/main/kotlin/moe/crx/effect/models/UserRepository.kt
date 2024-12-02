package moe.crx.effect.models

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import moe.crx.effect.database.ImageEntity
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
    id = id.value,
    fullName = fullName,
    username = username,
    registerDate = registerDate,
    about = about,
    image = image?.toModel()
)

interface UserRepository {
    suspend fun getByUsername(username: String): User?
    suspend fun register(username: String, password: String): User
    suspend fun all(): List<User>
    suspend fun update(user: User): User?
    suspend fun create(user: User, password: String? = null): User
    suspend fun delete(id: Long): User?
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

    override suspend fun all(): List<User> {
        return suspendTransaction {
            UserEntity
                .all()
                .map(UserEntity::toModel)
                .toList()
        }
    }

    override suspend fun update(user: User): User? {
        return suspendTransaction {
            UserEntity
                .findByIdAndUpdate(user.id) {
                    it.fullName = user.fullName
                    it.username = user.username
                    it.registerDate = user.registerDate
                    it.about = user.about
                    it.image = user.image?.let { ImageEntity[it.id] }
                }
                ?.toModel()
        }
    }

    override suspend fun create(user: User, password: String?): User {
        return suspendTransaction {
            UserEntity
                .new {
                    fullName = user.fullName
                    username = user.username
                    registerDate = user.registerDate
                    about = user.about
                    image = user.image?.let { ImageEntity[it.id] }
                    passwordHash = hashPassword(user.username, password ?: user.username)
                }
                .toModel()
        }
    }

    override suspend fun delete(id: Long): User? {
        var user: User? = null

        suspendTransaction {
            UserEntity
                .findById(id)
                .also { user = it?.toModel() }
                ?.delete()
        }

        return user
    }
}