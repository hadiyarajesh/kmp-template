package com.hadiyarajesh.kmptemplate.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hadiyarajesh.kmptemplate.datastore.DataStoreManager
import com.hadiyarajesh.kmptemplate.di.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** What the app root depends on. */
interface OnboardingViewModel {
    /** `null` until the stored value has been read. */
    val shouldShowOnboarding: StateFlow<Boolean?>

    fun completeOnboarding()
}

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class, binding = binding<ViewModel>())
internal class OnboardingViewModelImpl(private val dataStoreManager: DataStoreManager) :
    ViewModel(),
    OnboardingViewModel {
    override val shouldShowOnboarding: StateFlow<Boolean?> = dataStoreManager.hasSeenOnboardingFlow
        .map { hasSeenOnboarding -> !hasSeenOnboarding }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = null
        )

    override fun completeOnboarding() {
        viewModelScope.launch {
            dataStoreManager.markOnboardingSeen()
        }
    }
}
