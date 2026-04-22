package com.hadiyarajesh.kmptemplate

import com.hadiyarajesh.kmptemplate.utility.ImageUtility
import kotlin.test.Test
import kotlin.test.assertTrue

class ComposeAppCommonTest {
    @Test
    fun generatedImageUrlUsesPicsum() {
        assertTrue(ImageUtility.getRandomImageUrl().startsWith("https://picsum.photos/id/"))
    }
}
