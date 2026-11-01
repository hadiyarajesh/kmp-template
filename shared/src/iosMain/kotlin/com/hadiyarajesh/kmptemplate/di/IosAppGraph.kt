package com.hadiyarajesh.kmptemplate.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.hadiyarajesh.kmptemplate.data.database.AppDatabase
import com.hadiyarajesh.kmptemplate.data.database.createDatabase
import com.hadiyarajesh.kmptemplate.database.getDatabaseBuilderForIos
import com.hadiyarajesh.kmptemplate.datastore.createDataStoreForIos
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
}
