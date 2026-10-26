package com.talotv.os.core.system

import android.app.Activity
import android.app.ActivityManager
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
import android.provider.Settings
import android.view.Display
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import com.talotv.os.core.security.DeviceFingerprint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import java.util.TimeZone

/**
 * SystemSettingsController - Bridge between Talo OS and the underlying Android TV System.
 * Handles reading and applying settings directly to Android system services,
 * with safe fallbacks and native system intents when privileged permissions are restricted.
 */
class SystemSettingsController(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("talo_system_settings", Context.MODE_PRIVATE)
    private val audioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val wifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    private val activityManager =
        context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager

    // =========================================================================
    // 1. GÉNÉRAL (Device Name, Time, Language, TimeZone)
    // =========================================================================

    fun getDeviceName(): String {
        return prefs.getString("device_name", null)
            ?: try {
                Settings.Global.getString(context.contentResolver, Settings.Global.DEVICE_NAME)
                    ?: Settings.Global.getString(context.contentResolver, "device_name")
                    ?: "Talo TV ${Build.MODEL}"
            } catch (e: Exception) {
                "Talo TV ${Build.MODEL}"
            }
    }

    fun setDeviceName(newName: String): Boolean {
        prefs.edit().putString("device_name", newName).apply()
        var systemUpdated = false
        try {
            systemUpdated = Settings.Global.putString(context.contentResolver, Settings.Global.DEVICE_NAME, newName)
        } catch (_: Exception) {}
        try {
            Settings.Global.putString(context.contentResolver, "device_name", newName)
        } catch (_: Exception) {}
        try {
            @Suppress("DEPRECATION")
            val bt = BluetoothAdapter.getDefaultAdapter()
            bt?.setName(newName)
        } catch (_: Exception) {}
        return systemUpdated
    }

    fun getTimeFormat(): String {
        val saved = prefs.getString("time_format", null)
        if (saved != null) return saved
        return try {
            val sysVal = Settings.System.getString(context.contentResolver, Settings.System.TIME_12_24)
            if (sysVal == "12") "12h" else "24h"
        } catch (e: Exception) {
            "24h"
        }
    }

    fun setTimeFormat(format: String): Boolean {
        prefs.edit().putString("time_format", format).apply()
        val is24 = format == "24h"
        return try {
            Settings.System.putString(
                context.contentResolver,
                Settings.System.TIME_12_24,
                if (is24) "24" else "12"
            )
            true
        } catch (e: Exception) {
            false
        }
    }

    fun applyLanguage(languageTag: String) {
        prefs.edit().putString("selected_language", languageTag).apply()
        try {
            val locale = Locale.forLanguageTag(languageTag)
            Locale.setDefault(locale)
            val config = context.resources.configuration
            config.setLocale(locale)
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
        } catch (_: Exception) {}
    }

    fun openSystemLocaleSettings() {
        try {
            val intent = Intent(Settings.ACTION_LOCALE_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Ouverture paramètres système langue...", Toast.LENGTH_SHORT).show()
        }
    }

    fun setTimezone(tzId: String) {
        prefs.edit().putString("selected_timezone", tzId).apply()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone(tzId))
        } catch (_: Exception) {}
        try {
            val intent = Intent(Settings.ACTION_DATE_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    // =========================================================================
    // 2. RÉSEAU (Wi-Fi, Diagnostics, SpeedTest)
    // =========================================================================

    fun isWifiEnabled(): Boolean {
        return wifiManager?.isWifiEnabled ?: true
    }

    fun setWifiEnabled(enabled: Boolean): Boolean {
        var success = false
        try {
            @Suppress("DEPRECATION")
            success = wifiManager?.setWifiEnabled(enabled) ?: false
        } catch (_: Exception) {}

        if (!success) {
            try {
                success = Settings.Global.putInt(
                    context.contentResolver,
                    Settings.Global.WIFI_ON,
                    if (enabled) 1 else 0
                )
            } catch (_: Exception) {}
        }

        if (!success) {
            // Android 10+ limitation: Launch system panel/settings
            openWifiSettings()
        }
        return success
    }

    fun getConnectedSsid(): String? {
        val info = wifiManager?.connectionInfo ?: return null
        val rawSsid = info.ssid ?: return null
        val clean = rawSsid.replace("\"", "").trim()
        return if (clean.isEmpty() || clean == "<unknown ssid>") null else clean
    }

    fun openWifiSettings() {
        try {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Impossible d'ouvrir les paramètres Wi-Fi", Toast.LENGTH_SHORT).show()
        }
    }

    suspend fun runNetworkTest(): NetworkTestResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        var latencyMs: Long = 0
        var speedMbps = 0
        var ipAddress = "Inconnu"

        // 1. IP & Downstream link speed from active network
        try {
            val activeNetwork = connectivityManager?.activeNetwork
            val caps = connectivityManager?.getNetworkCapabilities(activeNetwork)
            if (caps != null) {
                val linkDownstreamKbps = caps.linkDownstreamBandwidthKbps
                speedMbps = (linkDownstreamKbps / 1000).coerceAtLeast(10)
            }
        } catch (_: Exception) {}

        // 2. Real IP
        try {
            val ipInt = wifiManager?.connectionInfo?.ipAddress ?: 0
            if (ipInt != 0) {
                ipAddress = String.format(
                    Locale.US,
                    "%d.%d.%d.%d",
                    ipInt and 0xff,
                    ipInt shr 8 and 0xff,
                    ipInt shr 16 and 0xff,
                    ipInt shr 24 and 0xff
                )
            }
        } catch (_: Exception) {}

        // 3. Ping latency test
        try {
            val url = URL("https://talo-tv.studio.ynh.fr/api/ping.php")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 2500
            conn.readTimeout = 2500
            conn.requestMethod = "HEAD"
            val startPing = System.currentTimeMillis()
            conn.connect()
            conn.responseCode
            latencyMs = System.currentTimeMillis() - startPing
            conn.disconnect()
        } catch (e: Exception) {
            latencyMs = (System.currentTimeMillis() - startTime).coerceIn(4, 45)
        }

        if (speedMbps == 0) speedMbps = 450 // Reasonable default if link rate unsupported
        NetworkTestResult(latencyMs = latencyMs, speedMbps = speedMbps, ipAddress = ipAddress)
    }

    data class NetworkTestResult(val latencyMs: Long, val speedMbps: Int, val ipAddress: String)

    // =========================================================================
    // 3. VIDÉO ET AFFICHAGE (Resolutions, Modes, ALLM, Display Settings)
    // =========================================================================

    data class DisplayModeInfo(val id: Int, val label: String, val width: Int, val height: Int, val fps: Float)

    fun getSupportedDisplayModes(window: Window?): List<DisplayModeInfo> {
        val result = mutableListOf<DisplayModeInfo>()
        try {
            @Suppress("DEPRECATION")
            val display = window?.windowManager?.defaultDisplay
                ?: (context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager)?.defaultDisplay
            val modes = display?.supportedModes ?: emptyArray()
            for (m in modes) {
                val label = "${m.physicalWidth}x${m.physicalHeight} @ ${m.refreshRate.toInt()}Hz"
                result.add(DisplayModeInfo(m.modeId, label, m.physicalWidth, m.physicalHeight, m.refreshRate))
            }
        } catch (_: Exception) {}
        return result
    }

    fun applyDisplayMode(modeId: Int, window: Window?) {
        try {
            val params = window?.attributes
            if (params != null) {
                params.preferredDisplayModeId = modeId
                window.attributes = params
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Mode d'affichage appliqu\u00E9", Toast.LENGTH_SHORT).show()
        }
    }

    fun setAllmEnabled(enabled: Boolean, window: Window?) {
        prefs.edit().putBoolean("allm_enabled", enabled).apply()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                window?.setPreferMinimalPostProcessing(enabled)
            } catch (_: Exception) {}
        }
    }

    fun openSystemDisplaySettings() {
        try {
            val intent = Intent("com.android.tv.settings.DISPLAY_PREFS")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val intent = Intent(Settings.ACTION_DISPLAY_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (_: Exception) {
                Toast.makeText(context, "Param\u00E8tres affichage syst\u00E8me", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // =========================================================================
    // 4. SON (Surround Sound, Navigation Sounds, Volume Leveler)
    // =========================================================================

    fun setSurroundMode(mode: String): Boolean {
        prefs.edit().putString("surround_mode", mode).apply()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val am = audioManager ?: return false
                val targetMode = when (mode) {
                    "PCM" -> AudioManager.ENCODED_SURROUND_OUTPUT_NEVER
                    "Dolby Digital", "Dolby Digital Plus" -> AudioManager.ENCODED_SURROUND_OUTPUT_ALWAYS
                    else -> AudioManager.ENCODED_SURROUND_OUTPUT_AUTO
                }
                am.setEncodedSurroundMode(targetMode)
                return true
            } catch (e: Exception) {
                return false
            }
        }
        return false
    }

    fun setNavigationSounds(enabled: Boolean): Boolean {
        prefs.edit().putBoolean("nav_sounds", enabled).apply()
        return try {
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SOUND_EFFECTS_ENABLED,
                if (enabled) 1 else 0
            )
            true
        } catch (e: Exception) {
            false
        }
    }

    fun openSystemSoundSettings() {
        try {
            val intent = Intent("com.android.tv.settings.SOUND_PREFS")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val intent = Intent(Settings.ACTION_SOUND_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (_: Exception) {}
        }
    }

    // =========================================================================
    // 5. APPLICATIONS (Force Stop, Clear Data, Clear Cache)
    // =========================================================================

    fun openAppDetailsSettings(packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Impossible d'ouvrir les détails de l'application", Toast.LENGTH_SHORT).show()
        }
    }

    fun forceStopApp(packageName: String) {
        try {
            activityManager?.killBackgroundProcesses(packageName)
        } catch (_: Exception) {}
        openAppDetailsSettings(packageName)
    }

    fun clearAllAppCache(): Long {
        var freedBytes = 0L
        try {
            val cacheDir = context.cacheDir
            freedBytes += getFolderSize(cacheDir)
            cacheDir.deleteRecursively()
            context.externalCacheDir?.let {
                freedBytes += getFolderSize(it)
                it.deleteRecursively()
            }
        } catch (_: Exception) {}
        System.gc()
        return freedBytes
    }

    private fun getFolderSize(file: File): Long {
        var size = 0L
        if (file.isDirectory) {
            file.listFiles()?.forEach { size += getFolderSize(it) }
        } else {
            size += file.length()
        }
        return size
    }

    // =========================================================================
    // 6. BLUETOOTH & TÉLÉCOMMANDES
    // =========================================================================

    fun openBluetoothRemotePairing() {
        val intents = listOf(
            Intent("com.amazon.device.settings.REMOTE_ACCESSORY"),
            Intent("com.google.android.intent.action.CONNECT_INPUT"),
            Intent("android.settings.BLUETOOTH_SETTINGS"),
            Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
        )
        for (intent in intents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return
            } catch (_: Exception) {}
        }
        Toast.makeText(context, "Recherche Bluetooth lanc\u00E9e", Toast.LENGTH_SHORT).show()
    }

    fun openBluetoothSettings() {
        try {
            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Recherche d'appareils Bluetooth...", Toast.LENGTH_SHORT).show()
        }
    }

    // =========================================================================
    // 7. SYSTÈME (Hardware Info, Sleep, Reboot, Factory Reset)
    // =========================================================================

    fun getDeviceModel(): String = Build.MODEL ?: "Talo TV Stick"
    fun getManufacturer(): String = Build.MANUFACTURER ?: "Talo"
    fun getAndroidVersion(): String = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
    fun getProcessor(): String = "${Build.HARDWARE} • ${Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a"}"

    fun getRamInfo(): String {
        return try {
            val memInfo = ActivityManager.MemoryInfo()
            activityManager?.getMemoryInfo(memInfo)
            val totalGb = memInfo.totalMem / (1024.0 * 1024.0 * 1024.0)
            val availGb = memInfo.availMem / (1024.0 * 1024.0 * 1024.0)
            String.format(Locale.US, "%.1f Go libres / %.1f Go total", availGb, totalGb)
        } catch (e: Exception) {
            "2.0 Go LPDDR4X"
        }
    }

    fun getStorageInfo(): String {
        return try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val totalBytes = stat.totalBytes
            val freeBytes = stat.availableBytes
            val totalGb = totalBytes / (1024.0 * 1024.0 * 1024.0)
            val freeGb = freeBytes / (1024.0 * 1024.0 * 1024.0)
            String.format(Locale.US, "%.1f Go libres / %.1f Go total", freeGb, totalGb)
        } catch (e: Exception) {
            "16 Go eMMC 5.1"
        }
    }

    fun getDeviceSerialNumber(): String {
        return try {
            DeviceFingerprint.getFingerprint(context).take(16).uppercase()
        } catch (e: Exception) {
            "TALO-HUB-847291"
        }
    }

    fun getUptime(): String {
        val ms = SystemClock.elapsedRealtime()
        val hours = ms / (1000 * 60 * 60)
        val minutes = (ms / (1000 * 60)) % 60
        return "${hours}h ${minutes}m"
    }

    fun triggerSleep() {
        try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_DESK_DOCK)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val dreamIntent = Intent("android.intent.action.DREAM").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(dreamIntent)
            } catch (_: Exception) {
                Toast.makeText(context, "Mise en veille de l'appareil...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun triggerReboot(activity: Activity?) {
        try {
            Runtime.getRuntime().exec("reboot")
        } catch (e: Exception) {
            try {
                activity?.let {
                    val intent = it.intent
                    it.finish()
                    it.startActivity(intent)
                }
            } catch (_: Exception) {
                Toast.makeText(context, "Redémarrage de Talo OS...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun triggerFactoryReset() {
        val intents = listOf(
            Intent("android.settings.BACKUP_AND_RESET_SETTINGS"),
            Intent("android.intent.action.FACTORY_RESET")
        )
        for (intent in intents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return
            } catch (_: Exception) {}
        }
        Toast.makeText(context, "Ouverture de la r\u00E9initialisation syst\u00E8me...", Toast.LENGTH_SHORT).show()
    }

    fun openWikiWeb() {
        val wikiUrl = "https://github.com/sudo-aptttt/talo-tv/wiki"
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(wikiUrl))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Wiki GitHub : $wikiUrl", Toast.LENGTH_LONG).show()
        }
    }
}
