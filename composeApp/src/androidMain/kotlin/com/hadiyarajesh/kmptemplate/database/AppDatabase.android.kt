package com.hadiyarajesh.kmptemplate.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.hadiyarajesh.kmptemplate.data.database.AppDatabase
import com.hadiyarajesh.kmptemplate.data.database.DATABASE_FILE_NAME

fun getDatabaseBuilderForAndroid(context: Context): RoomDatabase.Builder<AppDatabase> {
    val dbFile = context.getDatabasePath(DATABASE_FILE_NAME)
    return Room.databaseBuilder<AppDatabase>(
        context = context.applicationContext,
        name = dbFile.absolutePath
    )
}
