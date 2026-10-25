package com.talotv.os.core.hardware

import android.content.Context
import android.content.SharedPreferences

class ProfileManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("talo_profile_prefs", Context.MODE_PRIVATE)

    fun isCalibrated(): Boolean {
        return prefs.getBoolean("is_calibrated", false)
    }

    fun setCalibrated(calibrated: Boolean) {
        prefs.edit().putBoolean("is_calibrated", calibrated).apply()
    }

    fun getCurrentProfileType(): ProfileType {
        val name = prefs.getString("profile_type", ProfileType.STANDARD.name) ?: ProfileType.STANDARD.name
        return try {
            ProfileType.valueOf(name)
        } catch (e: Exception) {
            ProfileType.STANDARD
        }
    }

    fun setProfileType(type: ProfileType) {
        prefs.edit().putString("profile_type", type.name).apply()
    }

    fun getActiveSettings(): ProfileSettings {
        return when (getCurrentProfileType()) {
            ProfileType.ECO -> ProfileSettings.ECO
            ProfileType.STANDARD -> ProfileSettings.STANDARD
            ProfileType.ULTRA -> ProfileSettings.ULTRA
            ProfileType.CUSTOM -> getCustomSettings()
        }
    }

    fun getCustomSettings(): ProfileSettings {
        return ProfileSettings(
            uiResolution = prefs.getString("custom_ui_resolution", "1080p") ?: "1080p",
            targetFramerate = prefs.getInt("custom_target_framerate", 60),
            enableGlassmorphism = prefs.getBoolean("custom_glassmorphism", true),
            blurRadiusDp = prefs.getInt("custom_blur_radius", 16),
            enableParallax3D = prefs.getBoolean("custom_parallax_3d", true),
            parallaxIntensity = prefs.getFloat("custom_parallax_intensity", 1.0f),
            imageCacheSizeMb = prefs.getInt("custom_image_cache_mb", 80),
            enableDynamicWeatherTopShelf = prefs.getBoolean("custom_dynamic_weather", true)
        )
    }

    fun saveCustomSettings(settings: ProfileSettings) {
        prefs.edit()
            .putString("custom_ui_resolution", settings.uiResolution)
            .putInt("custom_target_framerate", settings.targetFramerate)
            .putBoolean("custom_glassmorphism", settings.enableGlassmorphism)
            .putInt("custom_blur_radius", settings.blurRadiusDp)
            .putBoolean("custom_parallax_3d", settings.enableParallax3D)
            .putFloat("custom_parallax_intensity", settings.parallaxIntensity)
            .putInt("custom_image_cache_mb", settings.imageCacheSizeMb)
            .putBoolean("custom_dynamic_weather", settings.enableDynamicWeatherTopShelf)
            .apply()
    }
}
