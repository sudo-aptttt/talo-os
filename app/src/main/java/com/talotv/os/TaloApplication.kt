package com.talotv.os

import android.app.Application
import com.talotv.os.core.apps.InstalledAppsManager
import com.talotv.os.core.audio.SoundEffectManager
import com.talotv.os.core.hardware.HardwareProfiler
import com.talotv.os.core.hardware.ProfileManager
import com.talotv.os.core.media.SidebarAppsManager

class TaloApplication : Application() {

    lateinit var hardwareProfiler: HardwareProfiler
        private set

    lateinit var profileManager: ProfileManager
        private set

    lateinit var soundEffectManager: SoundEffectManager
        private set

    lateinit var sidebarAppsManager: SidebarAppsManager
        private set

    lateinit var installedAppsManager: InstalledAppsManager
        private set

    lateinit var userProfileManager: com.talotv.os.core.profile.UserProfileManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize Core Engines
        profileManager = ProfileManager(this)
        hardwareProfiler = HardwareProfiler(this, profileManager)
        soundEffectManager = SoundEffectManager(this)
        sidebarAppsManager = SidebarAppsManager(this)
        installedAppsManager = InstalledAppsManager(this)
        userProfileManager = com.talotv.os.core.profile.UserProfileManager(this)

        // Perform initial hardware scan if not already calibrated
        if (!profileManager.isCalibrated()) {
            hardwareProfiler.autoCalibrate()
        }
    }

    companion object {
        lateinit var instance: TaloApplication
            private set
    }
}
