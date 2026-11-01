package com.hadiyarajesh.kmptemplate.ui.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import platform.Foundation.NSURL
import platform.Photos.PHAccessLevelAddOnly
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHAuthorizationStatusLimited
import platform.Photos.PHAuthorizationStatusNotDetermined
import platform.Photos.PHPhotoLibrary
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@Composable
internal actual fun rememberDownloadPermissionRequester(
    onResult: (Boolean) -> Unit
): DownloadPermissionRequester {
    val currentOnResult by rememberUpdatedState(onResult)

    return remember {
        object : DownloadPermissionRequester {
            override fun hasRequiredPermission(): Boolean = isAuthorized()

            override fun requestPermission() {
                when (PHPhotoLibrary.authorizationStatusForAccessLevel(PHAccessLevelAddOnly)) {
                    PHAuthorizationStatusAuthorized,
                    PHAuthorizationStatusLimited -> currentOnResult(true)

                    PHAuthorizationStatusNotDetermined -> {
                        PHPhotoLibrary.requestAuthorizationForAccessLevel(
                            PHAccessLevelAddOnly
                        ) { status ->
                            val granted = status == PHAuthorizationStatusAuthorized ||
                                status == PHAuthorizationStatusLimited
                            // Photos calls back on an arbitrary queue; UI state lives on main.
                            dispatch_async(dispatch_get_main_queue()) { currentOnResult(granted) }
                        }
                    }

                    else -> currentOnResult(false)
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
