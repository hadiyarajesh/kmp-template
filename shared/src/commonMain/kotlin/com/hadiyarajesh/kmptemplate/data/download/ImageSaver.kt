package com.hadiyarajesh.kmptemplate.data.download

interface ImageSaver {
    suspend fun saveImage(bytes: ByteArray, fileName: String, sourceUrl: String): ImageSaveResult
}

sealed interface ImageSaveResult {
    data object Success : ImageSaveResult
    data object PermissionDenied : ImageSaveResult
    data class Error(val message: String) : ImageSaveResult
}
