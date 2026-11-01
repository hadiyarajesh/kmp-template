package com.hadiyarajesh.kmptemplate.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.hadiyarajesh.kmptemplate.data.database.dao.ImageDao
import com.hadiyarajesh.kmptemplate.data.database.entity.Image
import com.hadiyarajesh.kmptemplate.data.network.PicsumApi
import com.hadiyarajesh.kmptemplate.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow

private const val IMAGE_PAGE_SIZE = 30

internal interface ImageRepository {
    fun getImages(): Flow<PagingData<Image>>
    suspend fun saveSelectedImage(image: Image)
}

@Inject
@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
internal class ImageRepositoryImpl(
    private val picsumApi: PicsumApi,
    private val imageDao: ImageDao
) : ImageRepository {
    override fun getImages(): Flow<PagingData<Image>> = Pager(
        config = PagingConfig(
            pageSize = IMAGE_PAGE_SIZE,
            initialLoadSize = IMAGE_PAGE_SIZE,
            prefetchDistance = 10,
            enablePlaceholders = false
        ),
        pagingSourceFactory = { PicsumPagingSource(picsumApi, IMAGE_PAGE_SIZE) }
    ).flow

    override suspend fun saveSelectedImage(image: Image) {
        imageDao.insertOrUpdateImage(image)
    }
}
