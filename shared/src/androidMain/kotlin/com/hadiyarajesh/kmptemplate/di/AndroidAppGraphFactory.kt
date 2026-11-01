package com.hadiyarajesh.kmptemplate.di

import android.content.Context
import dev.zacsweers.metro.createGraphFactory

/**
 * Metro graph creation lives in this module because the compiler plugin only rewrites
 * `createGraphFactory` calls in modules where it is applied.
 */
public fun createAndroidAppGraph(context: Context): CommonAppGraph =
    createGraphFactory<AndroidAppGraph.Factory>().create(context.applicationContext)
