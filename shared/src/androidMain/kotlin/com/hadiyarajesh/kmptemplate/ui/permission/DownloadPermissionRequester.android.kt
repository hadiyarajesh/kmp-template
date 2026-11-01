package com.hadiyarajesh.kmptemplate.ui.permission

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext

@Composable
internal actual fun rememberDownloadPermissionRequester(
    onResult: (Boolean) -> Unit
): DownloadPermissionRequester {
    val context = LocalContext.current
    val permissionToRequest = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    } else {
        null
    }
    val currentOnResult by rememberUpdatedState(onResult)
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = onResult
    )

    return remember(context, permissionToRequest, launcher) {
        object : DownloadPermissionRequester {
            override fun hasRequiredPermission(): Boolean = permissionToRequest == null ||
                context.checkSelfPermission(permissionToRequest) ==
                PackageManager.PERMISSION_GRANTED

            override fun requestPermission() {
                if (permissionToRequest == null) {
                    currentOnResult(true)
                } else {
                    launcher.launch(permissionToRequest)
                }
            }
        }
    }
}
