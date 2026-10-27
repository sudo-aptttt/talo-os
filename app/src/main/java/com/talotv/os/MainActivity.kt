package com.talotv.os

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.talotv.os.core.security.TaloSecurityVault
import com.talotv.os.core.system.CloudSyncManager
import com.talotv.os.ui.screens.HomeScreen
import com.talotv.os.ui.screens.LostModeScreen
import com.talotv.os.ui.screens.OOBESetupScreen
import com.talotv.os.ui.screens.SearchScreen
import com.talotv.os.ui.screens.SettingsScreen
import com.talotv.os.ui.theme.TaloOSTheme

enum class ScreenState {
    OOBE,
    HOME,
    SETTINGS,
    SEARCH
}

// Android TV Leanback key code for physical TV settings buttons (remote control)
private const val KEYCODE_TV_SETTINGS = 289

class MainActivity : ComponentActivity() {

    private val currentScreen = mutableStateOf(ScreenState.OOBE)
    private val isSettingsPanelOpen = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize hardware-backed security vault & Privacy Shield
        CloudSyncManager.initSecurityState(this)

        // Step 16: Background Cloud Sync, Lost Mode & OS Updates on every launch
        CloudSyncManager.startBackgroundSync(this)

        // Request necessary TV system permissions (Bluetooth Connect/Scan on Android 12+)
        checkAndRequestRequiredPermissions()

        val isCalibrated = (application as TaloApplication).profileManager.isCalibrated()
        val requestedSettings = intent?.getStringExtra("screen") == "settings"
        currentScreen.value = if (isCalibrated) ScreenState.HOME else ScreenState.OOBE
        if (requestedSettings && isCalibrated) {
            isSettingsPanelOpen.value = true
        }

        setContent {
            TaloOSTheme {
                val isLocked by CloudSyncManager.isDeviceLocked.collectAsState()
                val lockMsg by CloudSyncManager.lockMessage.collectAsState()

                Box(modifier = Modifier.fillMaxSize()) {
                    if (isLocked) {
                        // Fortress Lost Mode: Total isolation, MainAppNav is unmounted completely
                        LostModeScreen(
                            lockMessage = lockMsg,
                            onEmergencyUnlock = {
                                CloudSyncManager.unlockLocally(this@MainActivity)
                            }
                        )
                    } else {
                        MainAppNav(
                            currentScreen = currentScreen.value,
                            isSettingsPanelOpen = isSettingsPanelOpen.value,
                            onCloseSettingsPanel = { closeSettingsPanel() },
                            onOpenSettingsPanel = { openSettingsPanel() },
                            onNavigate = { currentScreen.value = it }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Step 16: Re-poll telemetry and cloud updates on resume
        CloudSyncManager.startBackgroundSync(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        if (intent.getBooleanExtra("TEST_LOST_MODE", false)) {
            val msg = intent.getStringExtra("LOCK_MESSAGE") ?: "Ce téléviseur a été verrouillé à distance."
            CloudSyncManager.isTestLockActive = true
            CloudSyncManager.updateLockState(this, true, msg)
            return
        } else if (intent.getBooleanExtra("TEST_UNLOCK", false)) {
            CloudSyncManager.isTestLockActive = false
            CloudSyncManager.unlockLocally(this)
            return
        }

        if (intent.getStringExtra("screen") == "settings") {
            currentScreen.value = ScreenState.HOME
            isSettingsPanelOpen.value = true
            return
        }

        val isCalibrated = (application as TaloApplication).profileManager.isCalibrated()
        if (isCalibrated && !CloudSyncManager.isDeviceLocked.value) {
            currentScreen.value = ScreenState.HOME
        } else if (!isCalibrated) {
            currentScreen.value = ScreenState.OOBE
        }
    }

    fun openSettingsPanel() {
        if (currentScreen.value != ScreenState.HOME) {
            currentScreen.value = ScreenState.HOME
        }
        isSettingsPanelOpen.value = true
    }

    fun closeSettingsPanel() {
        isSettingsPanelOpen.value = false
        if (currentScreen.value == ScreenState.SETTINGS) {
            currentScreen.value = ScreenState.HOME
        }
    }

    fun toggleSettingsPanel() {
        if (currentScreen.value != ScreenState.HOME) {
            currentScreen.value = ScreenState.HOME
            isSettingsPanelOpen.value = true
        } else {
            isSettingsPanelOpen.value = !isSettingsPanelOpen.value
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        // Fortress Mode: when locked, block all escape keys (Back, Home, Menu, AppSwitch, Settings)
        if (CloudSyncManager.isDeviceLocked.value) {
            val keyCode = event.keyCode
            val isNavigationEscape = keyCode == KeyEvent.KEYCODE_BACK ||
                    keyCode == KeyEvent.KEYCODE_HOME ||
                    keyCode == KeyEvent.KEYCODE_MENU ||
                    keyCode == KeyEvent.KEYCODE_SETTINGS ||
                    keyCode == KEYCODE_TV_SETTINGS ||
                    keyCode == KeyEvent.KEYCODE_APP_SWITCH ||
                    keyCode == KeyEvent.KEYCODE_WINDOW ||
                    keyCode == KeyEvent.KEYCODE_SEARCH ||
                    keyCode == KeyEvent.KEYCODE_VOICE_ASSIST

            if (isNavigationEscape) {
                return true // Consume and strictly block
            }
        }

        val keyCode = event.keyCode
        val isSettingsKey = keyCode == KeyEvent.KEYCODE_SETTINGS ||
                keyCode == KEYCODE_TV_SETTINGS ||
                keyCode == KeyEvent.KEYCODE_MENU

        if (isSettingsKey) {
            if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                toggleSettingsPanel()
            }
            return true
        }

        return super.dispatchKeyEvent(event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (CloudSyncManager.isDeviceLocked.value) {
            if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
                return true
            }
        }

        if (keyCode == KeyEvent.KEYCODE_SETTINGS ||
            keyCode == KEYCODE_TV_SETTINGS ||
            keyCode == KeyEvent.KEYCODE_MENU
        ) {
            if (event?.repeatCount == 0) {
                toggleSettingsPanel()
            }
            return true
        }

        if (keyCode == KeyEvent.KEYCODE_HOME) {
            closeSettingsPanel()
            currentScreen.value = ScreenState.HOME
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun checkAndRequestRequiredPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val permissionsToRequest = mutableListOf<String>()
            if (checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(android.Manifest.permission.BLUETOOTH_CONNECT)
            }
            if (checkSelfPermission(android.Manifest.permission.BLUETOOTH_SCAN) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(android.Manifest.permission.BLUETOOTH_SCAN)
            }
            if (permissionsToRequest.isNotEmpty()) {
                requestPermissions(permissionsToRequest.toTypedArray(), 1002)
            }
        }
    }
}

@Composable
fun MainAppNav(
    currentScreen: ScreenState,
    isSettingsPanelOpen: Boolean,
    onCloseSettingsPanel: () -> Unit,
    onOpenSettingsPanel: () -> Unit,
    onNavigate: (ScreenState) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    when (currentScreen) {
        ScreenState.OOBE -> {
            OOBESetupScreen(
                onSetupComplete = {
                    onNavigate(ScreenState.HOME)
                }
            )
        }
        ScreenState.HOME, ScreenState.SETTINGS -> {
            HomeScreen(
                isSettingsOpen = isSettingsPanelOpen || (currentScreen == ScreenState.SETTINGS),
                onOpenSettings = onOpenSettingsPanel,
                onCloseSettings = onCloseSettingsPanel,
                onNavigateToSettings = onOpenSettingsPanel,
                onNavigateToSearch = {
                    onNavigate(ScreenState.SEARCH)
                },
                onNavigateToStore = {
                    TaloApplication.instance.installedAppsManager.launchStore(context)
                }
            )
            // On the home screen, intercept Back button to do nothing (prevent exiting to bloated launcher)
            BackHandler(enabled = !isSettingsPanelOpen && currentScreen != ScreenState.SETTINGS) {
                // Keep Talo OS active as standalone OS
            }
        }
        ScreenState.SEARCH -> {
            SearchScreen(
                onNavigateBack = {
                    onNavigate(ScreenState.HOME)
                },
                onAppSelected = { app ->
                    onNavigate(ScreenState.HOME)
                    if (app.id == "app-store" || app.packageName == "com.android.vending") {
                        TaloApplication.instance.installedAppsManager.launchStore(context)
                    } else if (app.id == "dock-settings" || app.id == "app-settings" || app.packageName == "com.talotv.settings") {
                        onOpenSettingsPanel()
                    } else {
                        TaloApplication.instance.installedAppsManager.launchPackage(context, app.packageName, app.name)
                    }
                }
            )
            BackHandler(enabled = true) {
                onNavigate(ScreenState.HOME)
            }
        }
    }
}
