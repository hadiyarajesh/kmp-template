package com.hadiyarajesh.kmptemplate.ui.home

import androidx.paging.PagingData
import app.cash.turbine.test
import com.hadiyarajesh.kmptemplate.data.database.entity.Image
import com.hadiyarajesh.kmptemplate.data.repository.HomeRepository
import com.hadiyarajesh.kmptemplate.testutil.testImage
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val repository = FakeHomeRepository()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun startsInInitialState() = runTest {
        HomeViewModelImpl(repository).uiState.test {
            assertEquals(HomeScreenUiState.Initial, awaitItem())
        }
    }

    @Test
    fun loadDataEmitsSuccess() = runTest {
        val viewModel = HomeViewModelImpl(repository)

        viewModel.uiState.test {
            assertEquals(HomeScreenUiState.Initial, awaitItem())

            viewModel.loadData()

            assertIs<HomeScreenUiState.Success>(expectMostRecentItem())
        }
    }

    @Test
    fun loadDataKeepsExistingSuccessState() = runTest {
        val viewModel = HomeViewModelImpl(repository)

        viewModel.uiState.test {
            awaitItem()
            viewModel.loadData()
            expectMostRecentItem()

            viewModel.loadData()

            expectNoEvents()
            assertEquals(1, repository.getImagesCalls)
        }
    }

    @Test
    fun loadDataEmitsErrorWhenRepositoryThrows() = runTest {
        repository.getImagesError = IllegalStateException("boom")
        val viewModel = HomeViewModelImpl(repository)

        viewModel.uiState.test {
            awaitItem()

            viewModel.loadData()

            assertEquals(HomeScreenUiState.Error("boom"), expectMostRecentItem())
        }
    }

    @Test
    fun saveSelectedImageDelegatesToRepository() = runTest {
        val viewModel = HomeViewModelImpl(repository)

        viewModel.uiState.test {
            awaitItem()

            viewModel.saveSelectedImage(testImage(3))
            expectNoEvents()

            assertEquals(listOf(testImage(3)), repository.saved)
        }
    }
}

private class FakeHomeRepository : HomeRepository {
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
