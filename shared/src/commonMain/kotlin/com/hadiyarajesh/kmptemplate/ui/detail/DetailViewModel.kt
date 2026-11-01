package com.hadiyarajesh.kmptemplate.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import com.hadiyarajesh.kmptemplate.data.database.entity.Image
import com.hadiyarajesh.kmptemplate.data.download.ImageDownloadManager
import com.hadiyarajesh.kmptemplate.data.download.ImageSaveResult
import com.hadiyarajesh.kmptemplate.data.repository.ImageRepository
import com.hadiyarajesh.kmptemplate.di.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/** What the detail screen depends on. */
internal interface DetailViewModel {
    val uiState: StateFlow<DetailUiState>

    /** Records [image] as the selected image. [download] acts on it. */
    fun start(image: Image)
    fun download()
    fun notifyPermissionDenied()
    fun onEventConsumed()
}

/** Returns the [DetailViewModel] scoped to the current navigation entry. */
@Composable
internal fun detailViewModel(): DetailViewModel = metroViewModel<DetailViewModelImpl>()

private sealed interface DetailAction {
    data class Start(val image: Image) : DetailAction
    data object Download : DetailAction
    data object PermissionDenied : DetailAction
    data object EventConsumed : DetailAction
}

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class, binding = binding<ViewModel>())
internal class DetailViewModelImpl(
    private val imageDownloadManager: ImageDownloadManager,
    private val imageRepository: ImageRepository
) : ViewModel(),
    DetailViewModel {
    private val actions = Channel<DetailAction>(Channel.UNLIMITED)

    // Created once: a new Flow per recomposition would restart the presenter's LaunchedEffect.
    private val actionFlow = actions.receiveAsFlow()

    override val uiState: StateFlow<DetailUiState> = viewModelScope.launchMolecule(
        mode = RecompositionMode.Immediate
    ) {
        present()
    }

    override fun start(image: Image) {
        actions.trySend(DetailAction.Start(image))
    }

    override fun download() {
        actions.trySend(DetailAction.Download)
    }

    override fun notifyPermissionDenied() {
        actions.trySend(DetailAction.PermissionDenied)
    }

    override fun onEventConsumed() {
        actions.trySend(DetailAction.EventConsumed)
    }

    /** All state lives here: actions in, [DetailUiState] out. */
    @Composable
    private fun present(): DetailUiState {
        var image by remember { mutableStateOf<Image?>(null) }
        var isDownloading by remember { mutableStateOf(false) }
        var event by remember { mutableStateOf<DownloadEvent?>(null) }

        LaunchedEffect(Unit) {
            actionFlow.collect { action ->
                when (action) {
                    is DetailAction.Start -> {
                        image = action.image
                        launch { imageRepository.saveSelectedImage(action.image) }
                    }

                    DetailAction.Download -> {
                        val selected = image
                        if (selected != null && !isDownloading) {
                            isDownloading = true
                            launch {
                                event = download(selected)
                                isDownloading = false
                            }
                        }
                    }

                    DetailAction.PermissionDenied -> event = DownloadEvent.PermissionDenied
                    DetailAction.EventConsumed -> event = null
                }
            }
        }

        return DetailUiState(isDownloading = isDownloading, event = event)
    }

    private suspend fun download(image: Image): DownloadEvent =
        when (val result = imageDownloadManager.downloadAndSave(image)) {
            ImageSaveResult.Success -> DownloadEvent.Success
            ImageSaveResult.PermissionDenied -> DownloadEvent.PermissionDenied
            is ImageSaveResult.Error -> DownloadEvent.Error(result.message)
        }
}
