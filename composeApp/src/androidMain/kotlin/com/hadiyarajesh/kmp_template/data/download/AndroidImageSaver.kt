package com.hadiyarajesh.kmp_template.data.download

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

@Inject
class AndroidImageSaver(
    private val context: Context
) : ImageSaver {
    override suspend fun saveImage(
        bytes: ByteArray,
        fileName: String,
        sourceUrl: String
    ): ImageSaveResult =
        withContext(Dispatchers.IO) {
            runCatching {
                val contentResolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(
                            MediaStore.Images.Media.RELATIVE_PATH,
                            Environment.DIRECTORY_PICTURES + "/KMPTemplate"
                        )
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }
                }

                val uri = contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                ) ?: return@withContext ImageSaveResult.Error("Unable to create image entry")

                try {
                    val outputStream = contentResolver.openOutputStream(uri)
                        ?: throw IOException("Unable to open output stream")
                    outputStream.use { stream ->
                        stream.write(bytes)
                        stream.flush()
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val finalizeValues = ContentValues().apply {
                            put(MediaStore.Images.Media.IS_PENDING, 0)
                        }
                        contentResolver.update(uri, finalizeValues, null, null)
                    }

                    ImageSaveResult.Success
                } catch (e: Exception) {
                    contentResolver.delete(uri, null, null)
                    throw e
                }
            }.getOrElse { throwable ->
                when (throwable) {
                    is SecurityException -> ImageSaveResult.PermissionDenied
                    else -> ImageSaveResult.Error(
                        throwable.message ?: "Unable to save image"
                    )
                }
            }
        }
}
