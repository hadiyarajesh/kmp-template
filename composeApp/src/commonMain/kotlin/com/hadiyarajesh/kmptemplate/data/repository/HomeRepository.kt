package com.hadiyarajesh.kmptemplate.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.hadiyarajesh.kmptemplate.data.database.dao.ImageDao
import com.hadiyarajesh.kmptemplate.data.database.entity.Image
import com.hadiyarajesh.kmptemplate.di.AppScope
import com.hadiyarajesh.kmptemplate.network.PicsumApi
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow

private const val IMAGE_PAGE_SIZE = 30

interface HomeRepository {
    fun getImages(): Flow<PagingData<Image>>
    suspend fun saveSelectedImage(image: Image)
}

@Inject
@SingleIn(AppScope::class)
class HomeRepositoryImpl(private val picsumApi: PicsumApi, private val imageDao: ImageDao) :
    HomeRepository {
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
