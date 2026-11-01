package com.hadiyarajesh.kmptemplate.data.network

import com.hadiyarajesh.kmptemplate.data.database.entity.Image
import com.hadiyarajesh.kmptemplate.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

internal interface PicsumApi {
    suspend fun getImageList(page: Int, limit: Int): PicsumPage
}

@Inject
@ContributesBinding(AppScope::class)
internal class PicsumApiImpl(private val appHttpClient: AppHttpClient) : PicsumApi {
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

internal data class PicsumPage(val images: List<Image>, val hasNextPage: Boolean)

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
    fun toImage(): Image = Image(
        imageId = id,
        author = author,
        width = width,
        height = height,
        sourceUrl = url,
        downloadUrl = downloadUrl,
        description = "Photo by $author"
    )
}
