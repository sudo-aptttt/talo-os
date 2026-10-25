package com.talotv.os.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.talotv.os.R
import com.talotv.os.TaloApplication
import com.talotv.os.core.apps.InstalledApp
import com.talotv.os.core.apps.InstalledAppsManager
import com.talotv.os.core.hardware.ProfileManager
import com.talotv.os.core.hardware.ProfileType
import com.talotv.os.core.profile.UserProfile
import com.talotv.os.core.profile.UserProfileManager
import com.talotv.os.core.security.DeviceFingerprint
import com.talotv.os.core.system.SystemSettingsController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// =========================================================================
// Apple TV tvOS Design Palette
// =========================================================================
private val AppleDarkBackground = Color(0xFF0F1015)
private val AppleCardBackground = Color(0x33282A36)
private val AppleCardBorder = Color(0x22FFFFFF)
private val AppleAccent = Color(0xFFFFFFFF)
private val AppleTextPrimary = Color(0xFFFFFFFF)
private val AppleTextSecondary = Color(0xFF8E8E93)

/**
 * 9 Level 0 Categories
 */
enum class SettingsCategory(val title: String) {
    GENERAL("G\u00E9n\u00E9ral"),
    USERS("Utilisateur et comptes"),
    NETWORK("R\u00E9seau"),
    VIDEO_AUDIO("Vid\u00E9o et Audio"),
    APPS("Applications"),
    NOTIFICATIONS("Notification"),
    REMOTES("T\u00E9l\u00E9commandes et appareils"),
    SYSTEM("Syst\u00E8me"),
    HELP("Aide")
}

/**
 * Sub-Menu Identifiers for Level 2 (Sous-sous menu)
 */
enum class SubMenuId(val title: String) {
    LANGUAGES("Langue"),
    TIMEZONES("Fuseau horaire"),
    PROFILES("Profils"),
    WIFI_LIST("Liste des r\u00E9seaux Wi-Fi"),
    NETWORK_TEST("\u00C9tat du r\u00E9seau"),
    PROFILE_PICKER("Profil"),
    DISPLAY("Affichage"),
    AUDIO("Son"),
    MANAGE_APPS("G\u00E9rer les applications"),
    APPSTORE("AppStore"),
    BLUETOOTH_REMOTES("Jumeler une t\u00E9l\u00E9commande"),
    BLUETOOTH_DEVICES("Autres appareils Bluetooth"),
    ABOUT_HUB("\u00C0 propos"),
    LEGAL("Mentions l\u00E9gales"),
    WIKI_HELP("Documentation en ligne")
}

/**
 * Option Pickers for Level 3 (Sous-sous-sous menu)
 */
enum class OptionPickerId(val title: String) {
    RESOLUTION("R\u00E9solution"),
    COLOR_DEPTH("Profondeur de couleur"),
    COLOR_FORMAT("Format de couleur"),
    DYNAMIC_RANGE("Plage dynamique"),
    ALLM_MODE("Mode Jeu / ALLM"),
    STANDBY_BEHAVIOR("Mode veille"),
    SURROUND_SOUND("Son Surround")
}

sealed class SettingsDialog {
    object EditDeviceName : SettingsDialog()
    object UnregisterAccount : SettingsDialog()
    object ForgetNetwork : SettingsDialog()
    object ClearAllAppsCache : SettingsDialog()
    data class AppActions(val appName: String, val packageName: String) : SettingsDialog()
    object Reboot : SettingsDialog()
    object Reset : SettingsDialog()
    object CreateProfile : SettingsDialog()
    data class EditProfile(val profile: UserProfile) : SettingsDialog()
}

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val window = activity?.window
    val coroutineScope = rememberCoroutineScope()
    val systemController = remember { SystemSettingsController(context) }
    val profileManager = TaloApplication.instance.profileManager
    val soundManager = TaloApplication.instance.soundEffectManager
    val userProfileManager = remember { TaloApplication.instance.userProfileManager }

    // Navigation Level:
    // 0 = Root (Logo at left, 9 Categories at right)
    // 1 = Sub-menu (Categories slide left, Sub-menu at right, Logo slides offscreen left)
    // 2 = Sous-sous menu (Sub-menu slides left, Sous-sous menu at right, Categories slide offscreen left)
    // 3 = Level 3 Option Picker (Sous-sous menu slides left, Options at right, Sub-menu slides offscreen left)
    var navLevel by remember { mutableIntStateOf(0) }

    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    var activeCategory by remember { mutableStateOf(SettingsCategory.GENERAL) }
    var activeSubMenu by remember { mutableStateOf<SubMenuId?>(null) }
    var activeOptionPicker by remember { mutableStateOf<OptionPickerId?>(null) }

    var activeDialog by remember { mutableStateOf<SettingsDialog?>(null) }
    var showCalibrationOverlay by remember { mutableStateOf(false) }
    var showMirroringOverlay by remember { mutableStateOf(false) }

    // --- Category 1: General State ---
    var deviceName by remember { mutableStateOf(systemController.getDeviceName()) }
    var tempDeviceName by remember { mutableStateOf(deviceName) }
    var selectedLanguage by remember { mutableStateOf("Fran\u00E7ais (France)") }
    var timeFormat by remember { mutableStateOf(systemController.getTimeFormat()) } // "12h" or "24h"
    var selectedTimezone by remember { mutableStateOf("Europe/Paris (UTC+1)") }

    // --- Category 2: Utilisateur et comptes State ---
    var isAccountRegistered by remember { mutableStateOf(true) }
    val userProfiles by userProfileManager.profiles.collectAsState()

    // --- Category 3: Reseau State ---
    var wifiEnabled by remember { mutableStateOf(systemController.isWifiEnabled()) }
    var connectedSsid by remember { mutableStateOf<String?>(systemController.getConnectedSsid()) }

    // Diagnostics réseau réels
    var networkPing by remember { mutableStateOf("4 ms") }
    var networkSpeed by remember { mutableStateOf("Calcul...") }
    var networkIp by remember { mutableStateOf("127.0.0.1") }
    var isTestingNetwork by remember { mutableStateOf(false) }

    fun launchNetworkDiagnostic() {
        isTestingNetwork = true
        networkPing = "Mesure..."
        networkSpeed = "Mesure..."
        networkIp = "D\u00E9tection..."
        coroutineScope.launch {
            val result = systemController.runNetworkTest()
            networkPing = "${result.latencyMs} ms"
            networkSpeed = "${result.speedMbps} Mb/s"
            networkIp = result.ipAddress
            isTestingNetwork = false
        }
    }

    LaunchedEffect(activeSubMenu) {
        if (activeSubMenu == SubMenuId.NETWORK_TEST) {
            launchNetworkDiagnostic()
        }
    }

    // Modes d'affichage réels
    val supportedDisplayModes = remember(window) {
        systemController.getSupportedDisplayModes(window)
    }
    val resolutionOptions = remember(supportedDisplayModes) {
        val list = mutableListOf("Auto (Recommand\u00E9)")
        list.addAll(supportedDisplayModes.map { it.label })
        if (list.size <= 1) {
            listOf("Auto (Recommand\u00E9)", "3840x2160 @ 60Hz", "1920x1080 @ 60Hz", "1280x720 @ 60Hz")
        } else {
            list.distinct()
        }
    }

    // --- Category 4: Video et Audio State & Custom Profile Tracking ---
    var currentHwProfile by remember { mutableStateOf(profileManager.getCurrentProfileType()) }
    var lastBaseProfile by remember { mutableStateOf(ProfileType.STANDARD) }

    // Affichage Settings
    var displayResolution by remember { mutableStateOf("Auto (Recommand\u00E9)") }
    var matchFrameRate by remember { mutableStateOf(false) }
    var colorDepth by remember { mutableStateOf("Auto (N\u00E9goci\u00E9 HDMI)") }
    var colorFormat by remember { mutableStateOf("Auto (Recommand\u00E9)") }
    var dynamicRange by remember { mutableStateOf("Toujours HDR") }
    var allmMode by remember { mutableStateOf("Auto") }
    var standbyBehavior by remember { mutableStateOf("Aucun signal") }

    // Son Settings
    var navigationSounds by remember { mutableStateOf(soundManager.isSoundEnabled()) }
    var surroundSound by remember { mutableStateOf("S\u00E9lection automatique") }
    var volumeLeveler by remember { mutableStateOf(false) }
    var dialogueEnhancer by remember { mutableStateOf(true) }

    // Video/Audio override logic: preserves last base and sets profile to CUSTOM
    fun onVideoAudioSettingChanged() {
        if (currentHwProfile != ProfileType.CUSTOM) {
            lastBaseProfile = currentHwProfile
            currentHwProfile = ProfileType.CUSTOM
            profileManager.setProfileType(ProfileType.CUSTOM)
        }
    }

    // --- Category 5: Applications State ---
    val installedAppsManager = remember { TaloApplication.instance.installedAppsManager }
    val installedApps by installedAppsManager.installedApps.collectAsState()
    var appStoreAutoUpdate by remember { mutableStateOf(true) }
    var appStoreNotifications by remember { mutableStateOf(true) }
    var autoOffloadApps by remember { mutableStateOf(false) }

    // --- Category 6: Notification State ---
    val notifications = remember {
        mutableStateListOf(
            "Talo OS 0.0.1 \u2022 Syst\u00E8me op\u00E9rationnel et \u00E0 jour.",
            "R\u00E9seau Wi-Fi \u2022 Connect\u00E9 \u00E0 Bbox-Fib-5G (5 GHz - 850 Mb/s).",
            "T\u00E9l\u00E9commande Talo Remote \u2022 Niveau de batterie : 92%.",
            "AppStore \u2022 3 applications mises \u00E0 jour r\u00E9cemment."
        )
    }

    val fingerprint: String = remember { DeviceFingerprint.getFingerprint(context) }

    // Focus requesters and index persistence for each level
    val categoryFocusRequesters = remember {
        List(SettingsCategory.values().size) { FocusRequester() }
    }
    val level1FocusRequesters = remember { List(20) { FocusRequester() } }
    var level1SelectedIndex by remember { mutableIntStateOf(0) }

    val level2FocusRequesters = remember { List(30) { FocusRequester() } }
    var level2SelectedIndex by remember { mutableIntStateOf(0) }

    val level3FocusRequesters = remember { List(20) { FocusRequester() } }
    var level3SelectedIndex by remember { mutableIntStateOf(0) }

    val versionName = remember {
        try {
            val name = context.packageManager.getPackageInfo(context.packageName, 0).versionName
            if (name.isNullOrBlank() || name == "1.0.0") "0.0.1" else name
        } catch (e: Exception) {
            "0.0.1"
        }
    }

    // Navigation Unwinding Logic (DPAD_LEFT / Back)
    fun navigateBackOneLevel() {
        soundManager.playSelectSound()
        when {
            activeDialog != null -> activeDialog = null
            showCalibrationOverlay -> showCalibrationOverlay = false
            showMirroringOverlay -> showMirroringOverlay = false
            navLevel == 3 -> {
                navLevel = 2
                activeOptionPicker = null
            }
            navLevel == 2 -> {
                navLevel = 1
                activeSubMenu = null
            }
            navLevel == 1 -> {
                navLevel = 0
            }
            navLevel == 0 -> onNavigateBack()
        }
    }

    BackHandler(enabled = true) {
        navigateBackOneLevel()
    }

    // Dynamic Focus Dispatcher for Multi-Column Conveyor
    LaunchedEffect(navLevel, activeCategory, activeSubMenu, activeOptionPicker) {
        delay(60)
        when (navLevel) {
            0 -> {
                try {
                    categoryFocusRequesters[selectedCategoryIndex.coerceIn(0, categoryFocusRequesters.lastIndex)].requestFocus()
                } catch (e: Exception) {}
            }
            1 -> {
                try {
                    level1FocusRequesters[level1SelectedIndex.coerceIn(0, level1FocusRequesters.lastIndex)].requestFocus()
                } catch (e: Exception) {}
            }
            2 -> {
                try {
                    level2FocusRequesters[level2SelectedIndex.coerceIn(0, level2FocusRequesters.lastIndex)].requestFocus()
                } catch (e: Exception) {}
            }
            3 -> {
                try {
                    level3FocusRequesters[level3SelectedIndex.coerceIn(0, level3FocusRequesters.lastIndex)].requestFocus()
                } catch (e: Exception) {}
            }
        }
    }

    // =========================================================================
    // SLIDING COLUMN COORDINATES (Multi-column conveyor model)
    // Left Slot: x = 50.dp, width = 380.dp
    // Right Slot: x = 460.dp, width = 440.dp
    // Offscreen Left: x = -400.dp, alpha = 0f
    // Offscreen Right: x = 960.dp, alpha = 0f
    // =========================================================================

    // Slot: Brand Logo (Left at navLevel=0, offscreen left at navLevel>=1)
    val logoOffsetX by animateDpAsState(
        targetValue = if (navLevel == 0) 60.dp else (-380).dp,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "LogoOffset"
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (navLevel == 0) 1f else 0f,
        animationSpec = tween(durationMillis = 250),
        label = "LogoAlpha"
    )

    // Slot: Level 0 Categories
    // navLevel 0: Right slot (x = 460.dp, w = 440.dp)
    // navLevel 1: Left slot (x = 50.dp, w = 380.dp)
    // navLevel >= 2: Offscreen left (x = -400.dp, alpha = 0f) ("et le menu s'enleve")
    val level0OffsetX by animateDpAsState(
        targetValue = when (navLevel) {
            0 -> 460.dp
            1 -> 50.dp
            else -> (-400).dp
        },
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "Level0Offset"
    )
    val level0Width by animateDpAsState(
        targetValue = if (navLevel == 0) 440.dp else 380.dp,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "Level0Width"
    )
    val level0Alpha by animateFloatAsState(
        targetValue = if (navLevel <= 1) 1f else 0f,
        animationSpec = tween(durationMillis = 280),
        label = "Level0Alpha"
    )

    // Slot: Level 1 Sub-menu (sous menu)
    // navLevel 0: Offscreen right (x = 960.dp, alpha = 0f)
    // navLevel 1: Right slot (x = 460.dp, w = 440.dp)
    // navLevel 2: Left slot (x = 50.dp, w = 380.dp) ("le sous menu slide vers la gauche")
    // navLevel >= 3: Offscreen left (x = -400.dp, alpha = 0f)
    val level1OffsetX by animateDpAsState(
        targetValue = when (navLevel) {
            0 -> 960.dp
            1 -> 460.dp
            2 -> 50.dp
            else -> (-400).dp
        },
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "Level1Offset"
    )
    val level1Width by animateDpAsState(
        targetValue = if (navLevel == 1) 440.dp else 380.dp,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "Level1Width"
    )
    val level1Alpha by animateFloatAsState(
        targetValue = if (navLevel in 1..2) 1f else 0f,
        animationSpec = tween(durationMillis = 280),
        label = "Level1Alpha"
    )

    // Slot: Level 2 Sous-sous menu
    // navLevel <= 1: Offscreen right (x = 960.dp, alpha = 0f)
    // navLevel 2: Right slot (x = 460.dp, w = 440.dp) ("le sous sous menu se met a droite")
    // navLevel 3: Left slot (x = 50.dp, w = 380.dp)
    val level2OffsetX by animateDpAsState(
        targetValue = when (navLevel) {
            0, 1 -> 960.dp
            2 -> 460.dp
            else -> 50.dp
        },
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "Level2Offset"
    )
    val level2Width by animateDpAsState(
        targetValue = if (navLevel == 2) 440.dp else 380.dp,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "Level2Width"
    )
    val level2Alpha by animateFloatAsState(
        targetValue = if (navLevel >= 2) 1f else 0f,
        animationSpec = tween(durationMillis = 280),
        label = "Level2Alpha"
    )

    // Slot: Level 3 Option Picker
    // navLevel <= 2: Offscreen right (x = 960.dp, alpha = 0f)
    // navLevel 3: Right slot (x = 460.dp, w = 440.dp)
    val level3OffsetX by animateDpAsState(
        targetValue = if (navLevel == 3) 460.dp else 960.dp,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "Level3Offset"
    )
    val level3Alpha by animateFloatAsState(
        targetValue = if (navLevel == 3) 1f else 0f,
        animationSpec = tween(durationMillis = 280),
        label = "Level3Alpha"
    )

    // Header Title
    val currentHeaderTitle = when (navLevel) {
        0 -> "R\u00E9glages"
        1 -> activeCategory.title
        2 -> activeSubMenu?.title ?: activeCategory.title
        3 -> activeOptionPicker?.title ?: "Options"
        else -> "R\u00E9glages"
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0F1015),
                        Color(0xFF141722),
                        Color(0xFF0A0B0E)
                    )
                )
            )
    ) {
        // Centered Header
        Text(
            text = currentHeaderTitle,
            color = Color(0x99FFFFFF),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp)
                .align(Alignment.TopCenter)
        )

        // Main 3-Slot Sliding Canvas
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 50.dp, bottom = 18.dp)
        ) {
            // Slot: Brand Logo Lockup
            if (logoAlpha > 0.01f) {
                Box(
                    modifier = Modifier
                        .offset(x = logoOffsetX)
                        .width(340.dp)
                        .fillMaxHeight()
                        .alpha(logoAlpha),
                    contentAlignment = Alignment.Center
                ) {
                    TaloOsBrandLockup(versionName = versionName)
                }
            }

            // =================================================================
            // COLUMN 0: Level 0 Categories Menu
            // =================================================================
            if (level0Alpha > 0.01f) {
                Box(
                    modifier = Modifier
                        .offset(x = level0OffsetX)
                        .width(level0Width)
                        .fillMaxHeight()
                        .alpha(level0Alpha),
                    contentAlignment = Alignment.CenterStart
                ) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(SettingsCategory.values()) { index, cat ->
                            val isCurrentCat = activeCategory == cat
                            SettingsRowItem(
                                title = cat.title,
                                isParentActive = navLevel >= 1 && isCurrentCat,
                                enabled = (navLevel == 0),
                                focusRequester = categoryFocusRequesters[index],
                                onFocused = {
                                    selectedCategoryIndex = index
                                    activeCategory = cat
                                    level1SelectedIndex = 0
                                },
                                onClick = {
                                    soundManager.playSelectSound()
                                    selectedCategoryIndex = index
                                    activeCategory = cat
                                    level1SelectedIndex = 0
                                    navLevel = 1
                                },
                                onKeyEvent = { keyEvent ->
                                    if (keyEvent.type == KeyEventType.KeyDown) {
                                        when (keyEvent.key) {
                                            Key.DirectionRight, Key.Enter, Key.DirectionCenter, Key.NumPadEnter -> {
                                                soundManager.playSelectSound()
                                                selectedCategoryIndex = index
                                                activeCategory = cat
                                                level1SelectedIndex = 0
                                                navLevel = 1
                                                true
                                            }
                                            Key.DirectionLeft -> {
                                                navigateBackOneLevel()
                                                true
                                            }
                                            else -> false
                                        }
                                    } else false
                                }
                            )
                        }
                    }
                }
            }

            // =================================================================
            // COLUMN 1: Level 1 Sub-Menu (sous menu)
            // =================================================================
            if (level1Alpha > 0.01f) {
                Box(
                    modifier = Modifier
                        .offset(x = level1OffsetX)
                        .width(level1Width)
                        .fillMaxHeight()
                        .alpha(level1Alpha),
                    contentAlignment = Alignment.CenterStart
                ) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        when (activeCategory) {
                            SettingsCategory.GENERAL -> {
                                item {
                                    SettingsRowItem(
                                        title = "Nom de l'appareil",
                                        value = deviceName,
                                        enabled = (navLevel == 1),
                                        focusRequester = level1FocusRequesters.getOrNull(0),
                                        onFocused = { level1SelectedIndex = 0 },
                                        onClick = {
                                            tempDeviceName = deviceName
                                            activeDialog = SettingsDialog.EditDeviceName
                                        },
                                        onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                    )
                                }
                                item {
                                    SettingsRowItem(
                                        title = "Langue",
                                        value = selectedLanguage,
                                        enabled = (navLevel == 1),
                                        focusRequester = level1FocusRequesters.getOrNull(1),
                                        onFocused = { level1SelectedIndex = 1 },
                                        isParentActive = navLevel >= 2 && activeSubMenu == SubMenuId.LANGUAGES,
                                        onClick = {
                                            soundManager.playSelectSound()
                                            activeSubMenu = SubMenuId.LANGUAGES
                                            level2SelectedIndex = 0
                                            navLevel = 2
                                        },
                                        onKeyEvent = { handleRightOrLeft(it, onRight = {
                                            soundManager.playSelectSound()
                                            activeSubMenu = SubMenuId.LANGUAGES
                                            level2SelectedIndex = 0
                                            navLevel = 2
                                        }, onLeft = { navigateBackOneLevel() }) }
                                    )
                                }
                                item {
                                    SettingsRowItem(
                                        title = "Format horaire",
                                        value = timeFormat,
                                        hasChevron = false,
                                        enabled = (navLevel == 1),
                                        focusRequester = level1FocusRequesters.getOrNull(2),
                                        onFocused = { level1SelectedIndex = 2 },
                                        onClick = {
                                            soundManager.playSelectSound()
                                            val next = if (timeFormat == "24h") "12h" else "24h"
                                            timeFormat = next
                                            systemController.setTimeFormat(next)
                                        },
                                        onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                    )
                                }
                                item {
                                    SettingsRowItem(
                                        title = "Fuseau horaire",
                                        value = selectedTimezone,
                                        enabled = (navLevel == 1),
                                        focusRequester = level1FocusRequesters.getOrNull(3),
                                        onFocused = { level1SelectedIndex = 3 },
                                        isParentActive = navLevel >= 2 && activeSubMenu == SubMenuId.TIMEZONES,
                                        onClick = {
                                            soundManager.playSelectSound()
                                            activeSubMenu = SubMenuId.TIMEZONES
                                            level2SelectedIndex = 0
                                            navLevel = 2
                                        },
                                        onKeyEvent = { handleRightOrLeft(it, onRight = {
                                            soundManager.playSelectSound()
                                            activeSubMenu = SubMenuId.TIMEZONES
                                            level2SelectedIndex = 0
                                            navLevel = 2
                                        }, onLeft = { navigateBackOneLevel() }) }
                                    )
                                }
                            }

                            SettingsCategory.USERS -> {
                                item {
                                    SettingsRowItem(
                                        title = "Compte Talo",
                                        value = if (isAccountRegistered) "alexandre@talo.tv" else "Non associ\u00E9",
                                        enabled = (navLevel == 1),
                                        focusRequester = level1FocusRequesters.getOrNull(0),
                                        onFocused = { level1SelectedIndex = 0 },
                                        onClick = { activeDialog = SettingsDialog.UnregisterAccount },
                                        onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                    )
                                }
                                item {
                                    SettingsRowItem(
                                        title = "Profils",
                                        value = "${userProfiles.size} profils",
                                        enabled = (navLevel == 1),
                                        focusRequester = level1FocusRequesters.getOrNull(1),
                                        onFocused = { level1SelectedIndex = 1 },
                                        isParentActive = navLevel >= 2 && activeSubMenu == SubMenuId.PROFILES,
                                        onClick = {
                                            soundManager.playSelectSound()
                                            activeSubMenu = SubMenuId.PROFILES
                                            level2SelectedIndex = 0
                                            navLevel = 2
                                        },
                                        onKeyEvent = { handleRightOrLeft(it, onRight = {
                                            soundManager.playSelectSound()
                                            activeSubMenu = SubMenuId.PROFILES
                                            level2SelectedIndex = 0
                                            navLevel = 2
                                        }, onLeft = { navigateBackOneLevel() }) }
                                    )
                                }
                            }

                            SettingsCategory.NETWORK -> {
                                item {
                                    SettingsRowItem(
                                        title = "Wi-Fi",
                                        isChecked = wifiEnabled,
                                        hasChevron = false,
                                        enabled = (navLevel == 1),
                                        focusRequester = level1FocusRequesters.getOrNull(0),
                                        onFocused = { level1SelectedIndex = 0 },
                                        onToggle = {
                                            wifiEnabled = it
                                            systemController.setWifiEnabled(it)
                                        },
                                        onClick = {
                                            val target = !wifiEnabled
                                            wifiEnabled = target
                                            systemController.setWifiEnabled(target)
                                        },
                                        onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                    )
                                }
                                if (wifiEnabled) {
                                    item {
                                        SettingsRowItem(
                                            title = "Liste des r\u00E9seaux Wi-Fi",
                                            value = connectedSsid ?: "Recherche...",
                                            enabled = (navLevel == 1),
                                            focusRequester = level1FocusRequesters.getOrNull(1),
                                            onFocused = { level1SelectedIndex = 1 },
                                            isParentActive = navLevel >= 2 && activeSubMenu == SubMenuId.WIFI_LIST,
                                            onClick = {
                                                soundManager.playSelectSound()
                                                activeSubMenu = SubMenuId.WIFI_LIST
                                                level2SelectedIndex = 0
                                                navLevel = 2
                                            },
                                            onKeyEvent = { handleRightOrLeft(it, onRight = {
                                                soundManager.playSelectSound()
                                                activeSubMenu = SubMenuId.WIFI_LIST
                                                level2SelectedIndex = 0
                                                navLevel = 2
                                            }, onLeft = { navigateBackOneLevel() }) }
                                        )
                                    }
                                    if (connectedSsid != null) {
                                        item {
                                            SettingsRowItem(
                                                title = "Oublier ce r\u00E9seau",
                                                value = connectedSsid,
                                                enabled = (navLevel == 1),
                                                focusRequester = level1FocusRequesters.getOrNull(2),
                                                onFocused = { level1SelectedIndex = 2 },
                                                onClick = { activeDialog = SettingsDialog.ForgetNetwork },
                                                onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                            )
                                        }
                                    }
                                    item {
                                        val netIdx = if (connectedSsid != null) 3 else 2
                                        SettingsRowItem(
                                            title = "\u00C9tat du r\u00E9seau",
                                            value = if (connectedSsid != null && connectedSsid != "Wi-Fi D\u00E9connect\u00E9") "$networkSpeed \u2022 $networkPing" else "Non connect\u00E9",
                                            enabled = (navLevel == 1),
                                            focusRequester = level1FocusRequesters.getOrNull(netIdx),
                                            onFocused = { level1SelectedIndex = netIdx },
                                            isParentActive = navLevel >= 2 && activeSubMenu == SubMenuId.NETWORK_TEST,
                                            onClick = {
                                                soundManager.playSelectSound()
                                                activeSubMenu = SubMenuId.NETWORK_TEST
                                                level2SelectedIndex = 0
                                                navLevel = 2
                                            },
                                            onKeyEvent = { handleRightOrLeft(it, onRight = {
                                                soundManager.playSelectSound()
                                                activeSubMenu = SubMenuId.NETWORK_TEST
                                                level2SelectedIndex = 0
                                                navLevel = 2
                                            }, onLeft = { navigateBackOneLevel() }) }
                                        )
                                    }
                                }
                            }

                            SettingsCategory.VIDEO_AUDIO -> {
                                item {
                                    val profileLabel = when (currentHwProfile) {
                                        ProfileType.ECO -> "\u00C9co"
                                        ProfileType.STANDARD -> "Standard"
                                        ProfileType.ULTRA -> "Ultra"
                                        ProfileType.CUSTOM -> "Personnalis\u00E9"
                                    }
                                    SettingsRowItem(
                                        title = "Profil",
                                        value = profileLabel,
                                        enabled = (navLevel == 1),
                                        focusRequester = level1FocusRequesters.getOrNull(0),
                                        onFocused = { level1SelectedIndex = 0 },
                                        isParentActive = navLevel >= 2 && activeSubMenu == SubMenuId.PROFILE_PICKER,
                                        onClick = {
                                            soundManager.playSelectSound()
                                            activeSubMenu = SubMenuId.PROFILE_PICKER
                                            level2SelectedIndex = 0
                                            navLevel = 2
                                        },
                                        onKeyEvent = { handleRightOrLeft(it, onRight = {
                                            soundManager.playSelectSound()
                                            activeSubMenu = SubMenuId.PROFILE_PICKER
                                            level2SelectedIndex = 0
                                            navLevel = 2
                                        }, onLeft = { navigateBackOneLevel() }) }
                                    )
                                }
                                item {
                                    SettingsRowItem(
                                        title = "Affichage",
                                        value = "$displayResolution \u2022 $dynamicRange",
                                        enabled = (navLevel == 1),
                                        focusRequester = level1FocusRequesters.getOrNull(1),
                                        onFocused = { level1SelectedIndex = 1 },
                                        isParentActive = navLevel >= 2 && activeSubMenu == SubMenuId.DISPLAY,
                                        onClick = {
                                            soundManager.playSelectSound()
                                            activeSubMenu = SubMenuId.DISPLAY
                                            level2SelectedIndex = 0
                                            navLevel = 2
                                        },
                                        onKeyEvent = { handleRightOrLeft(it, onRight = {
                                            soundManager.playSelectSound()
                                            activeSubMenu = SubMenuId.DISPLAY
                                            level2SelectedIndex = 0
                                            navLevel = 2
                                        }, onLeft = { navigateBackOneLevel() }) }
                                    )
                                }
                                item {
                                    SettingsRowItem(
                                        title = "Son",
                                        value = surroundSound,
                                        enabled = (navLevel == 1),
                                        focusRequester = level1FocusRequesters.getOrNull(2),
                                        onFocused = { level1SelectedIndex = 2 },
                                        isParentActive = navLevel >= 2 && activeSubMenu == SubMenuId.AUDIO,
                                        onClick = {
                                            soundManager.playSelectSound()
                                            activeSubMenu = SubMenuId.AUDIO
                                            level2SelectedIndex = 0
                                            navLevel = 2
                                        },
                                        onKeyEvent = { handleRightOrLeft(it, onRight = {
                                            soundManager.playSelectSound()
                                            activeSubMenu = SubMenuId.AUDIO
                                            level2SelectedIndex = 0
                                            navLevel = 2
                                        }, onLeft = { navigateBackOneLevel() }) }
                                    )
                                }
                                item {
                                    SettingsRowItem(
                                        title = "Activer le partage d'\u00E9cran",
                                        value = "AirPlay / Cast",
                                        enabled = (navLevel == 1),
                                        focusRequester = level1FocusRequesters.getOrNull(3),
                                        onFocused = { level1SelectedIndex = 3 },
                                        onClick = {
                                            soundManager.playSelectSound()
                                            showMirroringOverlay = true
                                        },
                                        onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                    )
                                }
                            }

                            SettingsCategory.APPS -> {
                                item {
                                    SettingsRowItem(
                                        title = "G\u00E9rer les applications install\u00E9es",
                                        value = "${installedApps.size} apps",
                                        enabled = (navLevel == 1),
                                        focusRequester = level1FocusRequesters.getOrNull(0),
                                        onFocused = { level1SelectedIndex = 0 },
                                        isParentActive = navLevel >= 2 && activeSubMenu == SubMenuId.MANAGE_APPS,
                                        onClick = {
                                            soundManager.playSelectSound()
                                            activeSubMenu = SubMenuId.MANAGE_APPS
                                            level2SelectedIndex = 0
                                            navLevel = 2
                                        },
                                        onKeyEvent = { handleRightOrLeft(it, onRight = {
                                            soundManager.playSelectSound()
                                            activeSubMenu = SubMenuId.MANAGE_APPS
                                            level2SelectedIndex = 0
                                            navLevel = 2
                                        }, onLeft = { navigateBackOneLevel() }) }
                                    )
                                }
                                item {
                                    SettingsRowItem(
                                        title = "Vider le cache de toutes les applications",
                                        value = "1.8 Go",
                                        enabled = (navLevel == 1),
                                        focusRequester = level1FocusRequesters.getOrNull(1),
                                        onFocused = { level1SelectedIndex = 1 },
                                        onClick = { activeDialog = SettingsDialog.ClearAllAppsCache },
                                        onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                    )
                                }
                                item {
                                    SettingsRowItem(
                                        title = "AppStore",
                                        value = "Mises \u00E0 jour",
                                        enabled = (navLevel == 1),
                                        focusRequester = level1FocusRequesters.getOrNull(2),
                                        onFocused = { level1SelectedIndex = 2 },
                                        isParentActive = navLevel >= 2 && activeSubMenu == SubMenuId.APPSTORE,
                                        onClick = {
                                            soundManager.playSelectSound()
                                            activeSubMenu = SubMenuId.APPSTORE
                                            level2SelectedIndex = 0
                                            navLevel = 2
                                        },
                                        onKeyEvent = { handleRightOrLeft(it, onRight = {
                                            soundManager.playSelectSound()
                                            activeSubMenu = SubMenuId.APPSTORE
                                            level2SelectedIndex = 0
                                            navLevel = 2
                                        }, onLeft = { navigateBackOneLevel() }) }
                                    )
                                }
                                item {
                                    SettingsRowItem(
                                        title = "D\u00E9chargement automatique",
                                        isChecked = autoOffloadApps,
                                        hasChevron = false,
                                        enabled = (navLevel == 1),
                                        focusRequester = level1FocusRequesters.getOrNull(3),
                                        onFocused = { level1SelectedIndex = 3 },
                                        onToggle = { autoOffloadApps = it },
                                        onClick = { autoOffloadApps = !autoOffloadApps },
                                        onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                    )
                                }
                            }

                            SettingsCategory.NOTIFICATIONS -> {
                                item {
                                    SettingsRowItem(
                                        title = "Tout effacer",
                                        value = "${notifications.size} alertes",
                                        enabled = (navLevel == 1),
                                        focusRequester = level1FocusRequesters.getOrNull(0),
                                        onFocused = { level1SelectedIndex = 0 },
                                        onClick = {
                                            notifications.clear()
                                            Toast.makeText(context, "Notifications effac\u00E9es", Toast.LENGTH_SHORT).show()
                                        },
                                        onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                    )
                                }
                                itemsIndexed(notifications) { idx, notif ->
                                    val rowIdx = idx + 1
                                    SettingsRowItem(
                                        title = notif,
                                        hasChevron = false,
                                        enabled = (navLevel == 1),
                                        focusRequester = level1FocusRequesters.getOrNull(rowIdx),
                                        onFocused = { level1SelectedIndex = rowIdx },
                                        onClick = {},
                                        onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                    )
                                }
                                if (notifications.isEmpty()) {
                                    item {
                                        SettingsRowItem(
                                            title = "Aucune nouvelle notification",
                                            hasChevron = false,
                                            enabled = (navLevel == 1),
                                            focusRequester = level1FocusRequesters.getOrNull(1),
                                            onFocused = { level1SelectedIndex = 1 },
                                            onClick = {},
                                            onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                        )
                                    }
                                }
                            }

                            SettingsCategory.REMOTES -> {
                                item {
                                    SettingsRowItem(
                                        title = "Jumeler une t\u00E9l\u00E9commande",
                                        value = "Rechercher",
                                        enabled = (navLevel == 1),
                                        focusRequester = level1FocusRequesters.getOrNull(0),
                                        onFocused = { level1SelectedIndex = 0 },
                                        isParentActive = navLevel >= 2 && activeSubMenu == SubMenuId.BLUETOOTH_REMOTES,
                                        onClick = {
                                            soundManager.playSelectSound()
                                            activeSubMenu = SubMenuId.BLUETOOTH_REMOTES
                                            level2SelectedIndex = 0
                                            navLevel = 2
                                        },
                                        onKeyEvent = { handleRightOrLeft(it, onRight = {
                                            soundManager.playSelectSound()
                                            activeSubMenu = SubMenuId.BLUETOOTH_REMOTES
                                            level2SelectedIndex = 0
                                            navLevel = 2
                                        }, onLeft = { navigateBackOneLevel() }) }
                                    )
                                }
                                item {
                                    SettingsRowItem(
                                        title = "Autres appareils Bluetooth",
                                        value = "Rechercher",
                                        enabled = (navLevel == 1),
                                        focusRequester = level1FocusRequesters.getOrNull(1),
                                        onFocused = { level1SelectedIndex = 1 },
                                        isParentActive = navLevel >= 2 && activeSubMenu == SubMenuId.BLUETOOTH_DEVICES,
                                        onClick = {
                                            soundManager.playSelectSound()
                                            activeSubMenu = SubMenuId.BLUETOOTH_DEVICES
                                            level2SelectedIndex = 0
                                            navLevel = 2
                                        },
                                        onKeyEvent = { handleRightOrLeft(it, onRight = {
                                            soundManager.playSelectSound()
                                            activeSubMenu = SubMenuId.BLUETOOTH_DEVICES
                                            level2SelectedIndex = 0
                                            navLevel = 2
                                        }, onLeft = { navigateBackOneLevel() }) }
                                    )
                                }
                                item {
                                    SettingsRowItem(
                                        title = "T\u00E9l\u00E9commande actuelle",
                                        value = "Talo Remote Pro (92%)",
                                        hasChevron = false,
                                        enabled = (navLevel == 1),
                                        focusRequester = level1FocusRequesters.getOrNull(2),
                                        onFocused = { level1SelectedIndex = 2 },
                                        onClick = {},
                                        onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                    )
                                }
                            }

                            SettingsCategory.SYSTEM -> {
                                item {
                                    SettingsRowItem(
                                        title = "\u00C0 propos",
                                        value = "Talo HUB Ultra",
                                        enabled = (navLevel == 1),
                                        focusRequester = level1FocusRequesters.getOrNull(0),
                                        onFocused = { level1SelectedIndex = 0 },
                                        isParentActive = navLevel >= 2 && activeSubMenu == SubMenuId.ABOUT_HUB,
                                        onClick = {
                                            soundManager.playSelectSound()
                                            activeSubMenu = SubMenuId.ABOUT_HUB
                                            level2SelectedIndex = 0
                                            navLevel = 2
                                        },
                                        onKeyEvent = { handleRightOrLeft(it, onRight = {
                                            soundManager.playSelectSound()
                                            activeSubMenu = SubMenuId.ABOUT_HUB
                                            level2SelectedIndex = 0
                                            navLevel = 2
                                        }, onLeft = { navigateBackOneLevel() }) }
                                    )
                                }
                                item {
                                    SettingsRowItem(
                                        title = "Mentions l\u00E9gales et conformit\u00E9",
                                        value = "Licences & CE",
                                        enabled = (navLevel == 1),
                                        focusRequester = level1FocusRequesters.getOrNull(1),
                                        onFocused = { level1SelectedIndex = 1 },
                                        isParentActive = navLevel >= 2 && activeSubMenu == SubMenuId.LEGAL,
                                        onClick = {
                                            soundManager.playSelectSound()
                                            activeSubMenu = SubMenuId.LEGAL
                                            level2SelectedIndex = 0
                                            navLevel = 2
                                        },
                                        onKeyEvent = { handleRightOrLeft(it, onRight = {
                                            soundManager.playSelectSound()
                                            activeSubMenu = SubMenuId.LEGAL
                                            level2SelectedIndex = 0
                                            navLevel = 2
                                        }, onLeft = { navigateBackOneLevel() }) }
                                    )
                                }
                                item {
                                    SettingsRowItem(
                                        title = "Veille",
                                        value = "Mettre en veille",
                                        enabled = (navLevel == 1),
                                        focusRequester = level1FocusRequesters.getOrNull(2),
                                        onFocused = { level1SelectedIndex = 2 },
                                        onClick = {
                                            soundManager.playSelectSound()
                                            systemController.triggerSleep()
                                        },
                                        onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                    )
                                }
                                item {
                                    SettingsRowItem(
                                        title = "Red\u00E9marrer",
                                        value = "Red\u00E9marrer Talo OS",
                                        enabled = (navLevel == 1),
                                        focusRequester = level1FocusRequesters.getOrNull(3),
                                        onFocused = { level1SelectedIndex = 3 },
                                        onClick = { activeDialog = SettingsDialog.Reboot },
                                        onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                    )
                                }
                                item {
                                    SettingsRowItem(
                                        title = "R\u00E9initialiser",
                                        value = "Param\u00E8tres d'usine",
                                        enabled = (navLevel == 1),
                                        focusRequester = level1FocusRequesters.getOrNull(4),
                                        onFocused = { level1SelectedIndex = 4 },
                                        onClick = { activeDialog = SettingsDialog.Reset },
                                        onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                    )
                                }
                            }

                            SettingsCategory.HELP -> {
                                item {
                                    SettingsRowItem(
                                        title = "Wiki Web du site web",
                                        value = "wiki.talo.tv",
                                        enabled = (navLevel == 1),
                                        focusRequester = level1FocusRequesters.getOrNull(0),
                                        onFocused = { level1SelectedIndex = 0 },
                                        isParentActive = navLevel >= 2 && activeSubMenu == SubMenuId.WIKI_HELP,
                                        onClick = {
                                            soundManager.playSelectSound()
                                            activeSubMenu = SubMenuId.WIKI_HELP
                                            level2SelectedIndex = 0
                                            navLevel = 2
                                        },
                                        onKeyEvent = { handleRightOrLeft(it, onRight = {
                                            soundManager.playSelectSound()
                                            activeSubMenu = SubMenuId.WIKI_HELP
                                            level2SelectedIndex = 0
                                            navLevel = 2
                                        }, onLeft = { navigateBackOneLevel() }) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // =================================================================
            // COLUMN 2: Level 2 Sous-Sous Menu (le sous sous menu a droite)
            // =================================================================
            if (level2Alpha > 0.01f) {
                Box(
                    modifier = Modifier
                        .offset(x = level2OffsetX)
                        .width(level2Width)
                        .fillMaxHeight()
                        .alpha(level2Alpha),
                    contentAlignment = Alignment.CenterStart
                ) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        when (activeSubMenu) {
                            SubMenuId.LANGUAGES -> {
                                val languages = listOf(
                                    "Fran\u00E7ais (France)",
                                    "English (US)",
                                    "English (UK)",
                                    "Espa\u00F1ol (Espa\u00F1a)",
                                    "Deutsch (Deutschland)",
                                    "Italiano",
                                    "Portugu\u00EAs (Brasil)",
                                    "Nederlands",
                                    "\u65E5\u672C\u8A9E (Japonais)"
                                )
                                itemsIndexed(languages) { idx, lang ->
                                    val isSel = selectedLanguage == lang
                                    SettingsRowItem(
                                        title = lang,
                                        isSelected = isSel,
                                        hasChevron = false,
                                        enabled = (navLevel == 2),
                                        focusRequester = level2FocusRequesters.getOrNull(idx),
                                        onFocused = { level2SelectedIndex = idx },
                                        onClick = {
                                            soundManager.playSelectSound()
                                            selectedLanguage = lang
                                            val tag = when {
                                                lang.startsWith("English (US)") -> "en-US"
                                                lang.startsWith("English (UK)") -> "en-GB"
                                                lang.startsWith("Espa") -> "es-ES"
                                                lang.startsWith("Deutsch") -> "de-DE"
                                                lang.startsWith("Italiano") -> "it-IT"
                                                lang.startsWith("Portugu") -> "pt-BR"
                                                lang.startsWith("Nederlands") -> "nl-NL"
                                                lang.startsWith("\u65E5\u672C\u8A9E") -> "ja-JP"
                                                else -> "fr-FR"
                                            }
                                            systemController.applyLanguage(tag)
                                            Toast.makeText(context, "Langue appliqu\u00E9e : $lang", Toast.LENGTH_SHORT).show()
                                        },
                                        onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                    )
                                }
                                item {
                                    SettingsRowItem(
                                        title = "Param\u00E8tres de langue syst\u00E8me...",
                                        value = "Android TV",
                                        enabled = (navLevel == 2),
                                        focusRequester = level2FocusRequesters.getOrNull(languages.size),
                                        onFocused = { level2SelectedIndex = languages.size },
                                        onClick = {
                                            soundManager.playSelectSound()
                                            systemController.openSystemLocaleSettings()
                                        },
                                        onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                    )
                                }
                            }

                            SubMenuId.TIMEZONES -> {
                                val timezones = listOf(
                                    "Europe/Paris (UTC+1)",
                                    "Europe/London (UTC+0)",
                                    "America/New_York (UTC-5)",
                                    "America/Los_Angeles (UTC-8)",
                                    "America/Chicago (UTC-6)",
                                    "Asia/Tokyo (UTC+9)",
                                    "Asia/Dubai (UTC+4)",
                                    "Australia/Sydney (UTC+11)"
                                )
                                itemsIndexed(timezones) { idx, tz ->
                                    val isSel = selectedTimezone == tz
                                    SettingsRowItem(
                                        title = tz,
                                        isSelected = isSel,
                                        hasChevron = false,
                                        enabled = (navLevel == 2),
                                        focusRequester = level2FocusRequesters.getOrNull(idx),
                                        onFocused = { level2SelectedIndex = idx },
                                        onClick = {
                                            soundManager.playSelectSound()
                                            selectedTimezone = tz
                                            val tzId = tz.substringBefore(" (")
                                            systemController.setTimezone(tzId)
                                            Toast.makeText(context, "Fuseau horaire : $tzId", Toast.LENGTH_SHORT).show()
                                        },
                                        onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                    )
                                }
                            }

                            SubMenuId.PROFILES -> {
                                item {
                                    SettingsRowItem(
                                        title = "+ Cr\u00E9er un nouveau profil",
                                        value = "Ajouter",
                                        hasChevron = false,
                                        enabled = (navLevel == 2),
                                        focusRequester = level2FocusRequesters.getOrNull(0),
                                        onFocused = { level2SelectedIndex = 0 },
                                        onClick = { activeDialog = SettingsDialog.CreateProfile },
                                        onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                    )
                                }
                                itemsIndexed(userProfiles) { idx, prof ->
                                    val rowIdx = idx + 1
                                    SettingsRowItem(
                                        title = prof.name,
                                        value = if (prof.isPrimary) "Principal" else "Profil",
                                        isSelected = prof.isPrimary,
                                        enabled = (navLevel == 2),
                                        focusRequester = level2FocusRequesters.getOrNull(rowIdx),
                                        onFocused = { level2SelectedIndex = rowIdx },
                                        onClick = {
                                            soundManager.playSelectSound()
                                            userProfileManager.switchProfile(prof)
                                        },
                                        onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                    )
                                }
                            }

                            SubMenuId.WIFI_LIST -> {
                                item {
                                    SettingsRowItem(
                                        title = "Param\u00E8tres Wi-Fi syst\u00E8me...",
                                        value = "Gestionnaire Android",
                                        enabled = (navLevel == 2),
                                        focusRequester = level2FocusRequesters.getOrNull(0),
                                        onFocused = { level2SelectedIndex = 0 },
                                        onClick = {
                                            soundManager.playSelectSound()
                                            systemController.openWifiSettings()
                                        },
                                        onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                    )
                                }
                                val networks = listOf(
                                    Pair("Bbox-Fib-5G", "Connect\u00E9"),
                                    Pair("Freebox_Ultra", "S\u00E9curis\u00E9"),
                                    Pair("Livebox-6", "S\u00E9curis\u00E9"),
                                    Pair("SFR_WiFi_Home", "S\u00E9curis\u00E9"),
                                    Pair("Talo_Guest", "Ouvert")
                                )
                                itemsIndexed(networks) { idx, net ->
                                    val rowIdx = idx + 1
                                    val isConnected = connectedSsid == net.first
                                    SettingsRowItem(
                                        title = net.first,
                                        value = if (isConnected) "Connect\u00E9" else net.second,
                                        isSelected = isConnected,
                                        hasChevron = false,
                                        enabled = (navLevel == 2),
                                        focusRequester = level2FocusRequesters.getOrNull(rowIdx),
                                        onFocused = { level2SelectedIndex = rowIdx },
                                        onClick = {
                                            soundManager.playSelectSound()
                                            connectedSsid = net.first
                                            Toast.makeText(context, "Connect\u00E9 \u00E0 ${net.first}", Toast.LENGTH_SHORT).show()
                                        },
                                        onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                    )
                                }
                            }

                            SubMenuId.NETWORK_TEST -> {
                                item {
                                    SettingsRowItem(
                                        title = "Relancer le diagnostic r\u00E9seau",
                                        value = if (isTestingNetwork) "Mesure en cours..." else "Tester",
                                        enabled = (navLevel == 2),
                                        focusRequester = level2FocusRequesters.getOrNull(0),
                                        onFocused = { level2SelectedIndex = 0 },
                                        onClick = {
                                            soundManager.playSelectSound()
                                            launchNetworkDiagnostic()
                                        },
                                        onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                    )
                                }
                                item {
                                    SettingsRowItem(
                                        title = "D\u00E9bit descendant r\u00E9el",
                                        value = networkSpeed,
                                        hasChevron = false,
                                        enabled = (navLevel == 2),
                                        focusRequester = level2FocusRequesters.getOrNull(1),
                                        onFocused = { level2SelectedIndex = 1 },
                                        onClick = {},
                                        onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                    )
                                }
                                item {
                                    SettingsRowItem(
                                        title = "Latence (Ping serveur Talo)",
                                        value = networkPing,
                                        hasChevron = false,
                                        enabled = (navLevel == 2),
                                        focusRequester = level2FocusRequesters.getOrNull(2),
                                        onFocused = { level2SelectedIndex = 2 },
                                        onClick = {},
                                        onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                    )
                                }
                                item {
                                    SettingsRowItem(
                                        title = "Adresse IP de l'appareil",
                                        value = networkIp,
                                        hasChevron = false,
                                        enabled = (navLevel == 2),
                                        focusRequester = level2FocusRequesters.getOrNull(3),
                                        onFocused = { level2SelectedIndex = 3 },
                                        onClick = {},
                                        onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                    )
                                }
                                item {
                                    SettingsRowItem(
                                        title = "Type d'interface r\u00E9seau",
                                        value = if (wifiEnabled) "Wi-Fi 5 GHz (WLAN)" else "Ethernet Filaire",
                                        hasChevron = false,
                                        enabled = (navLevel == 2),
                                        focusRequester = level2FocusRequesters.getOrNull(4),
                                        onFocused = { level2SelectedIndex = 4 },
                                        onClick = {},
                                        onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                    )
                                }
                            }

                            SubMenuId.PROFILE_PICKER -> {
                                val profiles = listOf(
                                    Pair(ProfileType.ECO, "\u00C9co"),
                                    Pair(ProfileType.STANDARD, "Standard"),
                                    Pair(ProfileType.ULTRA, "Ultra"),
                                    Pair(ProfileType.CUSTOM, "Personnalis\u00E9")
                                )
                                itemsIndexed(profiles) { idx, p ->
                                    val isSel = currentHwProfile == p.first
                                    SettingsRowItem(
                                        title = p.second,
                                        isSelected = isSel,
                                        hasChevron = false,
                                        enabled = (navLevel == 2),
                                        focusRequester = level2FocusRequesters.getOrNull(idx),
                                        onFocused = { level2SelectedIndex = idx },
                                        onClick = {
                                            soundManager.playSelectSound()
                                            currentHwProfile = p.first
                                            profileManager.setProfileType(p.first)
                                        },
                                        onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                    )
                                }
                            }

                            SubMenuId.DISPLAY -> {
                                item {
                                    SettingsRowItem(
                                        title = "R\u00E9solution",
                                        value = displayResolution,
                                        enabled = (navLevel == 2),
                                        focusRequester = level2FocusRequesters.getOrNull(0),
                                        onFocused = { level2SelectedIndex = 0 },
                                        isParentActive = navLevel >= 3 && activeOptionPicker == OptionPickerId.RESOLUTION,
                                        onClick = {
                                            soundManager.playSelectSound()
                                            activeOptionPicker = OptionPickerId.RESOLUTION
                                            level3SelectedIndex = 0
                                            navLevel = 3
                                        },
                                        onKeyEvent = { handleRightOrLeft(it, onRight = {
                                            soundManager.playSelectSound()
                                            activeOptionPicker = OptionPickerId.RESOLUTION
                                            level3SelectedIndex = 0
                                            navLevel = 3
                                        }, onLeft = { navigateBackOneLevel() }) }
                                    )
                                }
                                item {
                                    SettingsRowItem(
                                        title = "Adapter \u00E0 la fr\u00E9quence d'image",
                                        isChecked = matchFrameRate,
                                        hasChevron = false,
                                        enabled = (navLevel == 2),
                                        focusRequester = level2FocusRequesters.getOrNull(1),
                                        onFocused = { level2SelectedIndex = 1 },
                                        onToggle = {
                                            matchFrameRate = it
                                            onVideoAudioSettingChanged()
                                        },
                                        onClick = {
                                            matchFrameRate = !matchFrameRate
                                            onVideoAudioSettingChanged()
                                        },
                                        onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                    )
                                }
                                item {
                                    SettingsRowItem(
                                        title = "Profondeur de couleur",
                                        value = colorDepth,
                                        enabled = (navLevel == 2),
                                        focusRequester = level2FocusRequesters.getOrNull(2),
                                        onFocused = { level2SelectedIndex = 2 },
                                        isParentActive = navLevel >= 3 && activeOptionPicker == OptionPickerId.COLOR_DEPTH,
                                        onClick = {
                                            soundManager.playSelectSound()
                                            activeOptionPicker = OptionPickerId.COLOR_DEPTH
                                            level3SelectedIndex = 0
                                            navLevel = 3
                                        },
                                        onKeyEvent = { handleRightOrLeft(it, onRight = {
                                            soundManager.playSelectSound()
                                            activeOptionPicker = OptionPickerId.COLOR_DEPTH
                                            level3SelectedIndex = 0
                                            navLevel = 3
                                        }, onLeft = { navigateBackOneLevel() }) }
                                    )
                                }
                                item {
                                    SettingsRowItem(
                                        title = "Format de couleur",
                                        value = colorFormat,
                                        enabled = (navLevel == 2),
                                        focusRequester = level2FocusRequesters.getOrNull(3),
                                        onFocused = { level2SelectedIndex = 3 },
                                        isParentActive = navLevel >= 3 && activeOptionPicker == OptionPickerId.COLOR_FORMAT,
                                        onClick = {
                                            soundManager.playSelectSound()
                                            activeOptionPicker = OptionPickerId.COLOR_FORMAT
                                            level3SelectedIndex = 0
                                            navLevel = 3
                                        },
                                        onKeyEvent = { handleRightOrLeft(it, onRight = {
                                            soundManager.playSelectSound()
                                            activeOptionPicker = OptionPickerId.COLOR_FORMAT
                                            level3SelectedIndex = 0
                                            navLevel = 3
                                        }, onLeft = { navigateBackOneLevel() }) }
                                    )
                                }
                                item {
                                    SettingsRowItem(
                                        title = "Calibrage de l'affichage",
                                        value = "Ajuster l'\u00E9cran",
                                        enabled = (navLevel == 2),
                                        focusRequester = level2FocusRequesters.getOrNull(4),
                                        onFocused = { level2SelectedIndex = 4 },
                                        onClick = {
                                            soundManager.playSelectSound()
                                            showCalibrationOverlay = true
                                        },
                                        onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                    )
                                }
                                item {
                                    SettingsRowItem(
                                        title = "Param\u00E8tre de la plage dynamique",
                                        value = dynamicRange,
                                        enabled = (navLevel == 2),
                                        focusRequester = level2FocusRequesters.getOrNull(5),
                                        onFocused = { level2SelectedIndex = 5 },
                                        isParentActive = navLevel >= 3 && activeOptionPicker == OptionPickerId.DYNAMIC_RANGE,
                                        onClick = {
                                            soundManager.playSelectSound()
                                            activeOptionPicker = OptionPickerId.DYNAMIC_RANGE
                                            level3SelectedIndex = 0
                                            navLevel = 3
                                        },
                                        onKeyEvent = { handleRightOrLeft(it, onRight = {
                                            soundManager.playSelectSound()
                                            activeOptionPicker = OptionPickerId.DYNAMIC_RANGE
                                            level3SelectedIndex = 0
                                            navLevel = 3
                                        }, onLeft = { navigateBackOneLevel() }) }
                                    )
                                }
                                item {
                                    SettingsRowItem(
                                        title = "Mode Jeu / ALLM",
                                        value = allmMode,
                                        enabled = (navLevel == 2),
                                        focusRequester = level2FocusRequesters.getOrNull(6),
                                        onFocused = { level2SelectedIndex = 6 },
                                        isParentActive = navLevel >= 3 && activeOptionPicker == OptionPickerId.ALLM_MODE,
                                        onClick = {
                                            soundManager.playSelectSound()
                                            activeOptionPicker = OptionPickerId.ALLM_MODE
                                            level3SelectedIndex = 0
                                            navLevel = 3
                                        },
                                        onKeyEvent = { handleRightOrLeft(it, onRight = {
                                            soundManager.playSelectSound()
                                            activeOptionPicker = OptionPickerId.ALLM_MODE
                                            level3SelectedIndex = 0
                                            navLevel = 3
                                        }, onLeft = { navigateBackOneLevel() }) }
                                    )
                                }
                                item {
                                    SettingsRowItem(
                                        title = "Comportement du mode veille",
                                        value = standbyBehavior,
                                        enabled = (navLevel == 2),
                                        focusRequester = level2FocusRequesters.getOrNull(7),
                                        onFocused = { level2SelectedIndex = 7 },
                                        isParentActive = navLevel >= 3 && activeOptionPicker == OptionPickerId.STANDBY_BEHAVIOR,
                                        onClick = {
                                            soundManager.playSelectSound()
                                            activeOptionPicker = OptionPickerId.STANDBY_BEHAVIOR
                                            level3SelectedIndex = 0
                                            navLevel = 3
                                        },
                                        onKeyEvent = { handleRightOrLeft(it, onRight = {
                                            soundManager.playSelectSound()
                                            activeOptionPicker = OptionPickerId.STANDBY_BEHAVIOR
                                            level3SelectedIndex = 0
                                            navLevel = 3
                                        }, onLeft = { navigateBackOneLevel() }) }
                                    )
                                }
                                item {
                                    SettingsRowItem(
                                        title = "Param\u00E8tres d'affichage syst\u00E8me...",
                                        value = "TV / HDMI",
                                        enabled = (navLevel == 2),
                                        focusRequester = level2FocusRequesters.getOrNull(8),
                                        onFocused = { level2SelectedIndex = 8 },
                                        onClick = {
                                            soundManager.playSelectSound()
                                            systemController.openSystemDisplaySettings()
                                        },
                                        onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                    )
                                }
                            }

                            SubMenuId.AUDIO -> {
                                item {
                                    SettingsRowItem(
                                        title = "Sons de navigation",
                                        isChecked = navigationSounds,
                                        hasChevron = false,
                                        enabled = (navLevel == 2),
                                        focusRequester = level2FocusRequesters.getOrNull(0),
                                        onFocused = { level2SelectedIndex = 0 },
                                        onToggle = {
                                            navigationSounds = it
                                            systemController.setNavigationSounds(it)
                                            soundManager.setSoundEnabled(it)
                                        },
                                        onClick = {
                                            val target = !navigationSounds
                                            navigationSounds = target
                                            systemController.setNavigationSounds(target)
                                            soundManager.setSoundEnabled(target)
                                        },
                                        onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                    )
                                }
                                item {
                                    SettingsRowItem(
                                        title = "Son Surround",
                                        value = surroundSound,
                                        enabled = (navLevel == 2),
                                        focusRequester = level2FocusRequesters.getOrNull(1),
                                        onFocused = { level2SelectedIndex = 1 },
                                        isParentActive = navLevel >= 3 && activeOptionPicker == OptionPickerId.SURROUND_SOUND,
                                        onClick = {
                                            soundManager.playSelectSound()
                                            activeOptionPicker = OptionPickerId.SURROUND_SOUND
                                            level3SelectedIndex = 0
                                            navLevel = 3
                                        },
                                        onKeyEvent = { handleRightOrLeft(it, onRight = {
                                            soundManager.playSelectSound()
                                            activeOptionPicker = OptionPickerId.SURROUND_SOUND
                                            level3SelectedIndex = 0
                                            navLevel = 3
                                        }, onLeft = { navigateBackOneLevel() }) }
                                    )
                                }
                                item {
                                    SettingsRowItem(
                                        title = "Niveleur de volume (Audio avanc\u00E9)",
                                        isChecked = volumeLeveler,
                                        hasChevron = false,
                                        enabled = (navLevel == 2),
                                        focusRequester = level2FocusRequesters.getOrNull(2),
                                        onFocused = { level2SelectedIndex = 2 },
                                        onToggle = {
                                            volumeLeveler = it
                                            onVideoAudioSettingChanged()
                                        },
                                        onClick = {
                                            volumeLeveler = !volumeLeveler
                                            onVideoAudioSettingChanged()
                                        },
                                        onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                    )
                                }
                                item {
                                    SettingsRowItem(
                                        title = "Am\u00E9lioration des dialogues Dolby",
                                        isChecked = dialogueEnhancer,
                                        hasChevron = false,
                                        enabled = (navLevel == 2),
                                        focusRequester = level2FocusRequesters.getOrNull(3),
                                        onFocused = { level2SelectedIndex = 3 },
                                        onToggle = {
                                            dialogueEnhancer = it
                                            onVideoAudioSettingChanged()
                                        },
                                        onClick = {
                                            dialogueEnhancer = !dialogueEnhancer
                                            onVideoAudioSettingChanged()
                                        },
                                        onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                    )
                                }
                                item {
                                    SettingsRowItem(
                                        title = "Param\u00E8tres audio syst\u00E8me...",
                                        value = "Sorties TV",
                                        enabled = (navLevel == 2),
                                        focusRequester = level2FocusRequesters.getOrNull(4),
                                        onFocused = { level2SelectedIndex = 4 },
                                        onClick = {
                                            soundManager.playSelectSound()
                                            systemController.openSystemSoundSettings()
                                        },
                                        onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                    )
                                }
                            }

                            SubMenuId.MANAGE_APPS -> {
                                itemsIndexed(installedApps) { idx, app ->
                                    SettingsRowItem(
                                        title = app.name,
                                        value = app.packageName,
                                        enabled = (navLevel == 2),
                                        focusRequester = level2FocusRequesters.getOrNull(idx),
                                        onFocused = { level2SelectedIndex = idx },
                                        onClick = {
                                            activeDialog = SettingsDialog.AppActions(app.name, app.packageName)
                                        },
                                        onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                    )
                                }
                            }

                            SubMenuId.APPSTORE -> {
                                item {
                                    SettingsRowItem(
                                        title = "Mise \u00E0 jour automatique",
                                        isChecked = appStoreAutoUpdate,
                                        hasChevron = false,
                                        enabled = (navLevel == 2),
                                        focusRequester = level2FocusRequesters.getOrNull(0),
                                        onFocused = { level2SelectedIndex = 0 },
                                        onToggle = { appStoreAutoUpdate = it },
                                        onClick = { appStoreAutoUpdate = !appStoreAutoUpdate },
                                        onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                    )
                                }
                                item {
                                    SettingsRowItem(
                                        title = "Notifications AppStore",
                                        isChecked = appStoreNotifications,
                                        hasChevron = false,
                                        enabled = (navLevel == 2),
                                        focusRequester = level2FocusRequesters.getOrNull(1),
                                        onFocused = { level2SelectedIndex = 1 },
                                        onToggle = { appStoreNotifications = it },
                                        onClick = { appStoreNotifications = !appStoreNotifications },
                                        onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                    )
                                }
                                item {
                                    SettingsRowItem(
                                        title = "D\u00E9chargement automatique des apps",
                                        isChecked = autoOffloadApps,
                                        hasChevron = false,
                                        enabled = (navLevel == 2),
                                        focusRequester = level2FocusRequesters.getOrNull(2),
                                        onFocused = { level2SelectedIndex = 2 },
                                        onToggle = { autoOffloadApps = it },
                                        onClick = { autoOffloadApps = !autoOffloadApps },
                                        onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                    )
                                }
                            }

                            SubMenuId.BLUETOOTH_REMOTES -> {
                                item {
                                    SettingsRowItem(
                                        title = "Jumeler une t\u00E9l\u00E9commande",
                                        value = "Assistant d'appairage",
                                        enabled = (navLevel == 2),
                                        focusRequester = level2FocusRequesters.getOrNull(0),
                                        onFocused = { level2SelectedIndex = 0 },
                                        onClick = {
                                            soundManager.playSelectSound()
                                            systemController.openBluetoothRemotePairing()
                                        },
                                        onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                    )
                                }
                                item {
                                    SettingsRowItem(
                                        title = "Talo Remote Pro",
                                        value = "Connect\u00E9e (92%)",
                                        hasChevron = false,
                                        isSelected = true,
                                        enabled = (navLevel == 2),
                                        focusRequester = level2FocusRequesters.getOrNull(1),
                                        onFocused = { level2SelectedIndex = 1 },
                                        onClick = {},
                                        onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                    )
                                }
                            }

                            SubMenuId.BLUETOOTH_DEVICES -> {
                                item {
                                    SettingsRowItem(
                                        title = "Rechercher des appareils Bluetooth",
                                        value = "Gestionnaire syst\u00E8me",
                                        enabled = (navLevel == 2),
                                        focusRequester = level2FocusRequesters.getOrNull(0),
                                        onFocused = { level2SelectedIndex = 0 },
                                        onClick = {
                                            soundManager.playSelectSound()
                                            systemController.openBluetoothSettings()
                                        },
                                        onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                    )
                                }
                                item {
                                    SettingsRowItem(
                                        title = "AirPods Pro Alexandre",
                                        value = "Jumel\u00E9",
                                        hasChevron = false,
                                        enabled = (navLevel == 2),
                                        focusRequester = level2FocusRequesters.getOrNull(1),
                                        onFocused = { level2SelectedIndex = 1 },
                                        onClick = {
                                            soundManager.playSelectSound()
                                            systemController.openBluetoothSettings()
                                        },
                                        onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                    )
                                }
                            }

                            SubMenuId.ABOUT_HUB -> {
                                val info = listOf(
                                    Pair("Mod\u00E8le", systemController.getDeviceModel()),
                                    Pair("Fabricant", systemController.getManufacturer()),
                                    Pair("Version Talo OS", versionName),
                                    Pair("Syst\u00E8me d'exploitation", systemController.getAndroidVersion()),
                                    Pair("Num\u00E9ro de s\u00E9rie", systemController.getDeviceSerialNumber()),
                                    Pair("Empreinte mat\u00E9rielle", fingerprint),
                                    Pair("Processeur", systemController.getProcessor()),
                                    Pair("M\u00E9moire vive (RAM)", systemController.getRamInfo()),
                                    Pair("Stockage interne", systemController.getStorageInfo()),
                                    Pair("Temps d'activit\u00E9", systemController.getUptime())
                                )
                                itemsIndexed(info) { idx, item ->
                                    SettingsRowItem(
                                        title = item.first,
                                        value = item.second,
                                        hasChevron = false,
                                        enabled = (navLevel == 2),
                                        focusRequester = level2FocusRequesters.getOrNull(idx),
                                        onFocused = { level2SelectedIndex = idx },
                                        onClick = {},
                                        onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                    )
                                }
                            }

                            SubMenuId.LEGAL -> {
                                val legalItems = listOf(
                                    "Mentions l\u00E9gales et CGU",
                                    "Licences de logiciels tiers",
                                    "Conformit\u00E9 radio et CE"
                                )
                                itemsIndexed(legalItems) { idx, item ->
                                    SettingsRowItem(
                                        title = item,
                                        value = "Consulter",
                                        enabled = (navLevel == 2),
                                        focusRequester = level2FocusRequesters.getOrNull(idx),
                                        onFocused = { level2SelectedIndex = idx },
                                        onClick = { Toast.makeText(context, "Consultation : $item", Toast.LENGTH_SHORT).show() },
                                        onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                    )
                                }
                            }

                            SubMenuId.WIKI_HELP -> {
                                item {
                                    SettingsRowItem(
                                        title = "Centre d'aide en ligne",
                                        value = "https://wiki.talo.tv",
                                        enabled = (navLevel == 2),
                                        focusRequester = level2FocusRequesters.getOrNull(0),
                                        onFocused = { level2SelectedIndex = 0 },
                                        onClick = {
                                            soundManager.playSelectSound()
                                            systemController.openWikiWeb()
                                        },
                                        onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                    )
                                }
                                item {
                                    SettingsRowItem(
                                        title = "Contacter le support Talo",
                                        value = "support@talo.tv",
                                        hasChevron = false,
                                        enabled = (navLevel == 2),
                                        focusRequester = level2FocusRequesters.getOrNull(1),
                                        onFocused = { level2SelectedIndex = 1 },
                                        onClick = {},
                                        onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                    )
                                }
                            }

                            null -> {}
                        }
                    }
                }
            }

            // =================================================================
            // COLUMN 3: Level 3 Option Picker
            // =================================================================
            if (level3Alpha > 0.01f) {
                Box(
                    modifier = Modifier
                        .offset(x = level3OffsetX)
                        .width(440.dp)
                        .fillMaxHeight()
                        .alpha(level3Alpha),
                    contentAlignment = Alignment.CenterStart
                ) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        when (activeOptionPicker) {
                            OptionPickerId.RESOLUTION -> {
                                itemsIndexed(resolutionOptions) { idx, res ->
                                    val isSel = displayResolution == res
                                    SettingsRowItem(
                                        title = res,
                                        isSelected = isSel,
                                        hasChevron = false,
                                        enabled = (navLevel == 3),
                                        focusRequester = level3FocusRequesters.getOrNull(idx),
                                        onFocused = { level3SelectedIndex = idx },
                                        onClick = {
                                            soundManager.playSelectSound()
                                            displayResolution = res
                                            onVideoAudioSettingChanged()
                                            val match = supportedDisplayModes.find { it.label == res }
                                            if (match != null) {
                                                systemController.applyDisplayMode(match.id, window)
                                            }
                                        },
                                        onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                    )
                                }
                            }

                            OptionPickerId.COLOR_DEPTH -> {
                                val depths = listOf("Auto (N\u00E9goci\u00E9 HDMI)", "8 Bits (SDR)", "10 Bits (HDR10)", "12 Bits (Dolby Vision)")
                                itemsIndexed(depths) { idx, d ->
                                    val isSel = colorDepth == d
                                    SettingsRowItem(
                                        title = d,
                                        isSelected = isSel,
                                        hasChevron = false,
                                        enabled = (navLevel == 3),
                                        focusRequester = level3FocusRequesters.getOrNull(idx),
                                        onFocused = { level3SelectedIndex = idx },
                                        onClick = {
                                            soundManager.playSelectSound()
                                            colorDepth = d
                                            onVideoAudioSettingChanged()
                                        },
                                        onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                    )
                                }
                            }

                            OptionPickerId.COLOR_FORMAT -> {
                                val formats = listOf("Auto (Recommand\u00E9)", "RGB 4:4:4", "YCbCr 4:2:2", "YCbCr 4:2:0")
                                itemsIndexed(formats) { idx, fmt ->
                                    val isSel = colorFormat == fmt
                                    SettingsRowItem(
                                        title = fmt,
                                        isSelected = isSel,
                                        hasChevron = false,
                                        enabled = (navLevel == 3),
                                        focusRequester = level3FocusRequesters.getOrNull(idx),
                                        onFocused = { level3SelectedIndex = idx },
                                        onClick = {
                                            soundManager.playSelectSound()
                                            colorFormat = fmt
                                            onVideoAudioSettingChanged()
                                        },
                                        onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                    )
                                }
                            }

                            OptionPickerId.DYNAMIC_RANGE -> {
                                val ranges = listOf("Toujours HDR", "Adaptative", "D\u00E9sactiver HDR")
                                itemsIndexed(ranges) { idx, r ->
                                    val isSel = dynamicRange == r
                                    SettingsRowItem(
                                        title = r,
                                        isSelected = isSel,
                                        hasChevron = false,
                                        enabled = (navLevel == 3),
                                        focusRequester = level3FocusRequesters.getOrNull(idx),
                                        onFocused = { level3SelectedIndex = idx },
                                        onClick = {
                                            soundManager.playSelectSound()
                                            dynamicRange = r
                                            onVideoAudioSettingChanged()
                                        },
                                        onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                    )
                                }
                            }

                            OptionPickerId.ALLM_MODE -> {
                                val modes = listOf("Auto", "Activ\u00E9", "D\u00E9sactiv\u00E9")
                                itemsIndexed(modes) { idx, m ->
                                    val isSel = allmMode == m
                                    SettingsRowItem(
                                        title = m,
                                        isSelected = isSel,
                                        hasChevron = false,
                                        enabled = (navLevel == 3),
                                        focusRequester = level3FocusRequesters.getOrNull(idx),
                                        onFocused = { level3SelectedIndex = idx },
                                        onClick = {
                                            soundManager.playSelectSound()
                                            allmMode = m
                                            val isEnabled = m != "D\u00E9sactiv\u00E9"
                                            systemController.setAllmEnabled(isEnabled, window)
                                            onVideoAudioSettingChanged()
                                        },
                                        onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                    )
                                }
                            }

                            OptionPickerId.STANDBY_BEHAVIOR -> {
                                val behaviors = listOf("Aucun signal", "\u00C9cran noir")
                                itemsIndexed(behaviors) { idx, b ->
                                    val isSel = standbyBehavior == b
                                    SettingsRowItem(
                                        title = b,
                                        isSelected = isSel,
                                        hasChevron = false,
                                        enabled = (navLevel == 3),
                                        focusRequester = level3FocusRequesters.getOrNull(idx),
                                        onFocused = { level3SelectedIndex = idx },
                                        onClick = {
                                            soundManager.playSelectSound()
                                            standbyBehavior = b
                                        },
                                        onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                    )
                                }
                            }

                            OptionPickerId.SURROUND_SOUND -> {
                                val surroundOptions = listOf(
                                    "S\u00E9lection automatique",
                                    "PCM",
                                    "Dolby Digital Plus",
                                    "Dolby Digital"
                                )
                                itemsIndexed(surroundOptions) { idx, s ->
                                    val isSel = surroundSound == s
                                    SettingsRowItem(
                                        title = s,
                                        isSelected = isSel,
                                        hasChevron = false,
                                        enabled = (navLevel == 3),
                                        focusRequester = level3FocusRequesters.getOrNull(idx),
                                        onFocused = { level3SelectedIndex = idx },
                                        onClick = {
                                            soundManager.playSelectSound()
                                            surroundSound = s
                                            systemController.setSurroundMode(s)
                                            onVideoAudioSettingChanged()
                                        },
                                        onKeyEvent = { handleLeftKey(it) { navigateBackOneLevel() } }
                                    )
                                }
                            }

                            null -> {}
                        }
                    }
                }
            }
        }

        // Full-Screen Calibration Overlay
        if (showCalibrationOverlay) {
            ScreenCalibrationOverlay(
                onFinish = {
                    showCalibrationOverlay = false
                    Toast.makeText(context, "Bordures d'affichage calibr\u00E9es", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Full-Screen Mirroring Overlay
        if (showMirroringOverlay) {
            ScreenMirroringWaitingOverlay(
                deviceName = deviceName,
                onCancel = { showMirroringOverlay = false }
            )
        }

        // Dialog Modals
        activeDialog?.let { dialog ->
            AppleTvModalDialog(
                onDismiss = { activeDialog = null }
            ) {
                when (dialog) {
                    is SettingsDialog.EditDeviceName -> {
                        DialogCard(
                            title = "Nom de l'appareil",
                            subtitle = "Saisissez le nouveau nom visible sur le r\u00E9seau pour Talo TV."
                        ) {
                            BasicTextField(
                                value = tempDeviceName,
                                onValueChange = { tempDeviceName = it },
                                textStyle = TextStyle(color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                                cursorBrush = SolidColor(AppleAccent),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x33FFFFFF))
                                    .border(BorderStroke(1.2.dp, AppleAccent), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                TvDialogButton(text = "Annuler", modifier = Modifier.weight(1f), onClick = { activeDialog = null })
                                TvDialogButton(
                                    text = "Valider",
                                    isPrimary = true,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        val newName = tempDeviceName.trim().ifEmpty { "Talo TV Salon" }
                                        deviceName = newName
                                        systemController.setDeviceName(newName)
                                        activeDialog = null
                                        Toast.makeText(context, "Nom mis \u00E0 jour : $deviceName", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }

                    is SettingsDialog.UnregisterAccount -> {
                        DialogCard(
                            title = "D\u00E9senregistrer le compte",
                            subtitle = "\u00CAtes-vous s\u00FBr de vouloir dissocier le compte alexandre@talo.tv de ce HUB Talo ?"
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                TvDialogButton(text = "Annuler", modifier = Modifier.weight(1f), onClick = { activeDialog = null })
                                TvDialogButton(
                                    text = "D\u00E9senregistrer",
                                    isPrimary = true,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        isAccountRegistered = false
                                        activeDialog = null
                                        Toast.makeText(context, "Compte dissoci\u00E9 avec succ\u00E8s", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }

                    is SettingsDialog.ForgetNetwork -> {
                        DialogCard(
                            title = "Oublier ce r\u00E9seau",
                            subtitle = "Voulez-vous oublier le r\u00E9seau Wi-Fi \"$connectedSsid\" ?"
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                TvDialogButton(text = "Annuler", modifier = Modifier.weight(1f), onClick = { activeDialog = null })
                                TvDialogButton(
                                    text = "Oublier",
                                    isPrimary = true,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        connectedSsid = null
                                        activeDialog = null
                                        Toast.makeText(context, "R\u00E9seau oubli\u00E9", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }

                    is SettingsDialog.ClearAllAppsCache -> {
                        DialogCard(
                            title = "Vider tout le cache",
                            subtitle = "Voulez-vous lib\u00E9rer de l'espace de stockage en vidant le cache de toutes les applications ?"
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                TvDialogButton(text = "Non", modifier = Modifier.weight(1f), onClick = { activeDialog = null })
                                TvDialogButton(
                                    text = "Oui, vider",
                                    isPrimary = true,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        val freedBytes = systemController.clearAllAppCache()
                                        val freedMb = (freedBytes / (1024 * 1024)).coerceAtLeast(1)
                                        activeDialog = null
                                        Toast.makeText(context, "Cache vid\u00E9 ($freedMb Mo lib\u00E9r\u00E9s)", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }

                    is SettingsDialog.AppActions -> {
                        DialogCard(
                            title = dialog.appName,
                            subtitle = "Options de gestion pour l'application (${dialog.packageName})."
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                TvDialogButton(
                                    text = "Forcer l'arr\u00EAt",
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = {
                                        systemController.forceStopApp(dialog.packageName)
                                        activeDialog = null
                                        Toast.makeText(context, "${dialog.appName} : arr\u00EAt demand\u00E9", Toast.LENGTH_SHORT).show()
                                    }
                                )
                                TvDialogButton(
                                    text = "G\u00E9rer (D\u00E9tails & Permissions)",
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = {
                                        activeDialog = null
                                        systemController.openAppDetailsSettings(dialog.packageName)
                                    }
                                )
                                TvDialogButton(
                                    text = "Effacer les donn\u00E9es / Cache",
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = {
                                        activeDialog = null
                                        systemController.openAppDetailsSettings(dialog.packageName)
                                    }
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                TvDialogButton(
                                    text = "Fermer",
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = { activeDialog = null }
                                )
                            }
                        }
                    }

                    is SettingsDialog.Reboot -> {
                        DialogCard(
                            title = "Red\u00E9marrer Talo OS",
                            subtitle = "Votre appareil va red\u00E9marrer le syst\u00E8me ou relancer Talo OS imm\u00E9diatement."
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                TvDialogButton(text = "Annuler", modifier = Modifier.weight(1f), onClick = { activeDialog = null })
                                TvDialogButton(
                                    text = "Red\u00E9marrer",
                                    isPrimary = true,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        activeDialog = null
                                        systemController.triggerReboot(activity)
                                    }
                                )
                            }
                        }
                    }

                    is SettingsDialog.Reset -> {
                        DialogCard(
                            title = "R\u00E9initialiser les param\u00E8tres d'usine",
                            subtitle = "Attention : ouverture de l'assistant de r\u00E9initialisation d'usine syst\u00E8me."
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                TvDialogButton(text = "Annuler", modifier = Modifier.weight(1f), onClick = { activeDialog = null })
                                TvDialogButton(
                                    text = "R\u00E9initialiser",
                                    isPrimary = true,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        activeDialog = null
                                        systemController.triggerFactoryReset()
                                    }
                                )
                            }
                        }
                    }

                    is SettingsDialog.CreateProfile -> {
                        var newName by remember { mutableStateOf("") }
                        DialogCard(
                            title = "Nouveau profil",
                            subtitle = "Entrez le nom du nouveau membre du foyer :"
                        ) {
                            BasicTextField(
                                value = newName,
                                onValueChange = { newName = it },
                                textStyle = TextStyle(color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                                cursorBrush = SolidColor(AppleAccent),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x33FFFFFF))
                                    .border(BorderStroke(1.2.dp, AppleAccent), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                TvDialogButton(text = "Annuler", modifier = Modifier.weight(1f), onClick = { activeDialog = null })
                                TvDialogButton(
                                    text = "Cr\u00E9er",
                                    isPrimary = true,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        if (newName.isNotBlank()) {
                                            userProfileManager.addProfile(name = newName.trim(), ageTier = "25 - 34", colorHex = 0xFFFFFFFF)
                                        }
                                        activeDialog = null
                                    }
                                )
                            }
                        }
                    }

                    is SettingsDialog.EditProfile -> {}
                }
            }
        }
    }
}

// =========================================================================
// Apple TV Full Uniform Settings Capsule Row
// Used across Level 0, Level 1, Level 2, and Level 3
// =========================================================================
@Composable
fun SettingsRowItem(
    title: String,
    value: String? = null,
    hasChevron: Boolean = true,
    isSelected: Boolean = false,
    isChecked: Boolean? = null,
    isParentActive: Boolean = false,
    enabled: Boolean = true,
    focusRequester: FocusRequester? = null,
    onFocused: (() -> Unit)? = null,
    onToggle: ((Boolean) -> Unit)? = null,
    onClick: () -> Unit,
    onKeyEvent: (KeyEvent) -> Boolean = { false }
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    LaunchedEffect(isFocused) {
        if (isFocused && enabled) {
            onFocused?.invoke()
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (isFocused && enabled) 1.02f else 1.0f,
        label = "SettingsItemScale"
    )

    val bg = when {
        isFocused && enabled -> Color.White
        isParentActive -> Color(0x35FFFFFF)
        else -> Color(0x2830323B)
    }
    val textColor = when {
        isFocused && enabled -> Color.Black
        isParentActive -> Color.White
        else -> Color.White
    }
    val valueColor = when {
        isFocused && enabled -> Color(0xFF636366)
        isParentActive -> Color(0xBBFFFFFF)
        else -> AppleTextSecondary
    }
    val chevronColor = when {
        isFocused && enabled -> Color(0xFF636366)
        isParentActive -> Color.White
        else -> Color(0x55FFFFFF)
    }
    val border = when {
        isFocused && enabled -> BorderStroke(1.5.dp, Color.White)
        isParentActive -> BorderStroke(1.2.dp, Color(0x55FFFFFF))
        else -> BorderStroke(1.dp, Color(0x18FFFFFF))
    }

    var rowModifier = Modifier
        .fillMaxWidth(0.96f)
        .scale(scale)
        .clip(RoundedCornerShape(14.dp))
        .background(bg)
        .border(border, RoundedCornerShape(14.dp))

    if (focusRequester != null) {
        rowModifier = rowModifier.focusRequester(focusRequester)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = rowModifier
            .onKeyEvent { if (enabled) onKeyEvent(it) else false }
            .clickable(enabled = enabled, interactionSource = interactionSource, indication = null) { onClick() }
            .focusable(enabled = enabled, interactionSource = interactionSource)
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Text(
            text = title,
            color = textColor,
            fontSize = 14.5.sp,
            fontWeight = if ((isFocused && enabled) || isParentActive) FontWeight.SemiBold else FontWeight.Medium,
            modifier = Modifier.weight(1f, fill = false),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isChecked != null) {
                Switch(
                    checked = isChecked,
                    onCheckedChange = if (enabled) onToggle else null,
                    enabled = enabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF636366),
                        uncheckedThumbColor = Color(0xFFA1A1A6),
                        uncheckedTrackColor = Color(0x33FFFFFF)
                    ),
                    modifier = Modifier.scale(0.85f)
                )
            } else {
                if (value != null) {
                    Text(
                        text = value,
                        color = valueColor,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (isSelected) {
                    Text(
                        text = "\u2713",
                        color = if (isFocused && enabled) Color.Black else Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else if (hasChevron) {
                    Text(
                        text = "\u203A",
                        color = chevronColor,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Helpers for Remote Keys
fun handleLeftKey(event: KeyEvent, onLeft: () -> Unit): Boolean {
    return if (event.type == KeyEventType.KeyDown && event.key == Key.DirectionLeft) {
        onLeft()
        true
    } else false
}

fun handleRightOrLeft(event: KeyEvent, onRight: () -> Unit, onLeft: () -> Unit): Boolean {
    if (event.type == KeyEventType.KeyDown) {
        when (event.key) {
            Key.DirectionRight, Key.Enter, Key.DirectionCenter, Key.NumPadEnter -> {
                onRight()
                return true
            }
            Key.DirectionLeft -> {
                onLeft()
                return true
            }
        }
    }
    return false
}

@Composable
fun TaloOsBrandLockup(
    versionName: String,
    modifier: Modifier = Modifier
) {
    val sfProFont = remember {
        FontFamily(
            Font(R.font.sf_pro_bold, FontWeight.Medium)
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(horizontal = 24.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.talo_os_logo_white),
            contentDescription = "Talo OS",
            modifier = Modifier
                .width(200.dp)
                .height(84.dp)
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Version $versionName",
            color = AppleTextSecondary,
            fontSize = 14.sp,
            fontFamily = sfProFont,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun AppleTvModalDialog(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.72f)),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Composable
fun DialogCard(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .width(460.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xE61E212B))
            .border(BorderStroke(1.2.dp, Color(0x33FFFFFF)), RoundedCornerShape(22.dp))
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = subtitle, color = AppleTextSecondary, fontSize = 13.5.sp, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(22.dp))
        content()
    }
}

@Composable
fun TvDialogButton(
    text: String,
    isPrimary: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val bg = when {
        isFocused -> Color.White
        isPrimary -> Color.White
        else -> Color(0x28FFFFFF)
    }
    val textColor = when {
        isFocused -> Color.Black
        isPrimary -> Color.Black
        else -> Color.White
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .focusable(interactionSource = interactionSource)
            .padding(vertical = 12.dp, horizontal = 18.dp)
    ) {
        Text(text = text, color = textColor, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun ScreenCalibrationOverlay(
    onFinish: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .onKeyEvent {
                if (it.type == KeyEventType.KeyDown && (it.key == Key.Back || it.key == Key.Enter || it.key == Key.DirectionCenter)) {
                    onFinish()
                    true
                } else false
            }
            .focusable()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .border(BorderStroke(3.dp, Color.White), RoundedCornerShape(16.dp))
        )

        Box(
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.TopStart)
                .background(Color.White)
        )
        Box(
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.TopEnd)
                .background(Color.White)
        )
        Box(
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.BottomStart)
                .background(Color.White)
        )
        Box(
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.BottomEnd)
                .background(Color.White)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text(
                text = "Calibrage de l'affichage",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Ajustez le format d'image de votre t\u00E9l\u00E9viseur jusqu'\u00E0 ce que les 4 rep\u00E8res blancs soient visibles.",
                color = AppleTextSecondary,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(520.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            TvDialogButton(text = "Terminer le calibrage", isPrimary = true, onClick = onFinish)
        }
    }
}

@Composable
fun ScreenMirroringWaitingOverlay(
    deviceName: String,
    onCancel: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.88f))
            .onKeyEvent {
                if (it.type == KeyEventType.KeyDown && (it.key == Key.Back || it.key == Key.DirectionLeft)) {
                    onCancel()
                    true
                } else false
            }
            .focusable(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(480.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = pulseAlpha * 0.2f))
                    .border(BorderStroke(2.dp, Color.White.copy(alpha = pulseAlpha)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(50.dp),
                    color = Color.White,
                    strokeWidth = 3.dp
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            Text(
                text = "En attente du partage d'\u00E9cran...",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Sur votre iPhone, iPad, Mac ou appareil Android / PC, ouvrez le centre de contr\u00F4le et s\u00E9lectionnez \"$deviceName\".",
                color = AppleTextSecondary,
                fontSize = 14.5.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            TvDialogButton(text = "Annuler", onClick = onCancel)
        }
    }
}