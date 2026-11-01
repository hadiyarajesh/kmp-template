package com.hadiyarajesh.kmptemplate.testutil

import com.hadiyarajesh.kmptemplate.data.database.dao.ImageDao
import com.hadiyarajesh.kmptemplate.data.database.entity.Image
import com.hadiyarajesh.kmptemplate.data.download.ImageSaveResult
import com.hadiyarajesh.kmptemplate.data.download.ImageSaver
import com.hadiyarajesh.kmptemplate.network.AppHttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

fun testImage(index: Int = 1): Image = Image(
    imageId = index.toString(),
    author = "Author $index",
    width = 1080,
    height = 1920,
    sourceUrl = "https://example.com/source/$index",
    downloadUrl = "https://example.com/download/$index",
    description = "Description $index",
    altText = "Alt $index"
)

fun mockHttpClient(handler: MockRequestHandler): AppHttpClient = AppHttpClient(MockEngine(handler))

class FakeImageDao : ImageDao {
    private val stored = MutableStateFlow<Image?>(null)
    var upsertCount = 0
        private set

    override suspend fun insertOrUpdateImage(image: Image) {
        upsertCount++
        stored.value = image
    }

    override fun getImage(): Flow<Image?> = stored

    override suspend fun imageCount(): Int = if (stored.value == null) 0 else 1
}

class FakeImageSaver(var result: ImageSaveResult = ImageSaveResult.Success) : ImageSaver {
    val savedFileNames = mutableListOf<String>()

    override suspend fun saveImage(
        bytes: ByteArray,
        fileName: String,
        sourceUrl: String
    ): ImageSaveResult {
        savedFileNames += fileName
        return result
    }
}
