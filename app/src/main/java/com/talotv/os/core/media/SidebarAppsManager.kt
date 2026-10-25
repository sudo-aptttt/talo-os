package com.talotv.os.core.media

import android.content.Context
import android.content.SharedPreferences
import com.talotv.os.core.apps.InstalledApp

/**
 * Manages the 4 pinned / favorite apps displayed in the left tvOS sidebar drawer.
 * Supports:
 * - MODE_MOST_USED: dynamically ranks apps by launch frequency
 * - MODE_CUSTOM: user-curated 4 apps selected in Settings
 */
class SidebarAppsManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("talo_sidebar_prefs", Context.MODE_PRIVATE)

    private val usagePrefs: SharedPreferences =
        context.getSharedPreferences("talo_app_usage_counter", Context.MODE_PRIVATE)

    companion object {
        const val MODE_MOST_USED = "most_used"
        const val MODE_CUSTOM = "custom"
        private const val KEY_MODE = "sidebar_mode"
        private const val KEY_CUSTOM_APPS = "sidebar_custom_apps"

        // Default to real installed TV apps
        val DEFAULT_CUSTOM_APP_IDS = listOf(
            "dock-tv",
            "dock-youtube",
            "dock-music",
            "pkg-com.google.android.play.games"
        )
    }

    fun recordAppLaunch(appId: String) {
        val current = usagePrefs.getInt(appId, 0)
        usagePrefs.edit().putInt(appId, current + 1).apply()
    }

    fun getAppUsageCount(appId: String): Int {
        return usagePrefs.getInt(appId, 0)
    }

    fun getSidebarMode(): String {
        return prefs.getString(KEY_MODE, MODE_MOST_USED) ?: MODE_MOST_USED
    }

    fun setSidebarMode(mode: String) {
        prefs.edit().putString(KEY_MODE, mode).apply()
    }

    fun getCustomAppIds(): List<String> {
        val raw = prefs.getString(KEY_CUSTOM_APPS, null)
        return if (!raw.isNullOrBlank()) {
            raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        } else {
            DEFAULT_CUSTOM_APP_IDS
        }
    }

    fun setCustomAppIds(appIds: List<String>) {
        val joined = appIds.take(4).joinToString(",")
        prefs.edit().putString(KEY_CUSTOM_APPS, joined).apply()
    }

    fun toggleCustomApp(appId: String) {
        val current = getCustomAppIds().toMutableList()
        if (current.contains(appId)) {
            if (current.size > 1) {
                current.remove(appId)
            }
        } else {
            if (current.size < 4) {
                current.add(appId)
            } else {
                current.removeAt(0)
                current.add(appId)
            }
        }
        setCustomAppIds(current)
    }

    fun getSidebarApps(
        installedApps: List<InstalledApp> = emptyList(),
        dockApps: List<DockAppItem> = emptyList()
    ): List<DockAppItem> {
        val installedDockItems = installedApps.map { it.toDockAppItem() }
        val allMap = (dockApps + installedDockItems).distinctBy { it.id }.associateBy { it.id }

        val mode = getSidebarMode()
        if (mode == MODE_CUSTOM) {
            val customIds = getCustomAppIds()
            val result = customIds.mapNotNull { allMap[it] }.toMutableList()
            if (result.size < 4) {
                allMap.values.filter { it.id != "dock-settings" && it.id != "app-settings" }.forEach { item ->
                    if (result.size < 4 && result.none { it.id == item.id }) {
                        result.add(item)
                    }
                }
            }
            return result.take(4)
        }

        // MODE_MOST_USED:
        val candidates = allMap.values
            .filter { it.id != "dock-settings" && it.id != "app-settings" && it.id != "app-search" }

        val sorted = candidates.sortedByDescending { getAppUsageCount(it.id) }
        val result = mutableListOf<DockAppItem>()

        sorted.filter { getAppUsageCount(it.id) > 0 }.forEach {
            if (result.size < 4 && result.none { existing -> existing.id == it.id }) {
                result.add(it)
            }
        }

        candidates.forEach { item ->
            if (result.size < 4 && result.none { it.id == item.id }) {
                result.add(item)
            }
        }

        return result.take(4)
    }
}

fun InstalledApp.toDockAppItem(): DockAppItem {
    val iconType = when {
        isStore -> DockIconType.STORE
        isSettings -> DockIconType.SETTINGS
        packageName.contains("youtube.tvmusic") -> DockIconType.MUSIC
        packageName.contains("youtube") -> DockIconType.YOUTUBE
        packageName.contains("games") -> DockIconType.ARCADE
        packageName.contains(".tv") -> DockIconType.TV
        else -> DockIconType.TV
    }
    return DockAppItem(
        id = id,
        name = name,
        category = if (isStore) "Boutique" else if (isSettings) "Système" else "Application",
        packageName = packageName,
        iconType = iconType,
        bgGradient = when (iconType) {
            DockIconType.STORE -> listOf(0xFF2C2C2E, 0xFF1C1C1E)
            DockIconType.YOUTUBE -> listOf(0xFFFF0000, 0xFF990000)
            DockIconType.MUSIC -> listOf(0xFFFF2D55, 0xFFCC113B)
            DockIconType.ARCADE -> listOf(0xFFFF3366, 0xFFFF5E3A)
            DockIconType.SETTINGS -> listOf(0xFFFFFFFF, 0xFFFFFFFF)
            else -> listOf(0xFF1C1D24, 0xFF12131A)
        },
        spotlight = MediaSpotlight(
            id = "spot-$id",
            appKey = id,
            title = name,
            badge = "Application Android TV",
            tagline = "Lancer $name sur votre téléviseur.",
            category = "Application",
            gradientColors = listOf(0xFF242426, 0xFF18181A, 0xFF0D0D0E)
        ),
        iconBitmap = iconBitmap,
        isBanner = isBanner
    )
}
