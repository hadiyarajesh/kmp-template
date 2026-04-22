package com.hadiyarajesh.kmptemplate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.hadiyarajesh.kmptemplate.di.CommonAppGraph
import com.hadiyarajesh.kmptemplate.di.LocalAppGraph
import com.hadiyarajesh.kmptemplate.navigation.AppNavigation
import com.hadiyarajesh.kmptemplate.ui.theme.AppTheme

@Composable
fun App(appGraph: CommonAppGraph) {
    CompositionLocalProvider(LocalAppGraph provides appGraph) {
        AppTheme {
            AppNavigation()
        }
    }
}
