package com.hadiyarajesh.kmp_template.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.hadiyarajesh.kmp_template.di.LocalAppGraph
import com.hadiyarajesh.kmp_template.ui.components.LoadingIndicator
import com.hadiyarajesh.kmp_template.ui.detail.DetailScreenRoute
import com.hadiyarajesh.kmp_template.ui.home.HomeScreenRoute
import com.hadiyarajesh.kmp_template.ui.onboarding.OnboardingScreen

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier
) {
    val appGraph = LocalAppGraph.current
    val onboardingViewModel = viewModel { appGraph.getOnboardingViewModel() }
    val shouldShowOnboarding by onboardingViewModel.shouldShowOnboarding.collectAsState()
    val backStack = remember { mutableStateListOf<NavDestination>() }

    LaunchedEffect(shouldShowOnboarding) {
        if (backStack.isNotEmpty()) return@LaunchedEffect

        when (shouldShowOnboarding) {
            true -> backStack.add(NavDestination.Onboarding)
            false -> backStack.add(NavDestination.Home)
            null -> Unit
        }
    }

    if (backStack.isEmpty()) {
        LoadingIndicator(modifier = modifier)
        return
    }

    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        onBack = {
            if (backStack.size > 1) {
                backStack.removeLastOrNull()
            }
        },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = { key ->
            when (key) {
                NavDestination.Onboarding -> NavEntry(key) {
                    OnboardingScreen(
                        onGetStarted = {
                            onboardingViewModel.completeOnboarding()
                            backStack.clear()
                            backStack.add(NavDestination.Home)
                        }
                    )
                }

                NavDestination.Home -> NavEntry(key) {
                    val homeViewModel = viewModel { appGraph.getHomeViewModel() }

                    HomeScreenRoute(
                        viewModel = homeViewModel,
                        onNavigateClick = { image ->
                            backStack.add(NavDestination.Detail(image))
                        }
                    )
                }

                is NavDestination.Detail -> NavEntry(key) {
                    val detailViewModel = viewModel { appGraph.getDetailViewModel() }

                    DetailScreenRoute(
                        image = key.image,
                        viewModel = detailViewModel,
                        onBackClick = {
                            if (backStack.size > 1) {
                                backStack.removeLastOrNull()
                            }
                        }
                    )
                }
            }
        }
    )
}
