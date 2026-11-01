package com.hadiyarajesh.kmptemplate

import androidx.compose.ui.window.ComposeUIViewController
import com.hadiyarajesh.kmptemplate.di.IosAppGraph
import dev.zacsweers.metro.createGraph
import platform.UIKit.UIViewController

private val appGraph: IosAppGraph by lazy { createGraph<IosAppGraph>() }

@Suppress("ktlint:standard:function-naming")
public fun MainViewController(): UIViewController = ComposeUIViewController { App(appGraph) }
