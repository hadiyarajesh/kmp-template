package com.hadiyarajesh.kmp_template.data.download

import com.hadiyarajesh.kmp_template.data.database.entity.Image
import com.hadiyarajesh.kmp_template.network.AppHttpClient
import dev.zacsweers.metro.Inject
import io.ktor.client.call.body
import io.ktor.client.request.get

@Inject
class ImageDownloadManager(
    private val appHttpClient: AppHttpClient,
    private val imageSaver: ImageSaver
) {
    suspend fun downloadAndSave(image: Image): ImageSaveResult {
        return try {
            val imageBytes: ByteArray = appHttpClient.client.get(image.downloadUrl).body()
            imageSaver.saveImage(
                bytes = imageBytes,
                fileName = image.suggestedFileName(),
                sourceUrl = image.downloadUrl
            )
        } catch (e: Exception) {
            ImageSaveResult.Error(message = e.message ?: "Unable to download image")
        }
    }
}

private fun Image.suggestedFileName(): String {
    val safeId = imageId
        .ifBlank { downloadUrl.hashCode().toString() }
        .replace("[^A-Za-z0-9_-]".toRegex(), "_")
    return "picsum_${safeId}.jpg"
}
