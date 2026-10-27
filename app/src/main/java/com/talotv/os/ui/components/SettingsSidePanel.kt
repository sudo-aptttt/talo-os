package com.talotv.os.ui.components

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.talotv.os.TaloApplication
import com.talotv.os.core.hardware.ProfileType
import com.talotv.os.core.security.TaloPrivacyShield
import com.talotv.os.core.system.CloudSyncManager
import com.talotv.os.core.system.SettingsLaunchCoordinator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class SettingsPanelLevel {
    MAIN,
    TALO_SETTINGS,
    ABOUT_TALO
}

/**
 * Volet Latéral Droit des Paramètres (Google TV Leanback Visual Style)
 * 100% conforme à la Direction Artistique Android TV / Google TV Settings :
 * - Largeur : exactement 360dp docked sur le bord droit
 * - Surface sombre unie : #202124 (Google Dark Surface), bords droits flush sans coin arrondi externe
 * - En-tête : Titre en blanc (#FFFFFF, 24sp, SemiBold), marge haute 56dp, marge gauche 24dp
 * - Sous-en-têtes de catégorie : Bleu Google TV Leanback (#8AB4F8, 13.5sp, Medium) alignés sur 24dp
 * - Focus : Surbrillance pleine largeur (#3C4043) sans marge horizontale, hauteur min 64dp
 * - Contrôles ON/OFF : Vrais interrupteurs à bascule Android TV Leanback (rail 36x20dp, curseur 16dp)
 * - Navigation D-Pad :
 *     - Droite / OK : actionne le réglage ou entre dans le sous-menu
 *     - Gauche / Retour : revient au menu parent ou ferme le volet
 *     - Interception D-Pad via onPreviewKeyEvent pour contourner la consommation prématurée par Compose focus traversal
 */
@Composable
fun SettingsSidePanel(
    onClose: () -> Unit,
    onOpenProfileSwitcher: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val soundManager = remember { TaloApplication.instance.soundEffectManager }
    val profileManager = remember { TaloApplication.instance.profileManager }

    val isShieldActive by TaloPrivacyShield.isShieldActive.collectAsState()
    val blockedCount by TaloPrivacyShield.blockedTrackersCount.collectAsState()

    var currentProfileType by remember { mutableStateOf(profileManager.getCurrentProfileType()) }

    var currentLevel by remember { mutableStateOf(SettingsPanelLevel.MAIN) }
    var hasReturnedFromTaloSettings by remember { mutableStateOf(false) }
    var hasReturnedFromAbout by remember { mutableStateOf(false) }

    val (cloudName, cloudEmail) = remember { CloudSyncManager.getCloudAccountInfo(context) }

    val versionName = remember {
        try {
            val name = context.packageManager.getPackageInfo(context.packageName, 0).versionName
            if (name.isNullOrBlank()) "0.0.1" else name
        } catch (_: Exception) {
            "0.0.1"
        }
    }

    val deviceDisplayName = remember {
        val model = Build.MODEL
        if (model.isNotBlank()) model else "Google TV Streamer (4K)"
    }

    // Dynamic Live Uptime updated every second when inside ABOUT_TALO
    var liveUptimeMs by remember { mutableStateOf(SystemClock.elapsedRealtime()) }
    LaunchedEffect(currentLevel) {
        if (currentLevel == SettingsPanelLevel.ABOUT_TALO) {
            while (true) {
                liveUptimeMs = SystemClock.elapsedRealtime()
                delay(1000)
            }
        }
    }

    // Software Update Checking state
    var isCheckingUpdates by remember { mutableStateOf(false) }
    var updateStatusText by remember { mutableStateOf("Votre système Talo OS est à jour") }

    // Level 1 Focus Requesters
    val focusTvSettings = remember { FocusRequester() }
    val focusTaloSettings = remember { FocusRequester() }

    // Level 2 Focus Requesters
    val focusAccount = remember { FocusRequester() }
    val focusRender = remember { FocusRequester() }
    val focusShield = remember { FocusRequester() }
    val focusAbout = remember { FocusRequester() }

    // Level 3 Focus Requesters
    val focusCheckUpdate = remember { FocusRequester() }
    val focusTvUpdate = remember { FocusRequester() }
    val focusOsVersion = remember { FocusRequester() }
    val focusDevice = remember { FocusRequester() }
    val focusAndroidVer = remember { FocusRequester() }
    val focusUptime = remember { FocusRequester() }

    // Restore focus when returning from native Android TV activities
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                coroutineScope.launch {
                    delay(60)
                    try {
                        when (currentLevel) {
                            SettingsPanelLevel.MAIN -> focusTvSettings.requestFocus()
                            SettingsPanelLevel.TALO_SETTINGS -> focusAccount.requestFocus()
                            SettingsPanelLevel.ABOUT_TALO -> focusTvUpdate.requestFocus()
                        }
                    } catch (_: Exception) {}
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Dynamic focus orchestration when switching levels
    LaunchedEffect(currentLevel) {
        delay(60)
        try {
            when (currentLevel) {
                SettingsPanelLevel.MAIN -> {
                    if (hasReturnedFromTaloSettings) {
                        focusTaloSettings.requestFocus()
                        hasReturnedFromTaloSettings = false
                    } else {
                        focusTvSettings.requestFocus()
                    }
                }
                SettingsPanelLevel.TALO_SETTINGS -> {
                    if (hasReturnedFromAbout) {
                        focusAbout.requestFocus()
                        hasReturnedFromAbout = false
                    } else {
                        focusAccount.requestFocus()
                    }
                }
                SettingsPanelLevel.ABOUT_TALO -> {
                    focusCheckUpdate.requestFocus()
                }
            }
        } catch (_: Exception) {}
    }

    // Back key handling (Strict Leanback: Level 3 -> Level 2 -> Level 1 -> Close)
    BackHandler(enabled = true) {
        soundManager.playSelectSound()
        when (currentLevel) {
            SettingsPanelLevel.ABOUT_TALO -> {
                hasReturnedFromAbout = true
                currentLevel = SettingsPanelLevel.TALO_SETTINGS
            }
            SettingsPanelLevel.TALO_SETTINGS -> {
                hasReturnedFromTaloSettings = true
                currentLevel = SettingsPanelLevel.MAIN
            }
            SettingsPanelLevel.MAIN -> {
                onClose()
            }
        }
    }

    // Panel docked to right edge, 360dp width, solid dark background #202124 (Android TV Leanback standard)
    Box(
        modifier = modifier
            .width(360.dp)
            .fillMaxHeight()
            .background(Color(0xFF202124))
    ) {
        AnimatedContent(
            targetState = currentLevel,
            transitionSpec = {
                if (initialState.ordinal < targetState.ordinal) {
                    (slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(240)
                    ) + fadeIn(animationSpec = tween(200))).togetherWith(
                        slideOutHorizontally(
                            targetOffsetX = { -it / 3 },
                            animationSpec = tween(220)
                        ) + fadeOut(animationSpec = tween(180))
                    )
                } else {
                    (slideInHorizontally(
                        initialOffsetX = { -it / 3 },
                        animationSpec = tween(240)
                    ) + fadeIn(animationSpec = tween(200))).togetherWith(
                        slideOutHorizontally(
                            targetOffsetX = { it },
                            animationSpec = tween(220)
                        ) + fadeOut(animationSpec = tween(180))
                    )
                }
            },
            label = "SettingsSidePanelTransition"
        ) { level ->
            when (level) {
                // ============================================================
                // LEVEL 1: PARAMÈTRES GÉNÉRAUX
                // ============================================================
                SettingsPanelLevel.MAIN -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "Paramètres",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.15.sp,
                            modifier = Modifier.padding(top = 56.dp, start = 24.dp, end = 24.dp, bottom = 18.dp)
                        )

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(0.dp),
                            contentPadding = PaddingValues(bottom = 36.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                AndroidTvSectionHeader(title = "Paramètres généraux")
                            }

                            // 1. Paramètres TV (Native Android TV settings)
                            item {
                                AndroidTvSettingsRow(
                                    title = "Paramètres TV",
                                    subtitle = "Réseau, Affichage et son, Comptes, Système...",
                                    focusRequester = focusTvSettings,
                                    isFirstItem = true,
                                    icon = { tint -> TvMonitorIcon(size = 24.dp, tint = tint) },
                                    trailingContent = { isFocused ->
                                        AndroidTvChevron(isFocused = isFocused)
                                    },
                                    onClick = {
                                        SettingsLaunchCoordinator.authorizeNativeLaunch()
                                        launchAndroidTvSettings(context)
                                    },
                                    onNavigateLeft = {
                                        onClose()
                                    },
                                    onNavigateRight = {
                                        SettingsLaunchCoordinator.authorizeNativeLaunch()
                                        launchAndroidTvSettings(context)
                                    }
                                )
                            }

                            // 2. Paramètres Talo TV (Sub-menu inside side panel)
                            item {
                                AndroidTvSettingsRow(
                                    title = "Paramètres Talo TV",
                                    subtitle = "Compte Cloud, Rendu graphique, Privacy Shield, À propos...",
                                    focusRequester = focusTaloSettings,
                                    isLastItem = true,
                                    icon = { tint -> SparkleTaloIcon(size = 24.dp, tint = tint) },
                                    trailingContent = { isFocused ->
                                        AndroidTvChevron(isFocused = isFocused)
                                    },
                                    onClick = {
                                        currentLevel = SettingsPanelLevel.TALO_SETTINGS
                                    },
                                    onNavigateLeft = {
                                        onClose()
                                    },
                                    onNavigateRight = {
                                        currentLevel = SettingsPanelLevel.TALO_SETTINGS
                                    }
                                )
                            }
                        }
                    }
                }

                // ============================================================
                // LEVEL 2: PARAMÈTRES TALO TV
                // ============================================================
                SettingsPanelLevel.TALO_SETTINGS -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "Paramètres Talo TV",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.15.sp,
                            modifier = Modifier.padding(top = 56.dp, start = 24.dp, end = 24.dp, bottom = 18.dp)
                        )

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(0.dp),
                            contentPadding = PaddingValues(bottom = 36.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Section 1: Compte & Rendu
                            item {
                                AndroidTvSectionHeader(title = "Compte & Rendu")
                            }

                            // 1. Compte Cloud Talo
                            item {
                                AndroidTvSettingsRow(
                                    title = "Compte Cloud Talo",
                                    subtitle = "$cloudEmail • Synchronisé",
                                    focusRequester = focusAccount,
                                    isFirstItem = true,
                                    icon = { tint -> CloudCheckIcon(size = 24.dp, tint = tint) },
                                    trailingContent = { isFocused ->
                                        AndroidTvValueText(
                                            value = "Connecté",
                                            isFocused = isFocused,
                                            showChevron = false
                                        )
                                    },
                                    onClick = {
                                        CloudSyncManager.startBackgroundSync(context)
                                        Toast.makeText(context, "Compte Cloud Talo ($cloudEmail) synchronisé", Toast.LENGTH_SHORT).show()
                                    },
                                    onNavigateLeft = {
                                        hasReturnedFromTaloSettings = true
                                        currentLevel = SettingsPanelLevel.MAIN
                                    },
                                    onNavigateRight = {
                                        CloudSyncManager.startBackgroundSync(context)
                                        Toast.makeText(context, "Compte Cloud Talo ($cloudEmail) synchronisé", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }

                            // 2. Rendu graphique
                            item {
                                val modeLabel = when (currentProfileType) {
                                    ProfileType.ECO -> "Mode Éco (Économie d'énergie)"
                                    ProfileType.STANDARD -> "Mode Standard (Équilibré)"
                                    ProfileType.ULTRA -> "Mode Ultra 60 FPS (Fluidité maximale)"
                                    else -> "Mode Standard (Équilibré)"
                                }
                                val badgeText = when (currentProfileType) {
                                    ProfileType.ECO -> "Éco"
                                    ProfileType.STANDARD -> "Standard"
                                    ProfileType.ULTRA -> "Ultra 60 FPS"
                                    else -> "Standard"
                                }

                                AndroidTvSettingsRow(
                                    title = "Rendu graphique",
                                    subtitle = modeLabel,
                                    focusRequester = focusRender,
                                    icon = { tint -> GraphicsMonitorIcon(size = 24.dp, tint = tint) },
                                    trailingContent = { isFocused ->
                                        AndroidTvValueText(
                                            value = badgeText,
                                            isFocused = isFocused,
                                            showChevron = false
                                        )
                                    },
                                    onClick = {
                                        val next = when (currentProfileType) {
                                            ProfileType.STANDARD -> ProfileType.ULTRA
                                            ProfileType.ULTRA -> ProfileType.ECO
                                            ProfileType.ECO -> ProfileType.STANDARD
                                            else -> ProfileType.STANDARD
                                        }
                                        profileManager.setProfileType(next)
                                        currentProfileType = next
                                        val newModeText = when (next) {
                                            ProfileType.ECO -> "Mode Éco"
                                            ProfileType.STANDARD -> "Mode Standard"
                                            ProfileType.ULTRA -> "Mode Ultra 60 FPS"
                                            else -> "Standard"
                                        }
                                        Toast.makeText(context, "Rendu : $newModeText", Toast.LENGTH_SHORT).show()
                                    },
                                    onNavigateLeft = {
                                        hasReturnedFromTaloSettings = true
                                        currentLevel = SettingsPanelLevel.MAIN
                                    },
                                    onNavigateRight = {
                                        val next = when (currentProfileType) {
                                            ProfileType.STANDARD -> ProfileType.ULTRA
                                            ProfileType.ULTRA -> ProfileType.ECO
                                            ProfileType.ECO -> ProfileType.STANDARD
                                            else -> ProfileType.STANDARD
                                        }
                                        profileManager.setProfileType(next)
                                        currentProfileType = next
                                        val newModeText = when (next) {
                                            ProfileType.ECO -> "Mode Éco"
                                            ProfileType.STANDARD -> "Mode Standard"
                                            ProfileType.ULTRA -> "Mode Ultra 60 FPS"
                                            else -> "Standard"
                                        }
                                        Toast.makeText(context, "Rendu : $newModeText", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }

                            // Section 2: Sécurité & Système
                            item {
                                AndroidTvSectionHeader(title = "Sécurité & Système")
                            }

                            // 3. Bouclier Privacy Shield
                            item {
                                val shieldSubtitle = if (isShieldActive) {
                                    "Actif • Désactiver si un replay vidéo avec pub bloque"
                                } else {
                                    "Désactivé • Recommandé pour débloquer les replays vidéos"
                                }

                                AndroidTvSettingsRow(
                                    title = "Bouclier Privacy Shield",
                                    subtitle = shieldSubtitle,
                                    focusRequester = focusShield,
                                    icon = { tint -> ShieldIcon(size = 24.dp, tint = tint) },
                                    trailingContent = { isFocused ->
                                        AndroidTvSwitch(
                                            checked = isShieldActive,
                                            isFocusedRow = isFocused
                                        )
                                    },
                                    onClick = {
                                        TaloPrivacyShield.toggleShield(context)
                                        val nowActive = !isShieldActive
                                        val msg = if (nowActive) "Bouclier Privacy Shield : Activé" else "Bouclier Privacy Shield : Désactivé"
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    },
                                    onNavigateLeft = {
                                        hasReturnedFromTaloSettings = true
                                        currentLevel = SettingsPanelLevel.MAIN
                                    },
                                    onNavigateRight = {
                                        if (!isShieldActive) {
                                            TaloPrivacyShield.toggleShield(context)
                                            Toast.makeText(context, "Bouclier Privacy Shield : Activé", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            }

                            // 4. À propos de Talo TV OS (Navigates to dedicated sub-page)
                            item {
                                AndroidTvSettingsRow(
                                    title = "À propos de Talo TV OS",
                                    subtitle = "Version $versionName • Système & Mises à jour",
                                    focusRequester = focusAbout,
                                    isLastItem = true,
                                    icon = { tint -> InfoCircleIcon(size = 24.dp, tint = tint) },
                                    trailingContent = { isFocused ->
                                        AndroidTvChevron(isFocused = isFocused)
                                    },
                                    onClick = {
                                        currentLevel = SettingsPanelLevel.ABOUT_TALO
                                    },
                                    onNavigateLeft = {
                                        hasReturnedFromTaloSettings = true
                                        currentLevel = SettingsPanelLevel.MAIN
                                    },
                                    onNavigateRight = {
                                        currentLevel = SettingsPanelLevel.ABOUT_TALO
                                    }
                                )
                            }
                        }
                    }
                }

                // ============================================================
                // LEVEL 3: À PROPOS DE TALO TV OS & MISES À JOUR
                // ============================================================
                SettingsPanelLevel.ABOUT_TALO -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "À propos de Talo TV OS",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.15.sp,
                            modifier = Modifier.padding(top = 56.dp, start = 24.dp, end = 24.dp, bottom = 18.dp)
                        )

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(0.dp),
                            contentPadding = PaddingValues(bottom = 36.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Section 1: Mises à jour logicielles
                            item {
                                AndroidTvSectionHeader(title = "Mises à jour logicielles")
                            }

                            // 1. Rechercher des mises à jour Talo OS
                            item {
                                AndroidTvSettingsRow(
                                    title = "Rechercher des mises à jour",
                                    subtitle = updateStatusText,
                                    focusRequester = focusCheckUpdate,
                                    isFirstItem = true,
                                    icon = { tint -> UpdateCheckIcon(size = 24.dp, tint = tint) },
                                    trailingContent = { isFocused ->
                                        AndroidTvValueText(
                                            value = if (isCheckingUpdates) "Vérification..." else "À jour",
                                            isFocused = isFocused,
                                            showChevron = false
                                        )
                                    },
                                    onClick = {
                                        if (!isCheckingUpdates) {
                                            coroutineScope.launch {
                                                isCheckingUpdates = true
                                                updateStatusText = "Recherche de mise à jour en cours..."
                                                delay(1500)
                                                CloudSyncManager.startBackgroundSync(context)
                                                val update = CloudSyncManager.availableUpdate.value
                                                isCheckingUpdates = false
                                                if (update != null && update.hasUpdate) {
                                                    updateStatusText = "Mise à jour v${update.latestVersion} disponible"
                                                    Toast.makeText(context, "Mise à jour Talo OS v${update.latestVersion} disponible", Toast.LENGTH_LONG).show()
                                                } else {
                                                    updateStatusText = "Votre système Talo OS est à jour (v$versionName)"
                                                    Toast.makeText(context, "Talo OS est à jour", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    },
                                    onNavigateLeft = {
                                        hasReturnedFromAbout = true
                                        currentLevel = SettingsPanelLevel.TALO_SETTINGS
                                    },
                                    onNavigateRight = {
                                        if (!isCheckingUpdates) {
                                            coroutineScope.launch {
                                                isCheckingUpdates = true
                                                updateStatusText = "Recherche de mise à jour en cours..."
                                                delay(1500)
                                                CloudSyncManager.startBackgroundSync(context)
                                                val update = CloudSyncManager.availableUpdate.value
                                                isCheckingUpdates = false
                                                if (update != null && update.hasUpdate) {
                                                    updateStatusText = "Mise à jour v${update.latestVersion} disponible"
                                                    Toast.makeText(context, "Mise à jour Talo OS v${update.latestVersion} disponible", Toast.LENGTH_LONG).show()
                                                } else {
                                                    updateStatusText = "Votre système Talo OS est à jour (v$versionName)"
                                                    Toast.makeText(context, "Talo OS est à jour", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    }
                                )
                            }

                            // 2. Mise à jour système Android TV
                            item {
                                AndroidTvSettingsRow(
                                    title = "Mise à jour système Android TV",
                                    subtitle = "Ouvrir la page de mise à jour du système Google TV",
                                    focusRequester = focusTvUpdate,
                                    icon = { tint -> TvMonitorIcon(size = 24.dp, tint = tint) },
                                    trailingContent = { isFocused ->
                                        AndroidTvChevron(isFocused = isFocused)
                                    },
                                    onClick = {
                                        SettingsLaunchCoordinator.authorizeNativeLaunch()
                                        launchSystemUpdate(context)
                                    },
                                    onNavigateLeft = {
                                        hasReturnedFromAbout = true
                                        currentLevel = SettingsPanelLevel.TALO_SETTINGS
                                    },
                                    onNavigateRight = {
                                        SettingsLaunchCoordinator.authorizeNativeLaunch()
                                        launchSystemUpdate(context)
                                    }
                                )
                            }

                            // Section 2: Informations système
                            item {
                                AndroidTvSectionHeader(title = "Informations système")
                            }

                            // 3. Version Talo OS
                            item {
                                AndroidTvSettingsRow(
                                    title = "Version Talo OS",
                                    subtitle = "v$versionName (Build 100 • Production)",
                                    focusRequester = focusOsVersion,
                                    icon = { tint -> SparkleTaloIcon(size = 24.dp, tint = tint) },
                                    trailingContent = null,
                                    onClick = {
                                        Toast.makeText(context, "Talo OS v$versionName (Production)", Toast.LENGTH_SHORT).show()
                                    },
                                    onNavigateLeft = {
                                        hasReturnedFromAbout = true
                                        currentLevel = SettingsPanelLevel.TALO_SETTINGS
                                    },
                                    onNavigateRight = null
                                )
                            }

                            // 4. Appareil
                            item {
                                AndroidTvSettingsRow(
                                    title = "Appareil cible",
                                    subtitle = "$deviceDisplayName (${Build.MANUFACTURER})",
                                    focusRequester = focusDevice,
                                    icon = { tint -> TvMonitorIcon(size = 24.dp, tint = tint) },
                                    trailingContent = null,
                                    onClick = {
                                        Toast.makeText(context, "$deviceDisplayName - ${Build.BRAND} ${Build.DEVICE}", Toast.LENGTH_SHORT).show()
                                    },
                                    onNavigateLeft = {
                                        hasReturnedFromAbout = true
                                        currentLevel = SettingsPanelLevel.TALO_SETTINGS
                                    },
                                    onNavigateRight = null
                                )
                            }

                            // 5. Version Android TV
                            item {
                                AndroidTvSettingsRow(
                                    title = "Version Android TV",
                                    subtitle = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
                                    focusRequester = focusAndroidVer,
                                    icon = { tint -> InfoCircleIcon(size = 24.dp, tint = tint) },
                                    trailingContent = null,
                                    onClick = {
                                        Toast.makeText(context, "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})", Toast.LENGTH_SHORT).show()
                                    },
                                    onNavigateLeft = {
                                        hasReturnedFromAbout = true
                                        currentLevel = SettingsPanelLevel.TALO_SETTINGS
                                    },
                                    onNavigateRight = null
                                )
                            }

                            // 6. Temps de fonctionnement (Uptime)
                            item {
                                val uptimeFormatted = formatUptime(liveUptimeMs)
                                AndroidTvSettingsRow(
                                    title = "Temps de fonctionnement",
                                    subtitle = "$uptimeFormatted • En direct",
                                    focusRequester = focusUptime,
                                    isLastItem = true,
                                    icon = { tint -> ClockTimerIcon(size = 24.dp, tint = tint) },
                                    trailingContent = null,
                                    onClick = {
                                        Toast.makeText(context, "Temps de fonctionnement : $uptimeFormatted", Toast.LENGTH_SHORT).show()
                                    },
                                    onNavigateLeft = {
                                        hasReturnedFromAbout = true
                                        currentLevel = SettingsPanelLevel.TALO_SETTINGS
                                    },
                                    onNavigateRight = null
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Formate le temps de fonctionnement en direct (jours, heures, minutes, secondes)
 */
private fun formatUptime(uptimeMs: Long): String {
    val totalSeconds = uptimeMs / 1000
    val days = totalSeconds / (24 * 3600)
    val hours = (totalSeconds % (24 * 3600)) / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return when {
        days > 0 -> "${days} j, ${hours} h, ${minutes} min"
        hours > 0 -> "${hours} h, ${minutes} min, ${seconds} s"
        else -> "${minutes} min, ${seconds} s"
    }
}

/**
 * Sous-titre de section Android TV / Google TV Settings
 * Typographie officielle Leanback : Bleu Google TV (#8AB4F8), 13.5sp, Medium
 * Aligné avec le même padding gauche de 24dp que le titre et les icônes
 */
@Composable
private fun AndroidTvSectionHeader(title: String) {
    Text(
        text = title,
        color = Color(0xFF8AB4F8),
        fontSize = 13.5.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.2.sp,
        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 8.dp)
    )
}

/**
 * Rangée de réglage Android TV Leanback :
 * - Pleine largeur (0dp marge externe), fond #3C4043 au focus, transparent hors focus
 * - Inactive : Fond transparent, icône #BDC1C6, titre #E8EAED, sous-titre #9AA0A6
 * - Focus : Surbrillance charbon/ardoise sombre pleine largeur (#3C4043),
 *           Icône #FFFFFF, titre blanc pur (#FFFFFF, 15sp, Medium), sous-titre #BDC1C6
 * - Aucun effet de zoom/scale déstabilisant, stabilité totale à la télécommande
 * - Navigation D-Pad :
 *     - onPreviewKeyEvent intercepte Key.DirectionRight et Key.DirectionLeft
 *       AVANT que Compose FocusManager n'évalue la navigation 2D et ne consomme les événements.
 */
@Composable
private fun AndroidTvSettingsRow(
    title: String,
    subtitle: String? = null,
    icon: (@Composable (tint: Color) -> Unit)? = null,
    trailingContent: (@Composable (isFocused: Boolean) -> Unit)? = null,
    focusRequester: FocusRequester? = null,
    isFirstItem: Boolean = false,
    isLastItem: Boolean = false,
    onClick: () -> Unit,
    onNavigateLeft: (() -> Unit)? = null,
    onNavigateRight: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val soundManager = remember { TaloApplication.instance.soundEffectManager }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    LaunchedEffect(isFocused) {
        if (isFocused) {
            soundManager.playFocusSound()
        }
    }

    val bgColor = if (isFocused) Color(0xFF3C4043) else Color.Transparent
    val titleColor = if (isFocused) Color.White else Color(0xFFE8EAED)
    val subtitleColor = if (isFocused) Color(0xFFBDC1C6) else Color(0xFF9AA0A6)
    val iconColor = if (isFocused) Color.White else Color(0xFFBDC1C6)

    var rowModifier = modifier
        .fillMaxWidth()
        .background(bgColor)

    if (focusRequester != null) {
        rowModifier = rowModifier.focusRequester(focusRequester)
    }

    rowModifier = rowModifier
        .onPreviewKeyEvent { event ->
            if (event.type == KeyEventType.KeyDown) {
                val code = event.nativeKeyEvent.keyCode
                val repeatCount = event.nativeKeyEvent.repeatCount

                // Strict boundary lock: never let focus escape out of the side panel vertically
                if (isFirstItem && (event.key == Key.DirectionUp || code == android.view.KeyEvent.KEYCODE_DPAD_UP)) {
                    return@onPreviewKeyEvent true
                }
                if (isLastItem && (event.key == Key.DirectionDown || code == android.view.KeyEvent.KEYCODE_DPAD_DOWN)) {
                    return@onPreviewKeyEvent true
                }

                // Suppress high-frequency repeat flutters on action keys
                if (repeatCount > 0) {
                    return@onPreviewKeyEvent true
                }

                if (event.key == Key.DirectionRight || code == android.view.KeyEvent.KEYCODE_DPAD_RIGHT) {
                    if (onNavigateRight != null) {
                        soundManager.playSelectSound()
                        onNavigateRight()
                    }
                    return@onPreviewKeyEvent true
                } else if (event.key == Key.DirectionLeft || code == android.view.KeyEvent.KEYCODE_DPAD_LEFT) {
                    if (onNavigateLeft != null) {
                        soundManager.playSelectSound()
                        onNavigateLeft()
                        return@onPreviewKeyEvent true
                    }
                } else if (event.key == Key.DirectionCenter || event.key == Key.Enter ||
                    code == android.view.KeyEvent.KEYCODE_DPAD_CENTER || code == android.view.KeyEvent.KEYCODE_ENTER
                ) {
                    soundManager.playSelectSound()
                    onClick()
                    return@onPreviewKeyEvent true
                }
            }
            false
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null
        ) {
            soundManager.playSelectSound()
            onClick()
        }
        .focusable(interactionSource = interactionSource)

    Row(
        modifier = rowModifier
            .defaultMinSize(minHeight = 64.dp)
            .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier.size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                icon(iconColor)
            }
            Spacer(modifier = Modifier.width(16.dp))
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                color = titleColor,
                fontSize = 15.sp,
                fontWeight = if (isFocused) FontWeight.Medium else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = subtitleColor,
                    fontSize = 12.5.sp,
                    lineHeight = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (trailingContent != null) {
            Spacer(modifier = Modifier.width(12.dp))
            trailingContent(isFocused)
        }
    }
}

/**
 * Interrupteur à bascule Android TV / Google TV Settings (M3 Leanback Switch)
 * Dimensions : 36dp x 20dp avec curseur rond 16dp
 * ON : Rail bleu/ardoise (#415D79), curseur bleu Google TV Leanback (#8AB4F8)
 * OFF : Rail ardoise sombre (#383B40), curseur gris clair (#BDC1C6)
 */
@Composable
private fun AndroidTvSwitch(
    checked: Boolean,
    isFocusedRow: Boolean
) {
    val trackBg = if (checked) {
        Color(0xFF415D79)
    } else {
        if (isFocusedRow) Color(0xFF484C51) else Color(0xFF383B40)
    }

    val thumbColor = if (checked) {
        Color(0xFF8AB4F8)
    } else {
        if (isFocusedRow) Color(0xFFBDC1C6) else Color(0xFF9AA0A6)
    }

    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 18.dp else 2.dp,
        animationSpec = tween(150),
        label = "switchThumbOffset"
    )

    Box(
        modifier = Modifier
            .width(36.dp)
            .height(20.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(trackBg),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(16.dp)
                .clip(CircleShape)
                .background(thumbColor)
        )
    }
}

/**
 * Chevron indicateur de sous-menu Android TV Leanback
 */
@Composable
private fun AndroidTvChevron(isFocused: Boolean) {
    Text(
        text = "›",
        color = if (isFocused) Color.White else Color(0xFF80868B),
        fontSize = 22.sp,
        fontWeight = FontWeight.Light
    )
}

/**
 * Valeur secondaire avec chevron optionnel pour sous-menu Android TV Leanback
 */
@Composable
private fun AndroidTvValueText(
    value: String,
    isFocused: Boolean,
    showChevron: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = value,
            color = if (isFocused) Color(0xFFBDC1C6) else Color(0xFF9AA0A6),
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal
        )
        if (showChevron) {
            Text(
                text = "›",
                color = if (isFocused) Color.White else Color(0xFF80868B),
                fontSize = 20.sp,
                fontWeight = FontWeight.Light
            )
        }
    }
}

/**
 * Lance l'activité native Android TV Settings de manière explicite et robuste
 */
private fun launchAndroidTvSettings(context: Context) {
    try {
        val intent = Intent().apply {
            setClassName("com.android.tv.settings", "com.android.tv.settings.MainSettings")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        try {
            val fallback = Intent(Settings.ACTION_SETTINGS).apply {
                setPackage("com.android.tv.settings")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallback)
        } catch (_: Exception) {
            try {
                val generalFallback = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(generalFallback)
            } catch (_: Exception) {
                Toast.makeText(context, "Ouverture des Paramètres TV...", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

/**
 * Ouvre la page de mise à jour système Android TV
 */
private fun launchSystemUpdate(context: Context) {
    val intents = listOf(
        Intent("android.settings.SYSTEM_UPDATE_SETTINGS"),
        Intent().setClassName("com.google.android.gms", "com.google.android.gms.update.SystemUpdateActivity"),
        Intent().setClassName("com.android.tv.settings", "com.android.tv.settings.about.AboutSettings"),
        Intent(Settings.ACTION_DEVICE_INFO_SETTINGS)
    )
    for (intent in intents) {
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            return
        } catch (_: Exception) {}
    }
    Toast.makeText(context, "Page de mise à jour système introuvable", Toast.LENGTH_SHORT).show()
}

// ============================================================
// Vector / Canvas Icons for Google TV Settings Items
// ============================================================

@Composable
fun TvMonitorIcon(size: Dp, tint: Color) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Monitor screen outline
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.1f, h * 0.15f),
            size = Size(w * 0.8f, h * 0.58f),
            cornerRadius = CornerRadius(4f, 4f),
            style = Stroke(width = 2.2f)
        )

        // Stand neck
        drawLine(
            color = tint,
            start = Offset(w * 0.5f, h * 0.73f),
            end = Offset(w * 0.5f, h * 0.85f),
            strokeWidth = 2.2f,
            cap = StrokeCap.Round
        )

        // Stand base
        drawLine(
            color = tint,
            start = Offset(w * 0.3f, h * 0.85f),
            end = Offset(w * 0.7f, h * 0.85f),
            strokeWidth = 2.2f,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun SparkleTaloIcon(size: Dp, tint: Color) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // 4-point Sparkle Star
        val path = Path().apply {
            moveTo(w * 0.5f, h * 0.1f)
            quadraticTo(w * 0.5f, h * 0.5f, w * 0.9f, h * 0.5f)
            quadraticTo(w * 0.5f, h * 0.5f, w * 0.5f, h * 0.9f)
            quadraticTo(w * 0.5f, h * 0.5f, w * 0.1f, h * 0.5f)
            quadraticTo(w * 0.5f, h * 0.5f, w * 0.5f, h * 0.1f)
            close()
        }
        drawPath(path = path, color = tint)
    }
}

@Composable
fun CloudCheckIcon(size: Dp, tint: Color) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Cloud contour path
        val cloudPath = Path().apply {
            moveTo(w * 0.28f, h * 0.68f)
            lineTo(w * 0.74f, h * 0.68f)
            quadraticTo(w * 0.88f, h * 0.68f, w * 0.88f, h * 0.54f)
            quadraticTo(w * 0.88f, h * 0.42f, w * 0.76f, h * 0.40f)
            quadraticTo(w * 0.74f, h * 0.22f, w * 0.54f, h * 0.22f)
            quadraticTo(w * 0.38f, h * 0.22f, w * 0.32f, h * 0.34f)
            quadraticTo(w * 0.16f, h * 0.36f, w * 0.16f, h * 0.54f)
            quadraticTo(w * 0.16f, h * 0.68f, w * 0.28f, h * 0.68f)
            close()
        }
        drawPath(
            path = cloudPath,
            color = tint,
            style = Stroke(width = 2.2f, cap = StrokeCap.Round)
        )

        // Checkmark inside cloud
        val checkPath = Path().apply {
            moveTo(w * 0.40f, h * 0.48f)
            lineTo(w * 0.49f, h * 0.57f)
            lineTo(w * 0.64f, h * 0.40f)
        }
        drawPath(
            path = checkPath,
            color = tint,
            style = Stroke(width = 2.2f, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun GraphicsMonitorIcon(size: Dp, tint: Color) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Screen frame
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.12f, h * 0.18f),
            size = Size(w * 0.76f, h * 0.54f),
            cornerRadius = CornerRadius(4f, 4f),
            style = Stroke(width = 2f)
        )

        // Wave / speed lines inside
        drawLine(
            color = tint,
            start = Offset(w * 0.25f, h * 0.45f),
            end = Offset(w * 0.75f, h * 0.45f),
            strokeWidth = 2f,
            cap = StrokeCap.Round
        )

        // Stand base
        drawLine(
            color = tint,
            start = Offset(w * 0.35f, h * 0.82f),
            end = Offset(w * 0.65f, h * 0.82f),
            strokeWidth = 2f,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun ShieldIcon(size: Dp, tint: Color) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        val shieldPath = Path().apply {
            moveTo(w * 0.5f, h * 0.12f)
            lineTo(w * 0.82f, h * 0.26f)
            quadraticTo(w * 0.82f, h * 0.62f, w * 0.5f, h * 0.88f)
            quadraticTo(w * 0.18f, h * 0.62f, w * 0.18f, h * 0.26f)
            close()
        }
        drawPath(
            path = shieldPath,
            color = tint,
            style = Stroke(width = 2.2f)
        )

        // Small inner checkmark
        val checkPath = Path().apply {
            moveTo(w * 0.36f, h * 0.50f)
            lineTo(w * 0.46f, h * 0.60f)
            lineTo(w * 0.66f, h * 0.40f)
        }
        drawPath(
            path = checkPath,
            color = tint,
            style = Stroke(width = 2.2f, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun InfoCircleIcon(size: Dp, tint: Color) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Circle outline
        drawCircle(
            color = tint,
            radius = w * 0.38f,
            center = Offset(w * 0.5f, h * 0.5f),
            style = Stroke(width = 2f)
        )

        // 'i' dot
        drawCircle(
            color = tint,
            radius = 2.5f,
            center = Offset(w * 0.5f, h * 0.34f)
        )

        // 'i' body
        drawLine(
            color = tint,
            start = Offset(w * 0.5f, h * 0.45f),
            end = Offset(w * 0.5f, h * 0.68f),
            strokeWidth = 2.2f,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun ClockTimerIcon(size: Dp, tint: Color) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Clock outline
        drawCircle(
            color = tint,
            radius = w * 0.38f,
            center = Offset(w * 0.5f, h * 0.5f),
            style = Stroke(width = 2.2f)
        )

        // Center pivot
        drawCircle(
            color = tint,
            radius = 2f,
            center = Offset(w * 0.5f, h * 0.5f)
        )

        // Hour hand
        drawLine(
            color = tint,
            start = Offset(w * 0.5f, h * 0.5f),
            end = Offset(w * 0.5f, h * 0.28f),
            strokeWidth = 2.2f,
            cap = StrokeCap.Round
        )

        // Minute hand
        drawLine(
            color = tint,
            start = Offset(w * 0.5f, h * 0.5f),
            end = Offset(w * 0.68f, h * 0.5f),
            strokeWidth = 2.2f,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun UpdateCheckIcon(size: Dp, tint: Color) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Circular sweep arc
        drawArc(
            color = tint,
            startAngle = 35f,
            sweepAngle = 285f,
            useCenter = false,
            topLeft = Offset(w * 0.15f, h * 0.15f),
            size = Size(w * 0.70f, h * 0.70f),
            style = Stroke(width = 2.2f, cap = StrokeCap.Round)
        )

        // Arrow head at top end
        val arrowPath = Path().apply {
            moveTo(w * 0.84f, h * 0.40f)
            lineTo(w * 0.88f, h * 0.58f)
            lineTo(w * 0.70f, h * 0.56f)
        }
        drawPath(
            path = arrowPath,
            color = tint,
            style = Stroke(width = 2.2f, cap = StrokeCap.Round)
        )
    }
}
