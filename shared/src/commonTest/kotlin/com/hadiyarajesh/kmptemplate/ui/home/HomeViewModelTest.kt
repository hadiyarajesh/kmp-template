package com.hadiyarajesh.kmptemplate.ui.home

import app.cash.turbine.test
import com.hadiyarajesh.kmptemplate.testutil.FakeImageRepository
import com.hadiyarajesh.kmptemplate.testutil.TestViewModelStore
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
internal class HomeViewModelTest {
    private val repository = FakeImageRepository()
    private val viewModels = TestViewModelStore()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() {
        viewModels.clear()
        Dispatchers.resetMain()
    }

    private fun viewModel(): HomeViewModel = viewModels.add(HomeViewModelImpl(repository))

    @Test
    fun startsInInitialState() = runTest {
        viewModel().uiState.test {
            assertEquals(HomeScreenUiState.Initial, awaitItem())
        }
    }

    @Test
    fun startEmitsSuccess() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            assertEquals(HomeScreenUiState.Initial, awaitItem())

            viewModel.start()

            assertIs<HomeScreenUiState.Success>(expectMostRecentItem())
        }
    }

    @Test
    fun startKeepsExistingSuccessState() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            awaitItem()
            viewModel.start()
            expectMostRecentItem()

            viewModel.start()

            expectNoEvents()
            assertEquals(1, repository.getImagesCalls)
        }
    }

    @Test
    fun startEmitsErrorWhenRepositoryThrows() = runTest {
        repository.getImagesError = IllegalStateException("boom")
        val viewModel = viewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.start()

            assertEquals(HomeScreenUiState.Error("boom"), expectMostRecentItem())
        }
    }
}
