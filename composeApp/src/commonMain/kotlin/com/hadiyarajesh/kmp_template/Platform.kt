package com.hadiyarajesh.kmp_template

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform