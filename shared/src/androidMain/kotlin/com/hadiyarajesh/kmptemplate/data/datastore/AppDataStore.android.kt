package com.hadiyarajesh.kmptemplate.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

internal fun createDataStoreForAndroid(context: Context): DataStore<Preferences> = createDataStore {
    context.filesDir
        .resolve(DATA_STORE_FILE_NAME)
        .absolutePath
}
