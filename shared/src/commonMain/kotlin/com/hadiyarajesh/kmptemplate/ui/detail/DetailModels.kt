package com.hadiyarajesh.kmptemplate.ui.detail

import com.hadiyarajesh.kmptemplate.data.database.entity.Image

internal data class DetailUiState(val isDownloading: Boolean = false)

/** One-shot outcomes of a download. */
internal sealed interface DownloadEvent {
    data object Succeeded : DownloadEvent
    data class Failed(val message: String) : DownloadEvent
}

internal sealed interface DetailAction {
    data class Start(val image: Image) : DetailAction
    data object Download : DetailAction
}
