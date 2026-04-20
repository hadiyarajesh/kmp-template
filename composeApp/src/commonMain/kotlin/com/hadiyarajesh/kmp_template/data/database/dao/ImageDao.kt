package com.hadiyarajesh.kmp_template.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.hadiyarajesh.kmp_template.data.database.entity.Image
import kotlinx.coroutines.flow.Flow

@Dao
interface ImageDao {
    @Upsert
    suspend fun insertOrUpdateImage(image: Image)

    @Query("SELECT * FROM images LIMIT 1")
    fun getImage(): Flow<Image?>

    @Query("SELECT COUNT(*) FROM images")
    suspend fun imageCount(): Int
}
