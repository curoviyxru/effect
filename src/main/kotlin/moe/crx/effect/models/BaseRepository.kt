package moe.crx.effect.models

interface BaseReadOnlyRepository<T> {
    suspend fun all(): List<T>
}

interface BaseRepository<T> : BaseReadOnlyRepository<T> {
    suspend fun create(value: T): T
    suspend fun update(value: T): T?
    suspend fun delete(id: Long): T?
}