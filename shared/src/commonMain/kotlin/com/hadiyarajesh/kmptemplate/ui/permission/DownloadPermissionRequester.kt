package com.hadiyarajesh.kmptemplate.ui.permission

import androidx.compose.runtime.Composable

interface DownloadPermissionRequester {
    fun hasRequiredPermission(): Boolean
    fun requestPermission()
    fun canOpenSettings(): Boolean = false
    fun openSettings() = Unit
}

@Composable
expect fun rememberDownloadPermissionRequester(
    onResult: (Boolean) -> Unit
): DownloadPermissionRequester
