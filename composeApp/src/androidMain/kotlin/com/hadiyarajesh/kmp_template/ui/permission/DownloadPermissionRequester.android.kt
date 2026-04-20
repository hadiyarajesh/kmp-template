package com.hadiyarajesh.kmp_template.ui.permission

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberDownloadPermissionRequester(
    onResult: (Boolean) -> Unit
): DownloadPermissionRequester {
    val context = LocalContext.current
    val permissionToRequest = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    } else {
        null
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = onResult
    )

    return remember(context, permissionToRequest, launcher, onResult) {
        object : DownloadPermissionRequester {
            override fun hasRequiredPermission(): Boolean {
                return permissionToRequest == null ||
                        context.checkSelfPermission(permissionToRequest) == PackageManager.PERMISSION_GRANTED
            }

            override fun requestPermission() {
                if (permissionToRequest == null) {
                    onResult(true)
                } else {
                    launcher.launch(permissionToRequest)
                }
            }
        }
    }
}
