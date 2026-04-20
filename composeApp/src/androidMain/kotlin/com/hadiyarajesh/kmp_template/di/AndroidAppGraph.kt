package com.hadiyarajesh.kmp_template.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.hadiyarajesh.kmp_template.data.database.AppDatabase
import com.hadiyarajesh.kmp_template.data.database.createDatabase
import com.hadiyarajesh.kmp_template.data.download.AndroidImageSaver
import com.hadiyarajesh.kmp_template.data.download.ImageSaver
import com.hadiyarajesh.kmp_template.database.getDatabaseBuilderForAndroid
import com.hadiyarajesh.kmp_template.datastore.createDataStoreForAndroid
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
@DependencyGraph(AppScope::class)
interface AndroidAppGraph : CommonAppGraph {
    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides context: Context): AndroidAppGraph
    }

    @Provides
    @SingleIn(AppScope::class)
    fun provideDataStore(context: Context): DataStore<Preferences> =
        createDataStoreForAndroid(context)

    @Provides
    @SingleIn(AppScope::class)
    fun provideDatabase(context: Context): AppDatabase =
        createDatabase(getDatabaseBuilderForAndroid(context))

    @Binds
    val AndroidImageSaver.bind: ImageSaver
}
