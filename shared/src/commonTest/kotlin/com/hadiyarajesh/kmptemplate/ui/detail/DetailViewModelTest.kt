package com.hadiyarajesh.kmptemplate.ui.detail

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.hadiyarajesh.kmptemplate.data.download.ImageDownloadManager
import com.hadiyarajesh.kmptemplate.data.download.ImageSaveResult
import com.hadiyarajesh.kmptemplate.testutil.FakeImageRepository
import com.hadiyarajesh.kmptemplate.testutil.FakeImageSaver
import com.hadiyarajesh.kmptemplate.testutil.mockHttpClient
import com.hadiyarajesh.kmptemplate.testutil.testImage
import io.ktor.client.engine.mock.respond
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
internal class DetailViewModelTest {
    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private val repository = FakeImageRepository()

    private fun viewModel(saverResult: ImageSaveResult): DetailViewModel = DetailViewModelImpl(
        ImageDownloadManager(
            mockHttpClient { respond(byteArrayOf(1)) },
            FakeImageSaver(saverResult)
        ),
        repository
    )

    // The download runs on a real dispatcher inside Ktor, so wait for the state that carries the event.
    private suspend fun ReceiveTurbine<DetailUiState>.awaitDownloadEvent(): DownloadEvent {
        while (true) {
            awaitItem().event?.let { return it }
        }
    }

    @Test
    fun startsIdle() = runTest {
        viewModel(ImageSaveResult.Success).uiState.test {
            assertEquals(DetailUiState(), awaitItem())
        }
    }

    @Test
    fun successfulDownloadEmitsSuccessAndResetsLoading() = runTest {
        val viewModel = viewModel(ImageSaveResult.Success)

        viewModel.uiState.test {
            awaitItem()

            viewModel.start(testImage())
            viewModel.download()

            assertEquals<DownloadEvent>(DownloadEvent.Success, awaitDownloadEvent())
            assertFalse(viewModel.uiState.value.isDownloading)
        }
    }

    @Test
    fun permissionDeniedIsForwarded() = runTest {
        val viewModel = viewModel(ImageSaveResult.PermissionDenied)

        viewModel.uiState.test {
            awaitItem()

            viewModel.start(testImage())
            viewModel.download()

            assertEquals<DownloadEvent>(DownloadEvent.PermissionDenied, awaitDownloadEvent())
        }
    }

    @Test
    fun saveErrorIsForwardedWithMessage() = runTest {
        val viewModel = viewModel(ImageSaveResult.Error("disk full"))

        viewModel.uiState.test {
            awaitItem()

            viewModel.start(testImage())
            viewModel.download()

            assertEquals<DownloadEvent>(DownloadEvent.Error("disk full"), awaitDownloadEvent())
        }
    }

    @Test
    fun notifyPermissionDeniedEmitsEvent() = runTest {
        val viewModel = viewModel(ImageSaveResult.Success)

        viewModel.uiState.test {
            awaitItem()

            viewModel.notifyPermissionDenied()

            assertEquals<DownloadEvent>(DownloadEvent.PermissionDenied, awaitDownloadEvent())
        }
    }

    @Test
    fun consumingEventClearsIt() = runTest {
        val viewModel = viewModel(ImageSaveResult.Success)

        viewModel.uiState.test {
            awaitItem()
            viewModel.notifyPermissionDenied()
            awaitDownloadEvent()

            viewModel.onEventConsumed()

            assertEquals(null, awaitItem().event)
        }
    }

    @Test
    fun startSavesTheSelectedImage() = runTest {
        val viewModel = viewModel(ImageSaveResult.Success)

        viewModel.uiState.test {
            awaitItem()

            viewModel.start(testImage(3))
            expectNoEvents()

            assertEquals(listOf(testImage(3)), repository.saved)
        }
    }

    @Test
    fun downloadBeforeStartDoesNothing() = runTest {
        val viewModel = viewModel(ImageSaveResult.Success)

        viewModel.uiState.test {
            awaitItem()

            viewModel.download()

            expectNoEvents()
        }
    }
}
