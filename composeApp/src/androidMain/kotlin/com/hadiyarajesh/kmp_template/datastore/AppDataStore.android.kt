package com.hadiyarajesh.kmp_template.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

fun createDataStoreForAndroid(context: Context): DataStore<Preferences> {
    return createDataStore {
        context.filesDir
            .resolve(dataStoreFileName)
            .absolutePath
    }
}
