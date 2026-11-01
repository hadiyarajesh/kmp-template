package com.hadiyarajesh.kmptemplate

import androidx.compose.ui.window.ComposeUIViewController
import com.hadiyarajesh.kmptemplate.di.IosAppGraph
import dev.zacsweers.metro.createGraph

private val appGraph: IosAppGraph by lazy { createGraph<IosAppGraph>() }

@Suppress("ktlint:standard:function-naming")
fun MainViewController() = ComposeUIViewController { App(appGraph) }
