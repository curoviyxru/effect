package moe.crx.effect.models

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import moe.crx.effect.database.FeedEntity
import moe.crx.effect.database.FeedsTable
import moe.crx.effect.database.PostEntity
import moe.crx.effect.database.PostsTable
import moe.crx.effect.database.UserEntity
import moe.crx.effect.database.UsersTable
import moe.crx.effect.utils.compareDate
import moe.crx.effect.utils.suspendTransaction
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll

@Serializable
data class Feed(
    var user: User = User(),
    var post: Post = Post(),
)

fun FeedEntity.toModel() = Feed(
    user = user.toModel(),
    post = post.toModel()
)

interface FeedRepository : BaseReadOnlyRepository<Feed> {
    suspend fun query(offset: Long, limit: Int, username: String? = null, titleContains: String? = null, category: String? = null, minViews: Long? = null, maxViews: Long? = null, date: Instant? = null, textContains: String? = null): List<Feed>
    suspend fun count(username: String? = null, titleContains: String? = null, category: String? = null, minViews: Long? = null, maxViews: Long? = null, date: Instant? = null, textContains: String? = null): Long
    suspend fun get(post: Post): Feed
    suspend fun create(user: User, post: Post): Feed
    suspend fun delete(post: Post)
}

class DatabaseFeedRepository : FeedRepository {
    override suspend fun all(): List<Feed> {
        return suspendTransaction {
            FeedEntity
                .all()
                .map(FeedEntity::toModel)
                .toList()
        }
    }

    override suspend fun query(offset: Long, limit: Int, username: String?, titleContains: String?, category: String?, minViews: Long?, maxViews: Long?, date: Instant?, textContains: String?): List<Feed> {
        return suspendTransaction {
            val query = (FeedsTable innerJoin UsersTable innerJoin PostsTable)
                .selectAll()
                .orderBy(FeedsTable.postId to SortOrder.DESC)

            username?.let {
                query.andWhere {
                    UsersTable.username eq username
                }
            }
            titleContains?.let {
                query.andWhere {
                    PostsTable.title like "%$titleContains%"
                }
            }
            category?.let {
                query.andWhere {
                    PostsTable.category eq category
                }
            }
            minViews?.let {
                query.andWhere {
                    PostsTable.viewCount greaterEq minViews
                }
            }
            maxViews?.let {
                query.andWhere {
                    PostsTable.viewCount lessEq maxViews
                }
            }
            date?.let {
                query.andWhere {
                    compareDate(PostsTable.creationDate, date)
                }
            }
            textContains?.let {
                query.andWhere {
                    (PostsTable.previewText like "%$textContains%").or(PostsTable.fullText like "%$textContains%")
                }
            }

            query
                .offset(offset)
                .limit(limit)
                .map {
                    Feed(UserEntity[it[FeedsTable.userId]].toModel(),
                        PostEntity[it[FeedsTable.postId]].toModel())
                }
                .toList()
        }
    }

    override suspend fun count(username: String?, titleContains: String?, category: String?, minViews: Long?, maxViews: Long?, date: Instant?, textContains: String?): Long {
        return suspendTransaction {
            val query = (FeedsTable innerJoin UsersTable innerJoin PostsTable)
                .selectAll()
                .orderBy(FeedsTable.postId to SortOrder.DESC)

            username?.let {
                query.andWhere {
                    UsersTable.username eq username
                }
            }
            titleContains?.let {
                query.andWhere {
                    PostsTable.title like "%$titleContains%"
                }
            }
            category?.let {
                query.andWhere {
                    PostsTable.category eq category
                }
            }
            minViews?.let {
                query.andWhere {
                    PostsTable.viewCount greaterEq minViews
                }
            }
            maxViews?.let {
                query.andWhere {
                    PostsTable.viewCount lessEq maxViews
                }
            }
            date?.let {
                query.andWhere {
                    compareDate(PostsTable.creationDate, date)
                }
            }
            textContains?.let {
                query.andWhere {
                    (PostsTable.previewText like "%$textContains%").or(PostsTable.fullText like "%$textContains%")
                }
            }

            query
                .count()
        }
    }

    override suspend fun get(post: Post): Feed {
        return suspendTransaction {
            FeedEntity
                .find { FeedsTable.postId eq post.id }
                .limit(1)
                .first()
                .toModel()
        }
    }

    override suspend fun create(
        user: User,
        post: Post
    ): Feed {
        return suspendTransaction {
            FeedEntity
                .new {
                    this.user = UserEntity[user.id]
                    this.post = PostEntity[post.id]
                }.toModel()
        }
    }

    override suspend fun delete(post: Post) {
        suspendTransaction {
            FeedEntity
                .find { FeedsTable.postId eq post.id }
                .limit(1)
                .first()
                .delete()
        }
    }
}
