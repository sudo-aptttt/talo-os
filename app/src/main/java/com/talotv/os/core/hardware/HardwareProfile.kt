package com.talotv.os.core.hardware

enum class ProfileType {
    ECO,
    STANDARD,
    ULTRA,
    CUSTOM
}

data class ProfileSettings(
    val uiResolution: String = "1080p", // "1080p" or "4K"
    val targetFramerate: Int = 60, // 60 or 120
    val enableGlassmorphism: Boolean = true,
    val blurRadiusDp: Int = 16,
    val enableParallax3D: Boolean = true,
    val parallaxIntensity: Float = 1.0f,
    val imageCacheSizeMb: Int = 80,
    val enableDynamicWeatherTopShelf: Boolean = true
) {
    companion object {
        val ECO = ProfileSettings(
            uiResolution = "1080p",
            targetFramerate = 60,
            enableGlassmorphism = false,
            blurRadiusDp = 0,
            enableParallax3D = false,
            parallaxIntensity = 0f,
            imageCacheSizeMb = 32,
            enableDynamicWeatherTopShelf = false
        )

        val STANDARD = ProfileSettings(
            uiResolution = "1080p",
            targetFramerate = 60,
            enableGlassmorphism = true,
            blurRadiusDp = 12,
            enableParallax3D = true,
            parallaxIntensity = 0.7f,
            imageCacheSizeMb = 80,
            enableDynamicWeatherTopShelf = true
        )

        val ULTRA = ProfileSettings(
            uiResolution = "4K",
            targetFramerate = 60,
            enableGlassmorphism = true,
            blurRadiusDp = 24,
            enableParallax3D = true,
            parallaxIntensity = 1.0f,
            imageCacheSizeMb = 256,
            enableDynamicWeatherTopShelf = true
        )
    }
}
