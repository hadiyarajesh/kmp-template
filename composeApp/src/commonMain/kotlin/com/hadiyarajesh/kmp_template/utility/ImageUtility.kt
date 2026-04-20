package com.hadiyarajesh.kmp_template.utility

import kotlin.random.Random

object ImageUtility {
    private const val IMAGE_WIDTH = "720"
    private const val IMAGE_HEIGHT = "720"

    private val randomImageId: Int
        get() = Random.nextInt(1, 201)

    fun getRandomImageUrl(): String {
        return "https://picsum.photos/id/$randomImageId/$IMAGE_WIDTH/$IMAGE_HEIGHT"
    }
}
