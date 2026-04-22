package com.hadiyarajesh.kmptemplate.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.hadiyarajesh.kmptemplate.di.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Inject
@SingleIn(AppScope::class)
class DataStoreManager(private val dataStore: DataStore<Preferences>) {
    private val hasSeenOnboardingPreferenceKey = booleanPreferencesKey("has_seen_onboarding")

    val hasSeenOnboardingFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[hasSeenOnboardingPreferenceKey] ?: false
    }

    suspend fun markOnboardingSeen() {
        dataStore.edit { preferences ->
            preferences[hasSeenOnboardingPreferenceKey] = true
        }
    }
}
