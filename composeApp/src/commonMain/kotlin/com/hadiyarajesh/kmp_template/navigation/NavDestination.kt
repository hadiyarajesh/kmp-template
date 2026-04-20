package com.hadiyarajesh.kmp_template.navigation

import com.hadiyarajesh.kmp_template.data.database.entity.Image

sealed interface NavDestination {
    data object Onboarding : NavDestination
    data object Home : NavDestination
    data class Detail(val image: Image) : NavDestination
}
