package com.hadiyarajesh.kmp_template.di

import androidx.compose.runtime.compositionLocalOf
import com.hadiyarajesh.kmp_template.data.database.AppDatabase
import com.hadiyarajesh.kmp_template.data.database.dao.ImageDao
import com.hadiyarajesh.kmp_template.data.repository.HomeRepository
import com.hadiyarajesh.kmp_template.data.repository.HomeRepositoryImpl
import com.hadiyarajesh.kmp_template.network.AppHttpClient
import com.hadiyarajesh.kmp_template.network.PicsumApi
import com.hadiyarajesh.kmp_template.network.PicsumApiImpl
import com.hadiyarajesh.kmp_template.ui.detail.DetailViewModel
import com.hadiyarajesh.kmp_template.ui.home.HomeViewModel
import com.hadiyarajesh.kmp_template.ui.onboarding.OnboardingViewModel
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
