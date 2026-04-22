package com.hadiyarajesh.kmptemplate

import android.app.Application
import com.hadiyarajesh.kmptemplate.di.AndroidAppGraph
import dev.zacsweers.metro.createGraphFactory

class MyApplication : Application() {
    lateinit var appGraph: AndroidAppGraph
        private set

    override fun onCreate() {
        super.onCreate()
        appGraph = createGraphFactory<AndroidAppGraph.Factory>()
            .create(context = applicationContext)
    }
}

val Application.appGraph: AndroidAppGraph
    get() = (this as MyApplication).appGraph
