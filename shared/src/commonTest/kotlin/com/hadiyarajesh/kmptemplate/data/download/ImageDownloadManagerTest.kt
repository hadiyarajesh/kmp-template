package com.hadiyarajesh.kmptemplate.data.download

import com.hadiyarajesh.kmptemplate.testutil.FakeImageSaver
import com.hadiyarajesh.kmptemplate.testutil.mockHttpClient
import com.hadiyarajesh.kmptemplate.testutil.testImage
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest

internal class ImageDownloadManagerTest {
    @Test
    fun savesDownloadedBytesWithSanitizedFileName() = runTest {
        val saver = FakeImageSaver()
        val manager = ImageDownloadManager(
            mockHttpClient { respond(byteArrayOf(1, 2, 3)) },
            saver
        )

        val result = manager.downloadAndSave(testImage(1).copy(imageId = "a/b 1"))

        assertEquals(ImageSaveResult.Success, result)
        assertEquals(listOf("picsum_a_b_1.jpg"), saver.savedFileNames)
    }

    @Test
    fun returnsSaverResult() = runTest {
        val saver = FakeImageSaver(ImageSaveResult.PermissionDenied)
        val manager = ImageDownloadManager(mockHttpClient { respond(byteArrayOf(1)) }, saver)

        assertEquals(ImageSaveResult.PermissionDenied, manager.downloadAndSave(testImage()))
    }

    @Test
    fun downloadFailureBecomesErrorAndNothingIsSaved() = runTest {
        val saver = FakeImageSaver()
        val manager = ImageDownloadManager(
            mockHttpClient { respond("", HttpStatusCode.NotFound) },
            saver
        )

        assertIs<ImageSaveResult.Error>(manager.downloadAndSave(testImage()))
        assertEquals(emptyList(), saver.savedFileNames)
    }
}
