package com.hadiyarajesh.kmptemplate.data.download

import dev.zacsweers.metro.Inject
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Photos.PHAccessLevelAddOnly
import platform.Photos.PHAssetChangeRequest
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHAuthorizationStatusDenied
import platform.Photos.PHAuthorizationStatusLimited
import platform.Photos.PHAuthorizationStatusNotDetermined
import platform.Photos.PHAuthorizationStatusRestricted
import platform.Photos.PHPhotoLibrary
import platform.UIKit.UIImage
import platform.posix.O_CREAT
import platform.posix.O_TRUNC
import platform.posix.O_WRONLY
import platform.posix.close
import platform.posix.open
import platform.posix.write
import kotlin.coroutines.resume

@Inject
@OptIn(ExperimentalForeignApi::class)
class IosImageSaver : ImageSaver {
    override suspend fun saveImage(
        bytes: ByteArray,
        fileName: String,
        sourceUrl: String
    ): ImageSaveResult {
        val hasPermission = requestPhotoPermission()
        if (!hasPermission) {
            return ImageSaveResult.PermissionDenied
        }

        val tempFilePath = NSTemporaryDirectory() + fileName
        if (!writeBytesToPath(tempFilePath, bytes)) {
            return ImageSaveResult.Error("Unable to prepare image file")
        }

        val image = UIImage.imageWithContentsOfFile(tempFilePath)
            ?: return ImageSaveResult.Error("Invalid image data")

        val saved = suspendCancellableCoroutine<Boolean> { continuation ->
            PHPhotoLibrary.sharedPhotoLibrary().performChanges(
                changeBlock = {
                    PHAssetChangeRequest.creationRequestForAssetFromImage(image)
                },
                completionHandler = { success, _ ->
                    continuation.resume(success)
                }
            )
        }
        NSFileManager.defaultManager.removeItemAtPath(tempFilePath, null)

        return if (saved) {
            ImageSaveResult.Success
        } else {
            ImageSaveResult.Error("Unable to save image")
        }
    }
}

private suspend fun requestPhotoPermission(): Boolean =
    when (PHPhotoLibrary.authorizationStatusForAccessLevel(PHAccessLevelAddOnly)) {
        PHAuthorizationStatusAuthorized,
        PHAuthorizationStatusLimited -> true

        PHAuthorizationStatusDenied,
        PHAuthorizationStatusRestricted -> false

        PHAuthorizationStatusNotDetermined -> suspendCancellableCoroutine { continuation ->
            PHPhotoLibrary.requestAuthorizationForAccessLevel(PHAccessLevelAddOnly) { status ->
                continuation.resume(
                    status == PHAuthorizationStatusAuthorized ||
                        status == PHAuthorizationStatusLimited
                )
            }
        }

        else -> false
    }

@OptIn(ExperimentalForeignApi::class)
private fun writeBytesToPath(path: String, bytes: ByteArray): Boolean {
    val fileDescriptor = open(path, O_WRONLY or O_CREAT or O_TRUNC, 0x1A4)
    if (fileDescriptor < 0) return false

    var offset = 0
    var success = true

    try {
        while (offset < bytes.size) {
            val written = bytes.usePinned { pinned ->
                write(
                    fileDescriptor,
                    pinned.addressOf(offset),
                    (bytes.size - offset).convert()
                )
            }
            if (written <= 0) {
                success = false
                break
            }
            offset += written.toInt()
        }
    } finally {
        close(fileDescriptor)
    }

    return success && offset == bytes.size
}
