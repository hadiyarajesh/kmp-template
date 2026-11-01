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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/** What the detail screen depends on. */
internal interface DetailViewModel {
    val uiState: StateFlow<DetailUiState>

    // One-shot events stay out of uiState. Each flow is meant for a single collector.
    val downloadEventFlow: Flow<DownloadEvent>
    val permissionDeniedFlow: Flow<Unit>

    /** Records [image] as the selected image. [download] acts on it. */
    fun start(image: Image)
    fun download()
    fun notifyPermissionDenied()
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

    // Channels, not SharedFlows: they hold events while nothing collects, e.g. during rotation.
    private val downloadEvents = Channel<DownloadEvent>(Channel.BUFFERED)
    private val permissionDenied = Channel<Unit>(Channel.BUFFERED)

    override val uiState: StateFlow<DetailUiState> = viewModelScope.launchMolecule(
        mode = RecompositionMode.Immediate
    ) {
        present()
    }

    override val downloadEventFlow: Flow<DownloadEvent> = downloadEvents.receiveAsFlow()
    override val permissionDeniedFlow: Flow<Unit> = permissionDenied.receiveAsFlow()

    override fun start(image: Image) {
        actions.trySend(DetailAction.Start(image))
    }

    override fun download() {
        actions.trySend(DetailAction.Download)
    }

    override fun notifyPermissionDenied() {
        permissionDenied.trySend(Unit)
    }

    /** All state lives here: actions in, [DetailUiState] out. */
    @Composable
    private fun present(): DetailUiState {
        var image by remember { mutableStateOf<Image?>(null) }
        var isDownloading by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            for (action in actions) {
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
                                val result = imageDownloadManager.downloadAndSave(selected)
                                isDownloading = false
                                when (result) {
                                    ImageSaveResult.Success ->
                                        downloadEvents.send(DownloadEvent.Succeeded)

                                    is ImageSaveResult.Error ->
                                        downloadEvents.send(DownloadEvent.Failed(result.message))

                                    ImageSaveResult.PermissionDenied -> permissionDenied.send(Unit)
                                }
                            }
                        }
                    }
                }
            }
        }

        return DetailUiState(isDownloading = isDownloading)
    }
}

/** Returns the [DetailViewModel] scoped to the current navigation entry. */
@Composable
internal fun detailViewModel(): DetailViewModel = metroViewModel<DetailViewModelImpl>()
