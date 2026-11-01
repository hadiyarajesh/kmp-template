package com.hadiyarajesh.kmptemplate.di

import com.hadiyarajesh.kmptemplate.data.database.AppDatabase
import com.hadiyarajesh.kmptemplate.data.database.dao.ImageDao
import dev.zacsweers.metro.Provides
import dev.zacsweers.metrox.viewmodel.ViewModelGraph

/**
 * Platform graphs extend this. Classes annotated with `@ContributesBinding` or
 * `@ContributesIntoMap(AppScope::class)` are merged in automatically, so only bindings that
 * need construction logic live here.
 */
interface CommonAppGraph : ViewModelGraph {
    @Provides
    fun provideImageDao(appDatabase: AppDatabase): ImageDao = appDatabase.imageDao()
}
