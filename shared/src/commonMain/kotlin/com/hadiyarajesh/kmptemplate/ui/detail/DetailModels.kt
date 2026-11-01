package com.hadiyarajesh.kmptemplate.ui.detail

import com.hadiyarajesh.kmptemplate.data.database.entity.Image

/**
 * [event] is a one-shot outcome the screen shows and then acknowledges with
 * [DetailViewModel.onEventConsumed], so it is never lost across recompositions or rotation.
 */
data class DetailUiState(val isDownloading: Boolean = false, val event: DownloadEvent? = null)

sealed interface DownloadEvent {
    data object Success : DownloadEvent
    data object PermissionDenied : DownloadEvent
    data class Error(val message: String) : DownloadEvent
}

sealed interface DetailAction {
    data class Download(val image: Image) : DetailAction
    data object PermissionDenied : DetailAction
    data object EventConsumed : DetailAction
}
