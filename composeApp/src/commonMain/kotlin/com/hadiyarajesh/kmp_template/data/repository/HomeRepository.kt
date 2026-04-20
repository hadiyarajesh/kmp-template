package com.hadiyarajesh.kmp_template.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.hadiyarajesh.kmp_template.data.database.dao.ImageDao
import com.hadiyarajesh.kmp_template.data.database.entity.Image
import com.hadiyarajesh.kmp_template.datastore.DataStoreManager
import com.hadiyarajesh.kmp_template.di.AppScope
import com.hadiyarajesh.kmp_template.network.PicsumApi
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow

private const val ImagePageSize = 30

interface HomeRepository {
    fun getImages(): Flow<PagingData<Image>>
    suspend fun saveSelectedImage(image: Image)
}

@Inject
@SingleIn(AppScope::class)
class HomeRepositoryImpl(
    private val picsumApi: PicsumApi,
    private val imageDao: ImageDao,
    private val dataStoreManager: DataStoreManager
) : HomeRepository {
    override fun getImages(): Flow<PagingData<Image>> = Pager(
        config = PagingConfig(
            pageSize = ImagePageSize,
            initialLoadSize = ImagePageSize,
            prefetchDistance = 10,
            enablePlaceholders = false
        ),
        pagingSourceFactory = { PicsumPagingSource(picsumApi, ImagePageSize) }
    ).flow

    override suspend fun saveSelectedImage(image: Image) {
        imageDao.insertOrUpdateImage(image)
    }
}
