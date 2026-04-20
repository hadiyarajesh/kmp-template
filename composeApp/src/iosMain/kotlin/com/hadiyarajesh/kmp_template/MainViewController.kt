package com.hadiyarajesh.kmp_template

import androidx.compose.ui.window.ComposeUIViewController
import com.hadiyarajesh.kmp_template.di.IosAppGraph
import dev.zacsweers.metro.createGraph

private val appGraph: IosAppGraph by lazy { createGraph<IosAppGraph>() }

fun MainViewController() = ComposeUIViewController { App(appGraph) }
