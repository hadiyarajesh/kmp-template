package com.hadiyarajesh.kmp_template

import com.hadiyarajesh.kmp_template.utility.ImageUtility
import kotlin.test.Test
import kotlin.test.assertTrue

class ComposeAppCommonTest {
    @Test
    fun generatedImageUrlUsesPicsum() {
        assertTrue(ImageUtility.getRandomImageUrl().startsWith("https://picsum.photos/id/"))
    }
}
