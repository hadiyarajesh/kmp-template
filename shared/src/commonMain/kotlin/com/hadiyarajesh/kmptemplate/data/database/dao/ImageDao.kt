package com.hadiyarajesh.kmptemplate.data.database.dao

import androidx.room.Dao
import androidx.room.Upsert
import com.hadiyarajesh.kmptemplate.data.database.entity.Image

@Dao
internal interface ImageDao {
    @Upsert
    suspend fun insertOrUpdateImage(image: Image)
}
