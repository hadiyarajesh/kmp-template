package com.hadiyarajesh.kmptemplate.ui.detail

import app.cash.turbine.test
import app.cash.turbine.turbineScope
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
import kotlin.test.assertTrue
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

    private fun viewModel(saverResult: ImageSaveResult = ImageSaveResult.Success): DetailViewModel =
        DetailViewModelImpl(
            ImageDownloadManager(
                mockHttpClient { respond(byteArrayOf(1)) },
                FakeImageSaver(saverResult)
            ),
            repository
        )

    @Test
    fun startsIdle() = runTest {
        viewModel().uiState.test {
            assertEquals(DetailUiState(), awaitItem())
        }
    }

    // The download runs on Ktor's own dispatcher, so collectors wait in wall-clock time.
    @Test
    fun successfulDownloadTogglesLoadingAndEmitsSucceeded() = runTest {
        val viewModel = viewModel()

        turbineScope {
            val states = viewModel.uiState.testIn(backgroundScope)
            val events = viewModel.downloadEventFlow.testIn(backgroundScope)
            assertFalse(states.awaitItem().isDownloading)

            viewModel.start(testImage())
            viewModel.download()

            assertTrue(states.awaitItem().isDownloading)
            assertFalse(states.awaitItem().isDownloading)
            assertEquals(DownloadEvent.Succeeded, events.awaitItem())
        }
    }

    @Test
    fun saverPermissionDeniedEmitsPermissionDenied() = runTest {
        val viewModel = viewModel(ImageSaveResult.PermissionDenied)

        viewModel.start(testImage())
        viewModel.download()

        viewModel.permissionDeniedFlow.test {
            awaitItem()
        }
    }

    @Test
    fun saveErrorEmitsFailureMessage() = runTest {
        val viewModel = viewModel(ImageSaveResult.Error("disk full"))

        viewModel.start(testImage())
        viewModel.download()

        viewModel.downloadEventFlow.test {
            assertEquals(DownloadEvent.Failed("disk full"), awaitItem())
        }
    }

    @Test
    fun notifyPermissionDeniedEmitsPermissionDenied() = runTest {
        val viewModel = viewModel()

        viewModel.notifyPermissionDenied()

        viewModel.permissionDeniedFlow.test {
            awaitItem()
        }
    }

    @Test
    fun eventsWaitForACollector() = runTest {
        val viewModel = viewModel()

        // Nothing collects yet, as during rotation; the event must still arrive later.
        viewModel.notifyPermissionDenied()

        viewModel.permissionDeniedFlow.test {
            awaitItem()
            expectNoEvents()
        }
    }

    @Test
    fun startSavesTheSelectedImage() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.start(testImage(3))
            expectNoEvents()

            assertEquals(listOf(testImage(3)), repository.saved)
        }
    }

    @Test
    fun downloadBeforeStartDoesNothing() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.download()

            expectNoEvents()
        }
    }
}
