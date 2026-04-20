package com.hadiyarajesh.kmp_template.network

import com.hadiyarajesh.kmp_template.data.database.entity.Image
import dev.zacsweers.metro.Inject
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface PicsumApi {
    suspend fun getImageList(page: Int, limit: Int): PicsumPage
}

@Inject
class PicsumApiImpl(
    private val appHttpClient: AppHttpClient
) : PicsumApi {
    override suspend fun getImageList(page: Int, limit: Int): PicsumPage {
        val response = appHttpClient.client.get("v2/list") {
            parameter("page", page)
            parameter("limit", limit)
        }

        return PicsumPage(
            images = response.body<List<PicsumImageResponse>>().map { it.toImage() },
            hasNextPage = response.headers["Link"]?.contains("rel=\"next\"") == true
        )
    }
}

data class PicsumPage(
    val images: List<Image>,
    val hasNextPage: Boolean
)

@Serializable
private data class PicsumImageResponse(
    val id: String,
    val author: String,
    val width: Int,
    val height: Int,
    val url: String,
    @SerialName("download_url")
    val downloadUrl: String
) {
    fun toImage(): Image {
        return Image(
            imageId = id,
            author = author,
            width = width,
            height = height,
            sourceUrl = url,
            downloadUrl = downloadUrl,
            description = "Photo by $author",
            altText = "Failed to load image"
        )
    }
}
