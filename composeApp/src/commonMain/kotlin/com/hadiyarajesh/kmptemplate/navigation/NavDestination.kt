package com.hadiyarajesh.kmptemplate.navigation

import com.hadiyarajesh.kmptemplate.data.database.entity.Image

sealed interface NavDestination {
    data object Onboarding : NavDestination
    data object Home : NavDestination
    data class Detail(val image: Image) : NavDestination
}
