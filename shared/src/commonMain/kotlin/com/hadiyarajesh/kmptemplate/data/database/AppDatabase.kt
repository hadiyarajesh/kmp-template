package com.hadiyarajesh.kmptemplate.data.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.hadiyarajesh.kmptemplate.data.database.dao.ImageDao
import com.hadiyarajesh.kmptemplate.data.database.entity.Image

@Database(
    version = 1,
    entities = [Image::class],
    exportSchema = true
)
@ConstructedBy(AppDatabaseConstructor::class)
internal abstract class AppDatabase : RoomDatabase() {
    abstract fun imageDao(): ImageDao
}

/**
 * The actual implementation of this constructor is not defined in common code.
 * It will be provided by Room for each platform (e.g., Android, iOS).
 */
internal expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

internal fun createDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase = builder
    .setDriver(BundledSQLiteDriver())
    .build()

internal const val DATABASE_FILE_NAME = "kmptemplate.db"
