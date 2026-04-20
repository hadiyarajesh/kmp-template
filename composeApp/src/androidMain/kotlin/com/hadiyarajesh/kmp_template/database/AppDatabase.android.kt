package com.hadiyarajesh.kmp_template.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.hadiyarajesh.kmp_template.data.database.AppDatabase
import com.hadiyarajesh.kmp_template.data.database.databaseFileName

fun getDatabaseBuilderForAndroid(context: Context): RoomDatabase.Builder<AppDatabase> {
    val dbFile = context.getDatabasePath(databaseFileName)
    return Room.databaseBuilder<AppDatabase>(
        context = context.applicationContext,
        name = dbFile.absolutePath
    )
}
