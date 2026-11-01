package com.hadiyarajesh.kmptemplate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.hadiyarajesh.kmptemplate.di.CommonAppGraph
import com.hadiyarajesh.kmptemplate.navigation.AppNavigation
import com.hadiyarajesh.kmptemplate.navigation.NavDestination
import com.hadiyarajesh.kmptemplate.ui.components.LoadingIndicator
import com.hadiyarajesh.kmptemplate.ui.onboarding.OnboardingViewModel
import com.hadiyarajesh.kmptemplate.ui.onboarding.OnboardingViewModelImpl
import com.hadiyarajesh.kmptemplate.ui.theme.AppTheme
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.metroViewModel

@Composable
fun App(appGraph: CommonAppGraph) {
    CompositionLocalProvider(LocalMetroViewModelFactory provides appGraph.metroViewModelFactory) {
        AppTheme {
            AppRoot()
        }
    }
}

@Composable
private fun AppRoot() {
    val onboardingViewModel: OnboardingViewModel = metroViewModel<OnboardingViewModelImpl>()
    val shouldShowOnboarding by onboardingViewModel.shouldShowOnboarding.collectAsState()

    // The start destination depends on a stored value, so wait for it before building the stack.
    when (val showOnboarding = shouldShowOnboarding) {
        null -> LoadingIndicator()

        else -> AppNavigation(
            startDestination = if (showOnboarding) {
                NavDestination.Onboarding
            } else {
                NavDestination.Home
            },
            onOnboardingCompleted = onboardingViewModel::completeOnboarding
        )
    }
}
