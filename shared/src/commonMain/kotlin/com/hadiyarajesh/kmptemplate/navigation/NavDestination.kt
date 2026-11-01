package com.hadiyarajesh.kmptemplate.navigation

import androidx.navigation3.runtime.NavKey
import com.hadiyarajesh.kmptemplate.data.database.entity.Image
import kotlinx.serialization.Serializable

/** Keys are serializable so the back stack survives configuration change and process death. */
@Serializable
internal sealed interface NavDestination : NavKey {
    @Serializable
    data object Onboarding : NavDestination

    @Serializable
    data object Home : NavDestination

    @Serializable
    data class Detail(val image: Image) : NavDestination
}
