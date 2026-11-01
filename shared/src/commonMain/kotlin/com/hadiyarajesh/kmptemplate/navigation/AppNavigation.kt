package com.hadiyarajesh.kmptemplate.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.hadiyarajesh.kmptemplate.ui.detail.DetailScreenRoute
import com.hadiyarajesh.kmptemplate.ui.home.HomeScreenRoute
import com.hadiyarajesh.kmptemplate.ui.onboarding.OnboardingScreen
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

// Non-JVM targets have no reflection, so polymorphic keys must be registered explicitly.
private val navSavedStateConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(NavDestination.Onboarding::class)
            subclass(NavDestination.Home::class)
            subclass(NavDestination.Detail::class)
        }
    }
}

@Composable
internal fun AppNavigation(
    startDestination: NavDestination,
    onOnboardingCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backStack = rememberNavBackStack(navSavedStateConfig, startDestination)

    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            // Scopes each ViewModel to its entry, so it is cleared when the entry is popped.
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<NavDestination.Onboarding> {
                OnboardingScreen(
                    onGetStarted = {
                        onOnboardingCompleted()
                        backStack.clear()
                        backStack.add(NavDestination.Home)
                    }
                )
            }

            entry<NavDestination.Home> {
                HomeScreenRoute(
                    onImageClick = { image -> backStack.add(NavDestination.Detail(image)) }
                )
            }

            entry<NavDestination.Detail> { key ->
                DetailScreenRoute(
                    image = key.image,
                    onBackClick = { backStack.removeLastOrNull() }
                )
            }
        }
    )
}
