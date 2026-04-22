package com.hadiyarajesh.kmptemplate.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hadiyarajesh.kmptemplate.datastore.DataStoreManager
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Inject
class OnboardingViewModel(private val dataStoreManager: DataStoreManager) : ViewModel() {
    val shouldShowOnboarding: StateFlow<Boolean?> = dataStoreManager.hasSeenOnboardingFlow
        .map { hasSeenOnboarding -> !hasSeenOnboarding }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = null
        )

    fun completeOnboarding() {
        viewModelScope.launch {
            dataStoreManager.markOnboardingSeen()
        }
    }
}
