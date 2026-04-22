package com.hadiyarajesh.kmptemplate.ui.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSURL
import platform.Photos.PHAccessLevelAddOnly
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHAuthorizationStatusLimited
import platform.Photos.PHAuthorizationStatusNotDetermined
import platform.Photos.PHPhotoLibrary
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

@Composable
actual fun rememberDownloadPermissionRequester(
    onResult: (Boolean) -> Unit
): DownloadPermissionRequester {
    return remember(onResult) {
        object : DownloadPermissionRequester {
            override fun hasRequiredPermission(): Boolean = isAuthorized()

            override fun requestPermission() {
                when (PHPhotoLibrary.authorizationStatusForAccessLevel(PHAccessLevelAddOnly)) {
                    PHAuthorizationStatusAuthorized,
                    PHAuthorizationStatusLimited -> onResult(true)

                    PHAuthorizationStatusNotDetermined -> {
                        PHPhotoLibrary.requestAuthorizationForAccessLevel(
                            PHAccessLevelAddOnly
                        ) { status ->
                            onResult(
                                status == PHAuthorizationStatusAuthorized ||
                                    status == PHAuthorizationStatusLimited
                            )
                        }
                    }

                    else -> onResult(false)
                }
            }

            override fun canOpenSettings(): Boolean = true

            override fun openSettings() {
                val settingsUrl = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
                UIApplication.sharedApplication.openURL(
                    settingsUrl,
                    options = emptyMap<Any?, Any>(),
                    completionHandler = null
                )
            }
        }
    }
}

private fun isAuthorized(): Boolean =
    when (PHPhotoLibrary.authorizationStatusForAccessLevel(PHAccessLevelAddOnly)) {
        PHAuthorizationStatusAuthorized,
        PHAuthorizationStatusLimited -> true

        else -> false
    }
