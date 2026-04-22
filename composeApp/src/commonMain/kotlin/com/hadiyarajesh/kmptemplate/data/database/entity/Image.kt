package com.hadiyarajesh.kmptemplate.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

@Serializable
@Entity(tableName = "images")
data class Image(
    @PrimaryKey
    val imageId: String,
    val author: String,
    val width: Int,
    val height: Int,
    val sourceUrl: String,
    val downloadUrl: String,
    val description: String,
    val altText: String
) {
    fun thumbnailUrl(targetWidth: Int = 500, targetHeight: Int = 750): String =
        "https://picsum.photos/id/$imageId/$targetWidth/$targetHeight"

    fun detailImageUrl(targetWidth: Int = 1200): String {
        val targetHeight = if (width > 0) {
            (targetWidth * (height.toDouble() / width.toDouble()))
                .roundToInt()
                .coerceAtLeast(1)
        } else {
            targetWidth
        }

        return "https://picsum.photos/id/$imageId/$targetWidth/$targetHeight"
    }
}
