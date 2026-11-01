package com.hadiyarajesh.kmptemplate.ui.permission

import androidx.compose.runtime.Composable

internal interface DownloadPermissionRequester {
    fun hasRequiredPermission(): Boolean
    fun requestPermission()
    fun canOpenSettings(): Boolean = false
    fun openSettings() = Unit
}

@Composable
internal expect fun rememberDownloadPermissionRequester(
    onResult: (Boolean) -> Unit
): DownloadPermissionRequester
