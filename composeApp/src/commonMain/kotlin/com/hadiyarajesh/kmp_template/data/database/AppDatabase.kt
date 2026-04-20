package com.hadiyarajesh.kmp_template.data.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.hadiyarajesh.kmp_template.data.database.dao.ImageDao
import com.hadiyarajesh.kmp_template.data.database.entity.Image

@Database(
    version = 1,
    entities = [Image::class],
    exportSchema = true
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun imageDao(): ImageDao
}

/**
 * The actual implementation of this constructor is not defined in common code.
 * It will be provided by Room for each platform (e.g., Android, iOS).
 */
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

fun createDatabase(
    builder: RoomDatabase.Builder<AppDatabase>
): AppDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .build()
}

const val databaseFileName = "kmp_template.db"
