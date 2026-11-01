package com.hadiyarajesh.kmptemplate.ui.detail

import com.hadiyarajesh.kmptemplate.data.database.entity.Image

/**
 * [event] is a one-shot outcome the screen shows and then acknowledges with
 * [DetailViewModel.onEventConsumed], so it is never lost across recompositions or rotation.
 */
internal data class DetailUiState(
    val isDownloading: Boolean = false,
    val event: DownloadEvent? = null
)

internal sealed interface DownloadEvent {
    data object Success : DownloadEvent
    data object PermissionDenied : DownloadEvent
    data class Error(val message: String) : DownloadEvent
}

internal sealed interface DetailAction {
    data class Start(val image: Image) : DetailAction
    data object Download : DetailAction
    data object PermissionDenied : DetailAction
    data object EventConsumed : DetailAction
}
