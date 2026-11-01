package com.hadiyarajesh.kmptemplate.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import com.hadiyarajesh.kmptemplate.data.repository.ImageRepository
import com.hadiyarajesh.kmptemplate.di.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow

/** What the home screen depends on. */
internal interface HomeViewModel {
    val uiState: StateFlow<HomeScreenUiState>

    fun start()
}

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class, binding = binding<ViewModel>())
internal class HomeViewModelImpl(private val imageRepository: ImageRepository) :
    ViewModel(),
    HomeViewModel {
    private val actions = Channel<HomeAction>(Channel.UNLIMITED)

    override val uiState: StateFlow<HomeScreenUiState> = viewModelScope.launchMolecule(
        mode = RecompositionMode.Immediate
    ) {
        present()
    }

    override fun start() {
        actions.trySend(HomeAction.Start)
    }

    /** All state lives here: actions in, [HomeScreenUiState] out. */
    @Composable
    private fun present(): HomeScreenUiState {
        var state: HomeScreenUiState by remember { mutableStateOf(HomeScreenUiState.Initial) }

        LaunchedEffect(Unit) {
            for (action in actions) {
                when (action) {
                    HomeAction.Start -> if (state !is HomeScreenUiState.Success) {
                        state = HomeScreenUiState.Loading
                        state = try {
                            HomeScreenUiState.Success(
                                imageRepository.getImages().cachedIn(viewModelScope)
                            )
                        } catch (e: Exception) {
                            HomeScreenUiState.Error(msg = e.message ?: "Something went wrong")
                        }
                    }
                }
            }
        }

        return state
    }
}

/** Returns the [HomeViewModel] scoped to the current navigation entry. */
@Composable
internal fun homeViewModel(): HomeViewModel = metroViewModel<HomeViewModelImpl>()
