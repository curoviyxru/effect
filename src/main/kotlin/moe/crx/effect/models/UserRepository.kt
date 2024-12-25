package moe.crx.effect.models

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import moe.crx.effect.database.ImageEntity
import moe.crx.effect.database.UserEntity
import moe.crx.effect.database.UsersTable
import moe.crx.effect.utils.compareDate
import moe.crx.effect.utils.formatDateTimeString
import moe.crx.effect.utils.hashPassword
import moe.crx.effect.utils.suspendTransaction
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll

@Serializable
data class User(
    var id: Long = 0,
    @SerialName("full_name")
    var fullName: String? = null,
    var username: String = "",
    @SerialName("creation_date")
    var creationDate: Instant = Clock.System.now(),
    var about: String? = null,
    var image: Image? = null,
    var creationDateString: String = formatDateTimeString(creationDate),
)

fun UserEntity.toModel() = User(
    id = id.value,
    fullName = fullName,
    username = username,
    creationDate = creationDate,
    about = about,
    image = image?.toModel()
)

interface UserRepository : BaseRepository<User> {
    suspend fun count(date: Instant? = null): Long
    suspend fun create(value: User, password: String?): User
    suspend fun update(value: User, password: String?): User?
    suspend fun getByUsername(username: String): User?
}

class DatabaseUserRepository : UserRepository {
    override suspend fun count(date: Instant?): Long {
        return suspendTransaction {
            val query = (UsersTable).selectAll()

            date?.let {
                query.andWhere {
                    compareDate(UsersTable.creationDate, date)
                }
            }

            query
                .count()
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

    override suspend fun update(value: User): User? {
        return update(value, null)
    }

    override suspend fun update(value: User, password: String?): User? {
        return suspendTransaction {
            UserEntity
                .findByIdAndUpdate(value.id) {
                    it.fullName = value.fullName
                    it.username = value.username
                    it.creationDate = value.creationDate
                    it.about = value.about
                    it.image = value.image?.run { ImageEntity[id] }
                    if (password != null) it.passwordHash = hashPassword(value.username, password)
                }
                ?.toModel()
        }
    }

    override suspend fun create(value: User): User {
        return create(value, null)
    }

    override suspend fun create(value: User, password: String?): User {
        return suspendTransaction {
            UserEntity
                .new {
                    fullName = value.fullName
                    username = value.username
                    creationDate = value.creationDate
                    about = value.about
                    image = value.image?.run { ImageEntity[id] }
                    passwordHash = if (password != null) hashPassword(value.username, password) else ""
                }
                .toModel()
        }
    }

    override suspend fun delete(id: Long): User? {
        var value: User? = null

        suspendTransaction {
            UserEntity
                .findById(id)
                .also { value = it?.toModel() }
                ?.delete()
        }

        return value
    }

    override suspend fun getByUsername(username: String): User? {
        return suspendTransaction {
            UserEntity
                .find { UsersTable.username eq username }
                .limit(1)
                .map(UserEntity::toModel)
                .firstOrNull()
        }
    }
}