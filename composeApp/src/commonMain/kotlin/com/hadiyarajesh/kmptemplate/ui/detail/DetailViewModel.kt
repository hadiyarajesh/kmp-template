package com.hadiyarajesh.kmptemplate.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hadiyarajesh.kmptemplate.data.database.entity.Image
import com.hadiyarajesh.kmptemplate.data.download.ImageDownloadManager
import com.hadiyarajesh.kmptemplate.data.download.ImageSaveResult
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Inject
class DetailViewModel(private val imageDownloadManager: ImageDownloadManager) : ViewModel() {
    private val _isDownloading = MutableStateFlow(false)
    val isDownloading: StateFlow<Boolean> = _isDownloading.asStateFlow()

    private val _events = MutableSharedFlow<DownloadEvent>()
    val events: SharedFlow<DownloadEvent> = _events.asSharedFlow()

    fun downloadImage(image: Image) {
        if (_isDownloading.value) return

        viewModelScope.launch {
            _isDownloading.value = true
            when (val result = imageDownloadManager.downloadAndSave(image)) {
                ImageSaveResult.Success -> _events.emit(DownloadEvent.Success)
                ImageSaveResult.PermissionDenied -> _events.emit(DownloadEvent.PermissionDenied)
                is ImageSaveResult.Error -> _events.emit(DownloadEvent.Error(result.message))
            }
            _isDownloading.value = false
        }
    }

    fun notifyPermissionDenied() {
        viewModelScope.launch {
            _events.emit(DownloadEvent.PermissionDenied)
        }
    }
}

sealed interface DownloadEvent {
    data object Success : DownloadEvent
    data object PermissionDenied : DownloadEvent
    data class Error(val message: String) : DownloadEvent
}
