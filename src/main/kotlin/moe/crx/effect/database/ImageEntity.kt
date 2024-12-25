package moe.crx.effect.database

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

object ImagesTable : LongIdTable("images") {
    val url = varchar("url", 500)
    val width = integer("width")
    val height = integer("height")
    val fileSize = long("file_size")
}

class ImageEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<ImageEntity>(ImagesTable)

    var url by ImagesTable.url
    var width by ImagesTable.width
    var height by ImagesTable.height
    var fileSize by ImagesTable.fileSize
}