package com.hadiyarajesh.kmptemplate.testutil

import androidx.paging.PagingData
import com.hadiyarajesh.kmptemplate.data.database.dao.ImageDao
import com.hadiyarajesh.kmptemplate.data.database.entity.Image
import com.hadiyarajesh.kmptemplate.data.download.ImageSaveResult
import com.hadiyarajesh.kmptemplate.data.download.ImageSaver
import com.hadiyarajesh.kmptemplate.data.network.AppHttpClient
import com.hadiyarajesh.kmptemplate.data.repository.ImageRepository
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal fun testImage(index: Int = 1): Image = Image(
    imageId = index.toString(),
    author = "Author $index",
    width = 1080,
    height = 1920,
    sourceUrl = "https://example.com/source/$index",
    downloadUrl = "https://example.com/download/$index",
    description = "Description $index"
)

internal fun mockHttpClient(handler: MockRequestHandler): AppHttpClient =
    AppHttpClient(MockEngine(handler))

internal class FakeImageDao : ImageDao {
    var stored: Image? = null
        private set
    var upsertCount = 0
        private set

    override suspend fun insertOrUpdateImage(image: Image) {
        upsertCount++
        stored = image
    }
}

internal class FakeImageSaver(var result: ImageSaveResult = ImageSaveResult.Success) : ImageSaver {
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

internal class FakeImageRepository : ImageRepository {
    var getImagesCalls = 0
    var getImagesError: Exception? = null
    val saved = mutableListOf<Image>()

    override fun getImages(): Flow<PagingData<Image>> {
        getImagesCalls++
        getImagesError?.let { throw it }
        return flowOf(PagingData.empty())
    }

    override suspend fun saveSelectedImage(image: Image) {
        saved += image
    }
}
