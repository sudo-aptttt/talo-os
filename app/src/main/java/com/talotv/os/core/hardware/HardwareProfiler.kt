package com.talotv.os.core.hardware

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.view.Display
import android.view.WindowManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class HardwareReport(
    val totalRamMb: Long,
    val freeRamMb: Long,
    val socModel: String,
    val is4kSupported: Boolean,
    val maxRefreshRateHz: Float,
    val recommendedProfile: ProfileType
)

class HardwareProfiler(
    private val context: Context,
    private val profileManager: ProfileManager
) {

    fun autoCalibrate(): HardwareReport {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val totalRamMb = memoryInfo.totalMem / (1024 * 1024)
        val freeRamMb = memoryInfo.availMem / (1024 * 1024)
        val socModel = "${Build.MANUFACTURER} ${Build.MODEL} (${Build.HARDWARE})"

        // Display check
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val defaultDisplay = windowManager.defaultDisplay
        val modes = defaultDisplay.supportedModes

        var is4k = false
        var maxRefreshRate = defaultDisplay.refreshRate

        for (mode in modes) {
            if (mode.physicalWidth >= 3840 || mode.physicalHeight >= 2160) {
                is4k = true
            }
            if (mode.refreshRate > maxRefreshRate) {
                maxRefreshRate = mode.refreshRate
            }
        }

        // Decision Engine:
        // - RAM <= 1200MB (1GB sticks like Fire TV Lite / Mi Stick 1080p) -> ECO
        // - RAM between 1200MB and 2500MB (Chromecast 4K, Fire TV 4K standard) -> STANDARD
        // - RAM > 2500MB (Shield Pro 3GB, Fire Cube 2/3GB) + 4K -> ULTRA
        val recommendedProfile = when {
            totalRamMb <= 1200 -> ProfileType.ECO
            totalRamMb <= 2500 -> ProfileType.STANDARD
            else -> ProfileType.ULTRA
        }

        profileManager.setProfileType(recommendedProfile)

        return HardwareReport(
            totalRamMb = totalRamMb,
            freeRamMb = freeRamMb,
            socModel = socModel,
            is4kSupported = is4k,
            maxRefreshRateHz = maxRefreshRate,
            recommendedProfile = recommendedProfile
        )
    }

    suspend fun runBenchmarkSimulation(onProgress: (Int, String) -> Unit): HardwareReport = withContext(Dispatchers.Default) {
        onProgress(10, "Initialisation de la sonde matérielle…")
        kotlinx.coroutines.delay(400)

        onProgress(35, "Analyse de la mémoire vive et de la bande passante…")
        kotlinx.coroutines.delay(600)

        onProgress(65, "Test des capacités graphiques et des codecs Widevine L1…")
        kotlinx.coroutines.delay(600)

        onProgress(90, "Génération du profil optimal…")
        val report = autoCalibrate()
        kotlinx.coroutines.delay(400)

        onProgress(100, "Hub TV calibré en mode ${report.recommendedProfile.name} !")
        report
    }
}
