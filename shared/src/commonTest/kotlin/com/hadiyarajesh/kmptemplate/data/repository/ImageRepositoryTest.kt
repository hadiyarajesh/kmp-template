package com.hadiyarajesh.kmptemplate.data.repository

import com.hadiyarajesh.kmptemplate.data.network.PicsumApi
import com.hadiyarajesh.kmptemplate.data.network.PicsumPage
import com.hadiyarajesh.kmptemplate.testutil.FakeImageDao
import com.hadiyarajesh.kmptemplate.testutil.testImage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

internal class ImageRepositoryTest {
    @Test
    fun saveSelectedImagePersistsToDao() = runTest {
        val dao = FakeImageDao()
        val repository = ImageRepositoryImpl(NoOpApi, dao)

        repository.saveSelectedImage(testImage(5))

        assertEquals(testImage(5), dao.stored)
        assertEquals(1, dao.upsertCount)
    }

    @Test
    fun savingTwiceKeepsLatestImage() = runTest {
        val dao = FakeImageDao()
        val repository = ImageRepositoryImpl(NoOpApi, dao)

        repository.saveSelectedImage(testImage(1))
        repository.saveSelectedImage(testImage(2))

        assertEquals(testImage(2), dao.stored)
    }

    private object NoOpApi : PicsumApi {
        override suspend fun getImageList(page: Int, limit: Int): PicsumPage =
            PicsumPage(images = emptyList(), hasNextPage = false)
    }
}
