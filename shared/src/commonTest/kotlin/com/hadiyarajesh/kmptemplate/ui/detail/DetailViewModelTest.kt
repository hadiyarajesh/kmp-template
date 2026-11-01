package com.hadiyarajesh.kmptemplate.ui.detail

import com.hadiyarajesh.kmptemplate.data.download.ImageDownloadManager
import com.hadiyarajesh.kmptemplate.data.download.ImageSaveResult
import com.hadiyarajesh.kmptemplate.testutil.FakeImageSaver
import com.hadiyarajesh.kmptemplate.testutil.mockHttpClient
import com.hadiyarajesh.kmptemplate.testutil.testImage
import io.ktor.client.engine.mock.respond
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {
    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(saverResult: ImageSaveResult): DetailViewModel = DetailViewModel(
        ImageDownloadManager(
            mockHttpClient { respond(byteArrayOf(1)) },
            FakeImageSaver(saverResult)
        )
    )

    // The download runs on a real dispatcher inside Ktor, so wait for the emitted event.
    private suspend fun TestScope.eventAfter(
        viewModel: DetailViewModel,
        action: () -> Unit
    ): DownloadEvent {
        val event = async(start = CoroutineStart.UNDISPATCHED) { viewModel.events.first() }
        action()
        return event.await()
    }

    @Test
    fun successfulDownloadEmitsSuccessAndResetsLoading() = runTest {
        val viewModel = viewModel(ImageSaveResult.Success)

        val event = eventAfter(viewModel) { viewModel.downloadImage(testImage()) }

        assertEquals(DownloadEvent.Success, event)
        viewModel.isDownloading.first { !it }
        assertFalse(viewModel.isDownloading.value)
    }

    @Test
    fun permissionDeniedIsForwarded() = runTest {
        val viewModel = viewModel(ImageSaveResult.PermissionDenied)

        val event = eventAfter(viewModel) { viewModel.downloadImage(testImage()) }

        assertEquals(DownloadEvent.PermissionDenied, event)
    }

    @Test
    fun saveErrorIsForwardedWithMessage() = runTest {
        val viewModel = viewModel(ImageSaveResult.Error("disk full"))

        val event = eventAfter(viewModel) { viewModel.downloadImage(testImage()) }

        assertEquals(DownloadEvent.Error("disk full"), event)
    }

    @Test
    fun notifyPermissionDeniedEmitsEvent() = runTest {
        val viewModel = viewModel(ImageSaveResult.Success)

        val event = eventAfter(viewModel) { viewModel.notifyPermissionDenied() }

        assertEquals(DownloadEvent.PermissionDenied, event)
    }
}
