package com.hadiyarajesh.kmp_template.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.hadiyarajesh.kmp_template.data.database.AppDatabase
import com.hadiyarajesh.kmp_template.data.database.createDatabase
import com.hadiyarajesh.kmp_template.data.download.ImageSaver
import com.hadiyarajesh.kmp_template.data.download.IosImageSaver
import com.hadiyarajesh.kmp_template.database.getDatabaseBuilderForIos
import com.hadiyarajesh.kmp_template.datastore.createDataStoreForIos
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
@DependencyGraph(AppScope::class)
interface IosAppGraph : CommonAppGraph {
    @Provides
    @SingleIn(AppScope::class)
    fun provideDataStore(): DataStore<Preferences> = createDataStoreForIos()

    @Provides
    @SingleIn(AppScope::class)
    fun provideDatabase(): AppDatabase = createDatabase(getDatabaseBuilderForIos())

    @Binds
    val IosImageSaver.bind: ImageSaver
}
