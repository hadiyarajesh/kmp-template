package com.hadiyarajesh.kmptemplate.ui.home

import androidx.paging.PagingData
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
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val repository = FakeHomeRepository()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun startsInInitialState() {
        assertEquals(HomeScreenUiState.Initial, HomeViewModel(repository).uiState.value)
    }

    @Test
    fun loadDataEmitsSuccess() {
        val viewModel = HomeViewModel(repository)

        viewModel.loadData()

        assertIs<HomeScreenUiState.Success>(viewModel.uiState.value)
    }

    @Test
    fun loadDataKeepsExistingSuccessState() {
        val viewModel = HomeViewModel(repository)
        viewModel.loadData()
        val first = viewModel.uiState.value

        viewModel.loadData()

        assertEquals(first, viewModel.uiState.value)
        assertEquals(1, repository.getImagesCalls)
    }

    @Test
    fun loadDataEmitsErrorWhenRepositoryThrows() {
        repository.getImagesError = IllegalStateException("boom")
        val viewModel = HomeViewModel(repository)

        viewModel.loadData()

        assertEquals(HomeScreenUiState.Error("boom"), viewModel.uiState.value)
    }

    @Test
    fun saveSelectedImageDelegatesToRepository() {
        val viewModel = HomeViewModel(repository)

        viewModel.saveSelectedImage(testImage(3))

        assertEquals(listOf(testImage(3)), repository.saved)
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
