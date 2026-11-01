package com.hadiyarajesh.kmptemplate.di

import com.hadiyarajesh.kmptemplate.data.database.AppDatabase
import com.hadiyarajesh.kmptemplate.data.database.dao.ImageDao
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides

@BindingContainer
@ContributesTo(AppScope::class)
internal object DaoProviders {
    @Provides
    fun provideImageDao(appDatabase: AppDatabase): ImageDao = appDatabase.imageDao()
}
