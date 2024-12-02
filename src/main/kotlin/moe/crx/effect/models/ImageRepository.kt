package moe.crx.effect.models

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import moe.crx.effect.database.ImageEntity

@Serializable
data class Image(
    var id: Long,
    var url: String = "",
    var width: Int = 0,
    var height: Int = 0,
    @SerialName("file_size")
    var fileSize: Long = 0,
    @SerialName("creation_date")
    var creationDate : Instant = Instant.DISTANT_PAST,
)

fun ImageEntity.toModel() = Image(
    id = id.value,
    url = url,
    width = width,
    height = height,
    fileSize = fileSize,
    creationDate = creationDate
)