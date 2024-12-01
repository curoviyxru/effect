package moe.crx.effect.models

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import moe.crx.effect.database.ImageEntity

@Serializable
data class Image(
    var id: Long,
    var url: String,
    var width: Int,
    var height: Int,
    @SerialName("file_size")
    var fileSize: Long,
    @SerialName("creation_date")
    var creationDate : Instant,
)

fun ImageEntity.toModel() = Image(
    id.value,
    url,
    width,
    height,
    fileSize,
    creationDate
)