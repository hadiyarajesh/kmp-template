package com.hadiyarajesh.kmptemplate.data.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.hadiyarajesh.kmptemplate.data.database.entity.Image
import com.hadiyarajesh.kmptemplate.network.PicsumApi

private const val FIRST_PAGE = 1

class PicsumPagingSource(private val picsumApi: PicsumApi, private val pageSize: Int) :
    PagingSource<Int, Image>() {
    override fun getRefreshKey(state: PagingState<Int, Image>): Int? =
        state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Image> = try {
        val page = params.key ?: FIRST_PAGE
        val response = picsumApi.getImageList(page = page, limit = pageSize)
        val images = if (page == FIRST_PAGE) {
            // The first 10 Picsum records are mostly computer/desk images and make
            // the initial wallpaper grid feel repetitive. Keep the API page order,
            // but skip only those first records on the initial page.
            response.images.drop(10)
        } else {
            response.images
        }

        LoadResult.Page(
            data = images,
            prevKey = if (page == FIRST_PAGE) null else page - 1,
            nextKey = if (response.hasNextPage) page + 1 else null
        )
    } catch (e: Exception) {
        LoadResult.Error(e)
    }
}
