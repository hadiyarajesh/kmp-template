package com.hadiyarajesh.kmp_template.database

import androidx.room.Room
import androidx.room.RoomDatabase
import com.hadiyarajesh.kmp_template.data.database.AppDatabase
import com.hadiyarajesh.kmp_template.data.database.databaseFileName
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
fun getDatabaseBuilderForIos(): RoomDatabase.Builder<AppDatabase> {
    val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )

    return Room.databaseBuilder<AppDatabase>(
        name = requireNotNull(documentDirectory?.path) + "/$databaseFileName"
    )
}
