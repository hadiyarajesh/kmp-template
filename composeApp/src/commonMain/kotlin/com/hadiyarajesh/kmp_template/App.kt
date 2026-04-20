package com.hadiyarajesh.kmp_template

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.hadiyarajesh.kmp_template.di.CommonAppGraph
import com.hadiyarajesh.kmp_template.di.LocalAppGraph
import com.hadiyarajesh.kmp_template.navigation.AppNavigation
import com.hadiyarajesh.kmp_template.ui.theme.AppTheme

@Composable
fun App(appGraph: CommonAppGraph) {
    CompositionLocalProvider(LocalAppGraph provides appGraph) {
        AppTheme {
            AppNavigation()
        }
    }
}
