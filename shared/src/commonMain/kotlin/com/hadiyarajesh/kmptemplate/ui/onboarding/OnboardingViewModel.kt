package com.hadiyarajesh.kmptemplate.ui.onboarding

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
import com.hadiyarajesh.kmptemplate.data.datastore.DataStoreManager
import com.hadiyarajesh.kmptemplate.di.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

private sealed interface OnboardingAction {
    data object Start : OnboardingAction
    data object Complete : OnboardingAction
}

/** What the app root depends on. */
internal interface OnboardingViewModel {
    /** `null` until [start] has run and the stored value has been read. */
    val shouldShowOnboarding: StateFlow<Boolean?>

    fun start()
    fun completeOnboarding()
}

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class, binding = binding<ViewModel>())
internal class OnboardingViewModelImpl(private val dataStoreManager: DataStoreManager) :
    ViewModel(),
    OnboardingViewModel {
    private val actions = Channel<OnboardingAction>(Channel.UNLIMITED)

    // Created once: a new Flow per recomposition would restart the presenter's LaunchedEffect.
    private val actionFlow = actions.receiveAsFlow()

    override val shouldShowOnboarding: StateFlow<Boolean?> = viewModelScope.launchMolecule(
        mode = RecompositionMode.Immediate
    ) {
        present()
    }

    override fun start() {
        actions.trySend(OnboardingAction.Start)
    }

    override fun completeOnboarding() {
        actions.trySend(OnboardingAction.Complete)
    }

    /** All state lives here: actions in, "should show onboarding" out. */
    @Composable
    private fun present(): Boolean? {
        var shouldShow by remember { mutableStateOf<Boolean?>(null) }
        var started by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            actionFlow.collect { action ->
                when (action) {
                    OnboardingAction.Start -> started = true
                    OnboardingAction.Complete -> launch { dataStoreManager.markOnboardingSeen() }
                }
            }
        }

        if (started) {
            LaunchedEffect(Unit) {
                dataStoreManager.hasSeenOnboardingFlow.collect { hasSeen -> shouldShow = !hasSeen }
            }
        }

        return shouldShow
    }
}
