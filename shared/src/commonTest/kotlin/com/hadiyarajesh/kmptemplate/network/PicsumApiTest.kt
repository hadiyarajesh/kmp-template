package com.hadiyarajesh.kmptemplate.network

import com.hadiyarajesh.kmptemplate.testutil.mockHttpClient
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class PicsumApiTest {
    private val json = """
        [{"id":"7","author":"Jane","width":100,"height":200,
        "url":"https://picsum.photos/7","download_url":"https://picsum.photos/id/7/100/200",
        "unknown":"ignored"}]
    """.trimIndent()

    @Test
    fun mapsResponseAndSendsPagingParameters() = runTest {
        var requestUrl = ""
        val api = PicsumApiImpl(
            mockHttpClient { request ->
                requestUrl = request.url.toString()
                respond(
                    content = json,
                    headers = headersOf(
                        HttpHeaders.ContentType to listOf("application/json"),
                        HttpHeaders.Link to
                            listOf("<https://picsum.photos/v2/list?page=3>; rel=\"next\"")
                    )
                )
            }
        )

        val page = api.getImageList(page = 2, limit = 30)

        assertTrue(requestUrl.contains("v2/list"))
        assertTrue(requestUrl.contains("page=2") && requestUrl.contains("limit=30"))
        assertTrue(page.hasNextPage)
        val image = page.images.single()
        assertEquals("7", image.imageId)
        assertEquals("Jane", image.author)
        assertEquals("https://picsum.photos/id/7/100/200", image.downloadUrl)
    }

    @Test
    fun hasNextPageIsFalseWithoutNextLink() = runTest {
        val api = PicsumApiImpl(
            mockHttpClient {
                respond(
                    content = json,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
        )

        assertFalse(api.getImageList(page = 1, limit = 30).hasNextPage)
    }

    @Test
    fun serverErrorThrows() = runTest {
        val api = PicsumApiImpl(mockHttpClient { respond("", HttpStatusCode.InternalServerError) })

        assertFailsWith<Exception> { api.getImageList(page = 1, limit = 30) }
    }
}
