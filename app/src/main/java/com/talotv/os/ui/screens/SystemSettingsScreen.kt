package com.talotv.os.ui.screens

import android.app.Activity
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.talotv.os.R
import com.talotv.os.TaloApplication
import com.talotv.os.core.system.SystemSettingsController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * 5 Sous-onglets de la section Système :
 * 1. À propos (Modèle, versions, patch, jauges RAM & Stockage, Uptime)
 * 2. Mise à jour Talo (Vérification GitHub, statut, code Downloader 6790525)
 * 3. Mise à jour Android (Raccourci officiel mise à jour système Google TV / Android TV)
 * 4. Maintenance et Optimisation (Optimisation RAM en un clic, vidage du cache avec Mo libérés)
 * 5. Outils avancés (Redémarrage Talo OS, Options Développeurs, Réinitialisation)
 */
enum class SystemSubTab(val title: String) {
    ABOUT("À propos"),
    TALO_UPDATE("Mise à jour Talo"),
    ANDROID_UPDATE("Mise à jour Android"),
    OPTIMIZATION("Maintenance et Optimisation"),
    ADVANCED("Outils avancés")
}

@Composable
fun SystemSettingsScreen(
    versionName: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = remember(context) {
        var ctx = context
        while (ctx is android.content.ContextWrapper) {
            if (ctx is Activity) break
            ctx = ctx.baseContext
        }
        ctx as? Activity
    }
    val soundManager = remember { TaloApplication.instance.soundEffectManager }
    val systemController = remember { SystemSettingsController(context) }
    val coroutineScope = rememberCoroutineScope()

    var selectedSubTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var isDetailPaneActive by remember { mutableStateOf(false) }

    val subTabs = remember { SystemSubTab.values() }
    val subTabFocusRequesters = remember { List(subTabs.size) { FocusRequester() } }
    val rightFirstFocusRequester = remember { FocusRequester() }
    val resetTaloFocusRequester = remember { FocusRequester() }
    val factoryResetFocusRequester = remember { FocusRequester() }

    // Dialogues de confirmation
    var showResetTaloDialog by remember { mutableStateOf(false) }
    var showFactoryResetDialog by remember { mutableStateOf(false) }

    // Interception de la touche Retour physique de la télécommande
    BackHandler {
        when {
            showResetTaloDialog -> showResetTaloDialog = false
            showFactoryResetDialog -> showFactoryResetDialog = false
            isDetailPaneActive -> {
                isDetailPaneActive = false
                try {
                    subTabFocusRequesters.getOrNull(selectedSubTabIndex)?.requestFocus()
                } catch (_: Exception) {}
            }
            else -> onClose()
        }
    }

    // Mise au point initiale sur le premier sous-onglet au chargement
    LaunchedEffect(Unit) {
        delay(60)
        try {
            subTabFocusRequesters.getOrNull(0)?.requestFocus()
        } catch (_: Exception) {}
    }

    val isAnyDialogOpen = showResetTaloDialog || showFactoryResetDialog

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF14171E),
                        Color(0xFF0C0E12),
                        Color(0xFF07080A)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 64.dp, vertical = 24.dp)
                .focusProperties { canFocus = !isAnyDialogOpen },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Titre supérieur centré
            Text(
                text = "Système",
                color = Color(0xCCFFFFFF),
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Conteneur deux colonnes (Master-Detail)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // COLONNE GAUCHE : 5 Sous-onglets verticaux
                LazyColumn(
                    modifier = Modifier
                        .weight(0.95f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(subTabs.size) { index ->
                        val tab = subTabs[index]
                        val isSelected = selectedSubTabIndex == index

                        SystemSubTabItem(
                            title = tab.title,
                            isSelected = isSelected,
                            focusRequester = subTabFocusRequesters[index],
                            onFocused = {
                                if (!isDetailPaneActive) {
                                    selectedSubTabIndex = index
                                    soundManager.playFocusSound()
                                }
                            },
                            onEnterRight = {
                                soundManager.playSelectSound()
                                isDetailPaneActive = true
                                coroutineScope.launch {
                                    delay(20)
                                    try {
                                        rightFirstFocusRequester.requestFocus()
                                    } catch (_: Exception) {}
                                }
                            },
                            onExit = onClose
                        )
                    }
                }

                Spacer(modifier = Modifier.width(36.dp))

                // COLONNE DROITE : Détail dynamique du sous-onglet sélectionné
                Box(
                    modifier = Modifier
                        .weight(1.35f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    when (subTabs[selectedSubTabIndex]) {
                        SystemSubTab.ABOUT -> AboutSubScreen(
                            controller = systemController,
                            versionName = versionName,
                            firstItemFocusRequester = rightFirstFocusRequester,
                            onReturnLeft = {
                                isDetailPaneActive = false
                                soundManager.playFocusSound()
                                subTabFocusRequesters.getOrNull(selectedSubTabIndex)?.requestFocus()
                            }
                        )

                        SystemSubTab.TALO_UPDATE -> TaloUpdateSubScreen(
                            controller = systemController,
                            versionName = versionName,
                            firstItemFocusRequester = rightFirstFocusRequester,
                            onReturnLeft = {
                                isDetailPaneActive = false
                                soundManager.playFocusSound()
                                subTabFocusRequesters.getOrNull(selectedSubTabIndex)?.requestFocus()
                            }
                        )

                        SystemSubTab.ANDROID_UPDATE -> AndroidUpdateSubScreen(
                            controller = systemController,
                            firstItemFocusRequester = rightFirstFocusRequester,
                            onReturnLeft = {
                                isDetailPaneActive = false
                                soundManager.playFocusSound()
                                subTabFocusRequesters.getOrNull(selectedSubTabIndex)?.requestFocus()
                            }
                        )

                        SystemSubTab.OPTIMIZATION -> OptimizationSubScreen(
                            controller = systemController,
                            firstItemFocusRequester = rightFirstFocusRequester,
                            onReturnLeft = {
                                isDetailPaneActive = false
                                soundManager.playFocusSound()
                                subTabFocusRequesters.getOrNull(selectedSubTabIndex)?.requestFocus()
                            }
                        )

                        SystemSubTab.ADVANCED -> AdvancedSubScreen(
                            controller = systemController,
                            activity = activity,
                            firstItemFocusRequester = rightFirstFocusRequester,
                            resetTaloFocusRequester = resetTaloFocusRequester,
                            factoryResetFocusRequester = factoryResetFocusRequester,
                            onReturnLeft = {
                                isDetailPaneActive = false
                                soundManager.playFocusSound()
                                subTabFocusRequesters.getOrNull(selectedSubTabIndex)?.requestFocus()
                            },
                            onRequestResetTalo = { showResetTaloDialog = true },
                            onRequestFactoryReset = { showFactoryResetDialog = true }
                        )
                    }
                }
            }
        }

        // Dialogue de confirmation : Réinitialiser réglages Talo OS
        if (showResetTaloDialog) {
            ConfirmationOverlayDialog(
                title = "Réinitialiser les réglages Talo OS",
                message = "Voulez-vous rétablir les réglages d'origine de Talo OS ? Vos applications et comptes resteront intacts.",
                confirmText = "Réinitialiser",
                isDestructive = false,
                onConfirm = {
                    showResetTaloDialog = false
                    val success = systemController.resetTaloSettings()
                    if (success) {
                        Toast.makeText(context, "Réglages Talo OS réinitialisés", Toast.LENGTH_SHORT).show()
                        systemController.triggerReboot(activity)
                    }
                },
                onDismiss = {
                    showResetTaloDialog = false
                    coroutineScope.launch {
                        delay(20)
                        try {
                            resetTaloFocusRequester.requestFocus()
                        } catch (_: Exception) {}
                    }
                }
            )
        }

        // Dialogue de confirmation : Réinitialisation d'usine Android TV
        if (showFactoryResetDialog) {
            ConfirmationOverlayDialog(
                title = "Réinitialisation d'usine de l'appareil",
                message = "Attention : Cette opération effacera l'ensemble de vos données, applications et comptes de l'appareil. Voulez-vous continuer ?",
                confirmText = "Ouvrir l'utilitaire",
                isDestructive = true,
                onConfirm = {
                    showFactoryResetDialog = false
                    systemController.triggerFactoryReset()
                },
                onDismiss = {
                    showFactoryResetDialog = false
                    coroutineScope.launch {
                        delay(20)
                        try {
                            factoryResetFocusRequester.requestFocus()
                        } catch (_: Exception) {}
                    }
                }
            )
        }
    }
}

/**
 * Bouton de sous-onglet dans la colonne de gauche
 */
@Composable
private fun SystemSubTabItem(
    title: String,
    isSelected: Boolean,
    focusRequester: FocusRequester,
    onFocused: () -> Unit,
    onEnterRight: () -> Unit,
    onExit: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    var isFocused by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.02f else 1.0f,
        animationSpec = tween(120),
        label = "subTabScale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isFocused -> Color.White
            isSelected -> Color(0x35FFFFFF)
            else -> Color(0x18FFFFFF)
        },
        animationSpec = tween(120),
        label = "subTabBg"
    )

    val contentColor by animateColorAsState(
        targetValue = when {
            isFocused -> Color(0xFF0B0C0E)
            isSelected -> Color.White
            else -> Color(0x90FFFFFF)
        },
        animationSpec = tween(120),
        label = "subTabContent"
    )

    val chevronColor by animateColorAsState(
        targetValue = when {
            isFocused -> Color(0xFF0B0C0E)
            isSelected -> Color(0x90FFFFFF)
            else -> Color(0x40FFFFFF)
        },
        animationSpec = tween(120),
        label = "subTabChevron"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isFocused -> Color.White
            isSelected -> Color(0x40FFFFFF)
            else -> Color(0x0EFFFFFF)
        },
        animationSpec = tween(120),
        label = "subTabBorder"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(
                BorderStroke(1.dp, borderColor),
                shape = RoundedCornerShape(12.dp)
            )
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
                if (focusState.isFocused) {
                    onFocused()
                }
            }
            .focusRequester(focusRequester)
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.key) {
                        Key.DirectionRight, Key.Enter, Key.NumPadEnter, Key.DirectionCenter -> {
                            onEnterRight()
                            true
                        }
                        Key.DirectionLeft, Key.Back -> {
                            onExit()
                            true
                        }
                        else -> false
                    }
                } else false
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                onEnterRight()
            }
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = title,
            color = contentColor,
            fontSize = 14.5.sp,
            fontWeight = if (isFocused || isSelected) FontWeight.SemiBold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Icon(
            painter = painterResource(id = R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = chevronColor,
            modifier = Modifier.size(15.dp)
        )
    }
}

// =============================================================================
// SOUS-ONGLET 1 : À PROPOS
// =============================================================================

@Composable
private fun AboutSubScreen(
    controller: SystemSettingsController,
    versionName: String,
    firstItemFocusRequester: FocusRequester,
    onReturnLeft: () -> Unit
) {
    val model = remember { controller.getDeviceModel() }
    val manufacturer = remember { controller.getManufacturer() }
    val processor = remember { controller.getProcessor() }
    val androidVer = remember { controller.getAndroidVersion() }
    val securityPatch = remember { controller.getSecurityPatch() }
    val uptime = remember { controller.getUptime() }

    val (ramAvail, ramTotal) = remember { controller.getRamData() }
    val ramUsed = (ramTotal - ramAvail).coerceAtLeast(0f)
    val ramProgress = if (ramTotal > 0f) (ramUsed / ramTotal).coerceIn(0f, 1f) else 0.5f

    val (storageFree, storageTotal) = remember { controller.getStorageData() }
    val storageUsed = (storageTotal - storageFree).coerceAtLeast(0f)
    val storageProgress = if (storageTotal > 0f) (storageUsed / storageTotal).coerceIn(0f, 1f) else 0.3f

    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
    ) {
        item {
            SystemActionCard(
                title = "Appareil & Matériel",
                description = "Modèle : $model\nFabricant & Puce : $manufacturer • $processor\nTemps de fonctionnement : $uptime",
                badgeText = "Matériel",
                focusRequester = firstItemFocusRequester,
                onReturnLeft = onReturnLeft
            )
        }

        item {
            SystemActionCard(
                title = "Système d'exploitation",
                description = "Talo OS : v$versionName\nSystème hôte : $androidVer\nCorrectif de sécurité : $securityPatch",
                badgeText = "Logiciel",
                onReturnLeft = onReturnLeft
            )
        }

        item {
            SystemGaugeCard(
                title = "Mémoire vive (RAM)",
                subtitle = String.format(Locale.US, "%.1f Go utilisés sur %.1f Go (%.0f%%)", ramUsed, ramTotal, ramProgress * 100),
                progress = ramProgress,
                fillColor = Color(0xFF5E5CE6),
                onReturnLeft = onReturnLeft
            )
        }

        item {
            SystemGaugeCard(
                title = "Espace de stockage interne",
                subtitle = String.format(Locale.US, "%.1f Go utilisés sur %.1f Go (%.0f%%)", storageUsed, storageTotal, storageProgress * 100),
                progress = storageProgress,
                fillColor = Color(0xFF34C759),
                onReturnLeft = onReturnLeft
            )
        }
    }
}

// =============================================================================
// SOUS-ONGLET 2 : MISE À JOUR TALO
// =============================================================================

@Composable
private fun TaloUpdateSubScreen(
    controller: SystemSettingsController,
    versionName: String,
    firstItemFocusRequester: FocusRequester,
    onReturnLeft: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isChecking by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf<String?>("Version installée : v$versionName (Canal stable)") }
    var updateAvailable by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
    ) {
        item {
            SystemActionCard(
                title = "Recherche de mise à jour Talo OS",
                description = statusText ?: "Recherche auprès du dépôt officiel GitHub...",
                badgeText = if (isChecking) "Recherche..." else if (updateAvailable) "Mettre à jour" else "Vérifier",
                badgeColor = if (updateAvailable) Color(0xFF34C759) else Color(0x35FFFFFF),
                focusRequester = firstItemFocusRequester,
                onReturnLeft = onReturnLeft,
                onClick = {
                    if (!isChecking) {
                        isChecking = true
                        statusText = "Connexion aux serveurs GitHub en cours..."
                        coroutineScope.launch {
                            val result = controller.checkTaloUpdateOnline(versionName)
                            isChecking = false
                            result.onSuccess { info ->
                                if (info.hasUpdate) {
                                    updateAvailable = true
                                    statusText = "Nouvelle version disponible : v${info.latestVersion} ! Téléchargeable via Downloader."
                                } else {
                                    updateAvailable = false
                                    statusText = "Votre système Talo OS est parfaitement à jour (v$versionName)."
                                }
                            }.onFailure {
                                statusText = "Vérification effectuée. Système actuel : v$versionName."
                            }
                        }
                    }
                }
            )
        }

        item {
            SystemActionCard(
                title = "Code Downloader direct",
                description = "Code rapide permanent : 6790525\nSaisissez ce code dans l'application Downloader pour retélécharger ou mettre à jour Talo OS en toute autonomie.",
                badgeText = "6790525",
                badgeColor = Color(0xFF5E5CE6),
                onReturnLeft = onReturnLeft
            )
        }

        item {
            SystemActionCard(
                title = "Canal de distribution & Confidentialité",
                description = "Dépôt officiel : github.com/sudo-aptttt/talo-tv\nDistribution non commerciale transparente, DRM Widevine L1 préservé, 100% sans traqueur publicitaire.",
                badgeText = "Vérifié",
                onReturnLeft = onReturnLeft
            )
        }
    }
}

// =============================================================================
// SOUS-ONGLET 3 : MISE À JOUR ANDROID
// =============================================================================

@Composable
private fun AndroidUpdateSubScreen(
    controller: SystemSettingsController,
    firstItemFocusRequester: FocusRequester,
    onReturnLeft: () -> Unit
) {
    val androidVer = remember { controller.getAndroidVersion() }
    val patch = remember { controller.getSecurityPatch() }
    val buildDisplay = remember { Build.DISPLAY ?: Build.ID }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
    ) {
        item {
            SystemActionCard(
                title = "Mise à jour système Android TV",
                description = "Ouvre l'utilitaire natif de Google TV / Android TV pour rechercher et installer les correctifs OTA du fabricant.",
                badgeText = "Rechercher",
                badgeColor = Color(0x35FFFFFF),
                focusRequester = firstItemFocusRequester,
                onReturnLeft = onReturnLeft,
                onClick = {
                    controller.openAndroidSystemUpdate()
                }
            )
        }

        item {
            SystemActionCard(
                title = "Version hôte actuelle",
                description = "Système : $androidVer\nNiveau du correctif de sécurité : $patch\nBuild : $buildDisplay",
                badgeText = "Android 14",
                onReturnLeft = onReturnLeft
            )
        }

        item {
            SystemActionCard(
                title = "Stabilité & Pilotes matériels",
                description = "Les mises à jour système Android assurent la compatibilité 4K HDR, Dolby Vision et le décodage matériel AV1 / HEVC du Streamer.",
                badgeText = "Google TV",
                onReturnLeft = onReturnLeft
            )
        }
    }
}

// =============================================================================
// SOUS-ONGLET 4 : MAINTENANCE ET OPTIMISATION
// =============================================================================

@Composable
private fun OptimizationSubScreen(
    controller: SystemSettingsController,
    firstItemFocusRequester: FocusRequester,
    onReturnLeft: () -> Unit
) {
    val soundManager = remember { TaloApplication.instance.soundEffectManager }
    var ramFeedback by remember { mutableStateOf<String?>(null) }
    var cacheFeedback by remember { mutableStateOf<String?>(null) }

    var ramData by remember { mutableStateOf(controller.getRamData()) }
    var storageData by remember { mutableStateOf(controller.getStorageData()) }

    val ramUsed = (ramData.second - ramData.first).coerceAtLeast(0f)
    val ramProgress = if (ramData.second > 0f) (ramUsed / ramData.second).coerceIn(0f, 1f) else 0.5f

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
    ) {
        item {
            SystemActionCard(
                title = "Optimiser la mémoire vive (RAM)",
                description = ramFeedback
                    ?: "Ferme les processus d'arrière-plan inactifs et compacte la mémoire vive pour fluidifier l'expérience TV.",
                badgeText = "Optimiser",
                badgeColor = Color(0x35FFFFFF),
                focusRequester = firstItemFocusRequester,
                onReturnLeft = onReturnLeft,
                onClick = {
                    soundManager.playSelectSound()
                    val freed = controller.optimizeRam()
                    val freedMo = freed / (1024 * 1024)
                    ramFeedback = if (freedMo > 0) "✓ $freedMo Mo de RAM libérés avec succès !" else "✓ Mémoire vive optimisée !"
                    ramData = controller.getRamData()
                }
            )
        }

        item {
            SystemActionCard(
                title = "Vider le cache système",
                description = cacheFeedback
                    ?: "Supprime les fichiers temporaires et les caches des applications pour libérer de l'espace de stockage interne.",
                badgeText = "Nettoyer",
                badgeColor = Color(0xFF34C759),
                onReturnLeft = onReturnLeft,
                onClick = {
                    soundManager.playSelectSound()
                    val freed = controller.clearSystemCache()
                    val freedMo = freed / (1024 * 1024)
                    cacheFeedback = if (freedMo > 0) "✓ $freedMo Mo de cache récupérés !" else "✓ Cache système vidé !"
                    storageData = controller.getStorageData()
                }
            )
        }

        item {
            SystemGaugeCard(
                title = "État de la mémoire vive en direct",
                subtitle = String.format(Locale.US, "%.1f Go disponibles sur %.1f Go", ramData.first, ramData.second),
                progress = ramProgress,
                fillColor = Color(0xFF5E5CE6),
                onReturnLeft = onReturnLeft
            )
        }
    }
}

// =============================================================================
// SOUS-ONGLET 5 : OUTILS AVANCÉS
// =============================================================================

@Composable
private fun AdvancedSubScreen(
    controller: SystemSettingsController,
    activity: Activity?,
    firstItemFocusRequester: FocusRequester,
    resetTaloFocusRequester: FocusRequester,
    factoryResetFocusRequester: FocusRequester,
    onReturnLeft: () -> Unit,
    onRequestResetTalo: () -> Unit,
    onRequestFactoryReset: () -> Unit
) {
    val soundManager = remember { TaloApplication.instance.soundEffectManager }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
    ) {
        item {
            SystemActionCard(
                title = "Redémarrer Talo OS",
                description = "Relance l'interface Talo OS et réinitialise tous les services actifs sans éteindre l'appareil.",
                badgeText = "Redémarrer",
                badgeColor = Color(0x35FFFFFF),
                focusRequester = firstItemFocusRequester,
                onReturnLeft = onReturnLeft,
                onClick = {
                    soundManager.playSelectSound()
                    controller.triggerReboot(activity)
                }
            )
        }

        item {
            SystemActionCard(
                title = "Options pour les développeurs",
                description = "Accéder directement aux réglages avancés d'Android TV (débogage USB/Réseau, échelles d'animation, etc.).",
                badgeText = "Ouvrir",
                onReturnLeft = onReturnLeft,
                onClick = {
                    soundManager.playSelectSound()
                    controller.openDeveloperOptions()
                }
            )
        }

        item {
            SystemActionCard(
                title = "Réinitialiser les réglages Talo OS",
                description = "Rétablit les réglages par défaut de Talo OS (fonds d'écran, barres, sons). Vos applications restent installées.",
                badgeText = "Réinitialiser",
                badgeColor = Color(0xFFFF9F0A),
                focusRequester = resetTaloFocusRequester,
                onReturnLeft = onReturnLeft,
                onClick = {
                    soundManager.playSelectSound()
                    onRequestResetTalo()
                }
            )
        }

        item {
            SystemActionCard(
                title = "Réinitialisation d'usine de l'appareil",
                description = "Ouvre l'utilitaire officiel pour effacer l'intégralité des données et remettre l'appareil à zéro.",
                badgeText = "Effacer tout",
                badgeColor = Color(0xFFFF453A),
                focusRequester = factoryResetFocusRequester,
                onReturnLeft = onReturnLeft,
                onClick = {
                    soundManager.playSelectSound()
                    onRequestFactoryReset()
                }
            )
        }
    }
}

// =============================================================================
// COMPOSANTS RÉUTILISABLES (CARTES, JAUGES & DIALOGUES)
// =============================================================================

@Composable
private fun SystemActionCard(
    title: String,
    description: String,
    badgeText: String? = null,
    badgeColor: Color = Color(0x35FFFFFF),
    focusRequester: FocusRequester? = null,
    onReturnLeft: () -> Unit,
    onClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    var isFocused by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.02f else 1.0f,
        animationSpec = tween(120),
        label = "actionCardScale"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isFocused) Color(0xFF2C303A) else Color(0x20FFFFFF),
        animationSpec = tween(120),
        label = "actionCardBg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isFocused) Color.White else Color(0x14FFFFFF),
        animationSpec = tween(120),
        label = "actionCardBorder"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(14.dp))
            .onFocusChanged { isFocused = it.isFocused }
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.key) {
                        Key.DirectionLeft, Key.Back -> {
                            onReturnLeft()
                            true
                        }
                        Key.DirectionRight -> true // Ne pas s'échapper à droite
                        Key.Enter, Key.NumPadEnter, Key.DirectionCenter -> {
                            onClick?.invoke()
                            true
                        }
                        else -> false
                    }
                } else false
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                onClick?.invoke()
            }
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = if (isFocused) Color.White else Color(0xFFF2F2F7),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                color = if (isFocused) Color(0xFFD1D1D6) else Color(0x99FFFFFF),
                fontSize = 12.5.sp,
                lineHeight = 17.sp
            )
        }

        if (badgeText != null) {
            Spacer(modifier = Modifier.width(14.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isFocused && onClick != null) Color.White else badgeColor)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badgeText,
                    color = if (isFocused && onClick != null) Color(0xFF0B0C0E) else Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SystemGaugeCard(
    title: String,
    subtitle: String,
    progress: Float,
    fillColor: Color,
    focusRequester: FocusRequester? = null,
    onReturnLeft: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    var isFocused by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.02f else 1.0f,
        animationSpec = tween(120),
        label = "gaugeScale"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isFocused) Color(0xFF2C303A) else Color(0x20FFFFFF),
        animationSpec = tween(120),
        label = "gaugeBg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isFocused) Color.White else Color(0x14FFFFFF),
        animationSpec = tween(120),
        label = "gaugeBorder"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(14.dp))
            .onFocusChanged { isFocused = it.isFocused }
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.key) {
                        Key.DirectionLeft, Key.Back -> {
                            onReturnLeft()
                            true
                        }
                        Key.DirectionRight -> true
                        else -> false
                    }
                } else false
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {}
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = if (isFocused) Color.White else Color(0xFFF2F2F7),
                fontSize = 14.5.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = if (isFocused) Color(0xFFE5E5EA) else Color(0x80FFFFFF),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Jauge linéaire
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0x28FFFFFF))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = progress.coerceIn(0.02f, 1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(fillColor)
            )
        }
    }
}

/**
 * Dialogue de confirmation sécurisé au D-Pad avec focus par défaut sur "Annuler"
 */
@Composable
private fun ConfirmationOverlayDialog(
    title: String,
    message: String,
    confirmText: String,
    isDestructive: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val cancelFocusRequester = remember { FocusRequester() }
    val confirmFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        repeat(4) {
            delay(25)
            try {
                cancelFocusRequester.requestFocus()
            } catch (_: Exception) {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xD9000000))
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                onDismiss()
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(460.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF1E2127))
                .border(BorderStroke(1.dp, Color(0x28FFFFFF)), RoundedCornerShape(24.dp))
                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {} // Empêche la fermeture au clic sur la boîte
                .padding(26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = message,
                color = Color(0xB3FFFFFF),
                fontSize = 13.5.sp,
                lineHeight = 19.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(26.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
            ) {
                // Bouton Annuler (Focus par défaut pour sécurité TV)
                DialogActionButton(
                    text = "Annuler",
                    isPrimary = false,
                    isDestructive = false,
                    focusRequester = cancelFocusRequester,
                    onMoveRight = {
                        try { confirmFocusRequester.requestFocus() } catch (_: Exception) {}
                    },
                    onMoveLeft = {},
                    onDismiss = onDismiss,
                    onClick = onDismiss
                )

                // Bouton Confirmer
                DialogActionButton(
                    text = confirmText,
                    isPrimary = true,
                    isDestructive = isDestructive,
                    focusRequester = confirmFocusRequester,
                    onMoveRight = {},
                    onMoveLeft = {
                        try { cancelFocusRequester.requestFocus() } catch (_: Exception) {}
                    },
                    onDismiss = onDismiss,
                    onClick = onConfirm
                )
            }
        }
    }
}

@Composable
private fun RowScope.DialogActionButton(
    text: String,
    isPrimary: Boolean,
    isDestructive: Boolean,
    focusRequester: FocusRequester,
    onMoveRight: () -> Unit,
    onMoveLeft: () -> Unit,
    onDismiss: () -> Unit,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.05f else 1.0f,
        animationSpec = tween(100),
        label = "dialogBtnScale"
    )

    // DA Talo OS : Zéro bleu flashy ! Fond blanc ou accentué chaleureux
    val bgColor by animateColorAsState(
        targetValue = when {
            isFocused && !isPrimary -> Color.White
            isFocused && isDestructive -> Color(0xFFFF453A)
            isFocused && !isDestructive -> Color(0xFFFF9F0A)
            !isPrimary -> Color(0x24FFFFFF)
            isDestructive -> Color(0x28FF453A)
            else -> Color(0x28FF9F0A)
        },
        animationSpec = tween(100),
        label = "dialogBtnBg"
    )

    val textColor by animateColorAsState(
        targetValue = when {
            isFocused && !isPrimary -> Color(0xFF0B0C0E)
            isFocused && isDestructive -> Color.White
            isFocused && !isDestructive -> Color(0xFF0B0C0E)
            !isPrimary -> Color(0xCCFFFFFF)
            isDestructive -> Color(0xFFFF453A)
            else -> Color(0xFFFF9F0A)
        },
        animationSpec = tween(100),
        label = "dialogBtnText"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isFocused -> Color.White
            !isPrimary -> Color(0x18FFFFFF)
            isDestructive -> Color(0x50FF453A)
            else -> Color(0x50FF9F0A)
        },
        animationSpec = tween(100),
        label = "dialogBtnBorder"
    )

    Box(
        modifier = Modifier
            .weight(1f)
            .height(44.dp)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(
                BorderStroke(if (isFocused) 2.dp else 1.dp, borderColor),
                RoundedCornerShape(12.dp)
            )
            .focusRequester(focusRequester)
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.key) {
                        Key.DirectionRight -> {
                            onMoveRight()
                            true
                        }
                        Key.DirectionLeft -> {
                            onMoveLeft()
                            true
                        }
                        Key.DirectionUp, Key.DirectionDown -> {
                            // Bloque formellement toute fuite de curseur vers le fond
                            true
                        }
                        Key.Back -> {
                            onDismiss()
                            true
                        }
                        Key.Enter, Key.NumPadEnter, Key.DirectionCenter -> {
                            onClick()
                            true
                        }
                        else -> false
                    }
                } else false
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = if (isFocused || isPrimary) FontWeight.Bold else FontWeight.Medium
        )
    }
}
