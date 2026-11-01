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
import com.hadiyarajesh.kmptemplate.di.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/** What the detail screen depends on. */
interface DetailViewModel {
    val uiState: StateFlow<DetailUiState>

    fun downloadImage(image: Image)
    fun notifyPermissionDenied()
    fun onEventConsumed()
}

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class, binding = binding<ViewModel>())
internal class DetailViewModelImpl(private val imageDownloadManager: ImageDownloadManager) :
    ViewModel(),
    DetailViewModel {
    private val actions = Channel<DetailAction>(Channel.UNLIMITED)

    // Created once: a new Flow per recomposition would restart the presenter's LaunchedEffect.
    private val actionFlow = actions.receiveAsFlow()

    override val uiState: StateFlow<DetailUiState> = viewModelScope.launchMolecule(
        mode = RecompositionMode.Immediate
    ) {
        present()
    }

    override fun downloadImage(image: Image) {
        actions.trySend(DetailAction.Download(image))
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
        var isDownloading by remember { mutableStateOf(false) }
        var event by remember { mutableStateOf<DownloadEvent?>(null) }

        LaunchedEffect(Unit) {
            actionFlow.collect { action ->
                when (action) {
                    is DetailAction.Download -> if (!isDownloading) {
                        isDownloading = true
                        launch {
                            event = when (
                                val result = imageDownloadManager.downloadAndSave(action.image)
                            ) {
                                ImageSaveResult.Success -> DownloadEvent.Success
                                ImageSaveResult.PermissionDenied -> DownloadEvent.PermissionDenied
                                is ImageSaveResult.Error -> DownloadEvent.Error(result.message)
                            }
                            isDownloading = false
                        }
                    }

                    DetailAction.PermissionDenied -> event = DownloadEvent.PermissionDenied
                    DetailAction.EventConsumed -> event = null
                }
            }
        }

        return DetailUiState(isDownloading = isDownloading, event = event)
    }
}
