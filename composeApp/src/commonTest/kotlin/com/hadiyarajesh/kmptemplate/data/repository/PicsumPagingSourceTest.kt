package com.hadiyarajesh.kmptemplate.data.repository

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.hadiyarajesh.kmptemplate.data.database.entity.Image
import com.hadiyarajesh.kmptemplate.network.PicsumApi
import com.hadiyarajesh.kmptemplate.network.PicsumPage
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

class PicsumPagingSourceTest {
    @Test
    fun firstPageSkipsFirstTenItems() = runBlocking {
        val source = createPagingSource(
            items = (1..30).map(::image),
            hasNextPage = true
        )

        val result = source.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = PAGE_SIZE,
                placeholdersEnabled = false
            )
        )

        val page = assertIs<PagingSource.LoadResult.Page<Int, Image>>(result)
        assertEquals((11..30).map(::image), page.data)
        assertEquals(null, page.prevKey)
        assertEquals(2, page.nextKey)
    }

    @Test
    fun nonFirstPageKeepsItemsAndUsesExpectedKeys() = runBlocking {
        val source = createPagingSource(
            items = (31..40).map(::image),
            hasNextPage = false
        )

        val result = source.load(
            PagingSource.LoadParams.Refresh(
                key = 2,
                loadSize = PAGE_SIZE,
                placeholdersEnabled = false
            )
        )

        val page = assertIs<PagingSource.LoadResult.Page<Int, Image>>(result)
        assertEquals((31..40).map(::image), page.data)
        assertEquals(1, page.prevKey)
        assertEquals(null, page.nextKey)
    }

    @Test
    fun loadReturnsErrorWhenApiFails() = runBlocking {
        val expected = IllegalStateException("boom")
        val source = PicsumPagingSource(
            picsumApi = FakePicsumApi(error = expected),
            pageSize = PAGE_SIZE
        )

        val result = source.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = PAGE_SIZE,
                placeholdersEnabled = false
            )
        )

        val error = assertIs<PagingSource.LoadResult.Error<Int, Image>>(result)
        assertSame(expected, error.throwable)
    }

    @Test
    fun refreshKeyUsesClosestPage() {
        val source = createPagingSource(items = emptyList(), hasNextPage = false)
        val state = PagingState(
            pages = listOf(
                PagingSource.LoadResult.Page(
                    data = listOf(image(1)),
                    prevKey = null,
                    nextKey = 2
                ),
                PagingSource.LoadResult.Page(
                    data = listOf(image(2)),
                    prevKey = 1,
                    nextKey = 3
                )
            ),
            anchorPosition = 1,
            config = PagingConfig(pageSize = PAGE_SIZE),
            leadingPlaceholderCount = 0
        )

        assertEquals(2, source.getRefreshKey(state))
    }

    private fun createPagingSource(items: List<Image>, hasNextPage: Boolean): PicsumPagingSource =
        PicsumPagingSource(
            picsumApi = FakePicsumApi(
                page = PicsumPage(images = items, hasNextPage = hasNextPage)
            ),
            pageSize = PAGE_SIZE
        )

    private fun image(index: Int): Image = Image(
        imageId = index.toString(),
        author = "Author $index",
        width = 1080,
        height = 1920,
        sourceUrl = "https://example.com/source/$index",
        downloadUrl = "https://example.com/download/$index",
        description = "Description $index",
        altText = "Alt $index"
    )

    private companion object {
        const val PAGE_SIZE = 30
    }
}

private class FakePicsumApi(
    private val page: PicsumPage? = null,
    private val error: Exception? = null
) : PicsumApi {
    override suspend fun getImageList(page: Int, limit: Int): PicsumPage {
        error?.let { throw it }
        return this.page ?: error("FakePicsumApi requires either page or error")
    }
}
