package com.hadiyarajesh.kmptemplate.di

import androidx.compose.runtime.compositionLocalOf
import com.hadiyarajesh.kmptemplate.data.database.AppDatabase
import com.hadiyarajesh.kmptemplate.data.database.dao.ImageDao
import com.hadiyarajesh.kmptemplate.data.repository.HomeRepository
import com.hadiyarajesh.kmptemplate.data.repository.HomeRepositoryImpl
import com.hadiyarajesh.kmptemplate.network.AppHttpClient
import com.hadiyarajesh.kmptemplate.network.PicsumApi
import com.hadiyarajesh.kmptemplate.network.PicsumApiImpl
import com.hadiyarajesh.kmptemplate.ui.detail.DetailViewModel
import com.hadiyarajesh.kmptemplate.ui.home.HomeViewModel
import com.hadiyarajesh.kmptemplate.ui.onboarding.OnboardingViewModel
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.Provides

interface CommonAppGraph {
    fun getOnboardingViewModel(): OnboardingViewModel
    fun getHomeViewModel(): HomeViewModel
    fun getDetailViewModel(): DetailViewModel
    fun getAppHttpClient(): AppHttpClient

    @Binds
    val HomeRepositoryImpl.bind: HomeRepository

    @Binds
    val PicsumApiImpl.bind: PicsumApi

    @Provides
    fun provideImageDao(appDatabase: AppDatabase): ImageDao = appDatabase.imageDao()
}

val LocalAppGraph = compositionLocalOf<CommonAppGraph> {
    error("No ${CommonAppGraph::class.simpleName} was provided")
}
