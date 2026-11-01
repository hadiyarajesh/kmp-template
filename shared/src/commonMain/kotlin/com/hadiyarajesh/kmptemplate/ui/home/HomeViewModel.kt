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
import com.hadiyarajesh.kmptemplate.data.database.entity.Image
import com.hadiyarajesh.kmptemplate.data.repository.HomeRepository
import com.hadiyarajesh.kmptemplate.di.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/** What the home screen depends on. */
interface HomeViewModel {
    val uiState: StateFlow<HomeScreenUiState>

    fun loadData()
    fun saveSelectedImage(image: Image)
}

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class, binding = binding<ViewModel>())
internal class HomeViewModelImpl(private val homeRepository: HomeRepository) :
    ViewModel(),
    HomeViewModel {
    private val events = Channel<HomeEvent>(Channel.UNLIMITED)

    // Created once: a new Flow per recomposition would restart the presenter's LaunchedEffect.
    private val eventFlow = events.receiveAsFlow()

    override val uiState: StateFlow<HomeScreenUiState> = viewModelScope.launchMolecule(
        mode = RecompositionMode.Immediate
    ) {
        present()
    }

    override fun loadData() {
        events.trySend(HomeEvent.LoadData)
    }

    override fun saveSelectedImage(image: Image) {
        events.trySend(HomeEvent.SelectImage(image))
    }

    /** All state lives here: events in, [HomeScreenUiState] out. */
    @Composable
    private fun present(): HomeScreenUiState {
        var state: HomeScreenUiState by remember { mutableStateOf(HomeScreenUiState.Initial) }

        LaunchedEffect(Unit) {
            eventFlow.collect { event ->
                when (event) {
                    HomeEvent.LoadData -> if (state !is HomeScreenUiState.Success) {
                        state = HomeScreenUiState.Loading
                        state = try {
                            HomeScreenUiState.Success(
                                homeRepository.getImages().cachedIn(viewModelScope)
                            )
                        } catch (e: Exception) {
                            HomeScreenUiState.Error(msg = e.message ?: "Something went wrong")
                        }
                    }

                    is HomeEvent.SelectImage -> launch {
                        homeRepository.saveSelectedImage(event.image)
                    }
                }
            }
        }

        return state
    }
}
