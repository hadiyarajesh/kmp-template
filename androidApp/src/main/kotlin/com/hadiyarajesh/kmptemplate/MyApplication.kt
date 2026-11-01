package com.hadiyarajesh.kmptemplate

import android.app.Application
import com.hadiyarajesh.kmptemplate.di.CommonAppGraph
import com.hadiyarajesh.kmptemplate.di.createAndroidAppGraph

class MyApplication : Application() {
    lateinit var appGraph: CommonAppGraph
        private set

    override fun onCreate() {
        super.onCreate()
        appGraph = createAndroidAppGraph(this)
    }
}

val Application.appGraph: CommonAppGraph
    get() = (this as MyApplication).appGraph
