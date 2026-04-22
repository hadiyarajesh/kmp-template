plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.androidx.room) apply false
    alias(libs.plugins.ktlint) apply false
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    val allowedSourceKtlintCheckTaskNames = listOf(
        "runKtlintCheckOverCommonMainSourceSet",
        "runKtlintCheckOverCommonTestSourceSet",
        "runKtlintCheckOverAndroidMainSourceSet",
        "runKtlintCheckOverAndroidUnitTestSourceSet",
        "runKtlintCheckOverAndroidInstrumentedTestSourceSet",
        "runKtlintCheckOverIosMainSourceSet",
        "runKtlintCheckOverIosTestSourceSet",
        "runKtlintCheckOverKotlinScripts"
    )

    val allowedSourceKtlintFormatTaskNames = listOf(
        "runKtlintFormatOverCommonMainSourceSet",
        "runKtlintFormatOverCommonTestSourceSet",
        "runKtlintFormatOverAndroidMainSourceSet",
        "runKtlintFormatOverAndroidUnitTestSourceSet",
        "runKtlintFormatOverAndroidInstrumentedTestSourceSet",
        "runKtlintFormatOverIosMainSourceSet",
        "runKtlintFormatOverIosTestSourceSet",
        "runKtlintFormatOverKotlinScripts"
    )

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        android.set(true)
        outputToConsole.set(true)
        reporters {
            reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
            reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
        }
        filter {
            exclude("**/build/**")
            exclude("**/generated/**")
        }
    }

    tasks.register("ktlintSourceCheck") {
        dependsOn(tasks.matching { it.name in allowedSourceKtlintCheckTaskNames })
    }

    tasks.register("ktlintSourceFormat") {
        dependsOn(tasks.matching { it.name in allowedSourceKtlintFormatTaskNames })
    }
}
