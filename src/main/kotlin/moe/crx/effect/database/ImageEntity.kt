package moe.crx.effect.database

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object ImagesTable : LongIdTable() {
    val url = varchar("url", 500)
    val width = integer("width")
    val height = integer("height")
    val fileSize = long("file_size")
    val creationDate = timestamp("creation_date")
}

class ImageEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<ImageEntity>(ImagesTable)

    var url by ImagesTable.url
    var width by ImagesTable.width
    var height by ImagesTable.height
    var fileSize by ImagesTable.fileSize
    var creationDate by ImagesTable.creationDate
}