package moe.crx.effect.models

import io.ktor.http.content.PartData
import io.ktor.utils.io.availableForRead
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.datetime.Clock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import moe.crx.effect.database.ImageEntity
import moe.crx.effect.utils.suspendTransaction
import java.io.File
import java.util.UUID
import javax.imageio.ImageIO

@Serializable
data class Image(
    var id: Long = 0,
    var url: String = "",
    var width: Int = 0,
    var height: Int = 0,
    @SerialName("file_size")
    var fileSize: Long = 0,
)

fun ImageEntity.toModel() = Image(
    id = id.value,
    url = url,
    width = width,
    height = height,
    fileSize = fileSize,
)

interface ImageRepository : BaseRepository<Image> {
    suspend fun upload(data: PartData.FileItem): Image?
}

class DatabaseImageRepository : ImageRepository {
    override suspend fun all(): List<Image> {
        return suspendTransaction {
            ImageEntity
                .all()
                .map(ImageEntity::toModel)
                .toList()
        }
    }

    override suspend fun update(value: Image): Image? {
        return suspendTransaction {
            ImageEntity
                .findByIdAndUpdate(value.id) {
                    it.url = value.url
                    it.width = value.width
                    it.height = value.height
                    it.fileSize = value.fileSize
                }
                ?.toModel()
        }
    }

    override suspend fun create(value: Image): Image {
        return suspendTransaction {
            ImageEntity
                .new {
                    url = value.url
                    width = value.width
                    height = value.height
                    fileSize = value.fileSize
                }
                .toModel()
        }
    }

    override suspend fun delete(id: Long): Image? {
        var value: Image? = null

        suspendTransaction {
            ImageEntity
                .findById(id)
                .also { value = it?.toModel() }
                ?.delete()
        }

        return value
    }

    override suspend fun upload(data: PartData.FileItem): Image? {
        val provider = data.provider()

        if (provider.availableForRead > 20 * 1024 * 1024) {
            throw IllegalArgumentException("Image is bigger than 20 MiB.")
        }

        val stream = provider.toInputStream()
        val image = ImageIO.read(stream)

        if (image == null) {
            return null
        }

        val directory = File("uploads")
        directory.mkdirs()
        val fileName = Clock.System.now().toEpochMilliseconds().toString() + "_" + UUID.randomUUID().toString() + ".png"
        val file = File(directory, fileName)
        file.createNewFile()

        val width = image.width
        val height = image.height
        ImageIO.write(image, "png", file)
        val fileSize = file.length()

        return create(Image(
            url = "/uploads/$fileName",
            width = width,
            height = height,
            fileSize = fileSize
        ))
    }
}
