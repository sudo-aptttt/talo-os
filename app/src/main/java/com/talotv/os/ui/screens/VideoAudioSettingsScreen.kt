package com.talotv.os.ui.screens

import android.app.Activity
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.talotv.os.TaloApplication
import com.talotv.os.core.hardware.ProfileType
import com.talotv.os.core.system.SystemSettingsController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 3 Sous-onglets de la section Affichage et son
 */
enum class VideoAudioSubTab(val title: String) {
    DISPLAY_PERF("Affichage & Rendu 4K"),
    AUDIO("Audio & Son Surround"),
    CALIBRATION("Étalonnage TV & HDR")
}

@Composable
fun VideoAudioSettingsScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val soundManager = remember { TaloApplication.instance.soundEffectManager }
    val profileManager = remember { TaloApplication.instance.profileManager }
    val controller = remember { SystemSettingsController(context) }
    val coroutineScope = rememberCoroutineScope()

    var selectedSubTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var isDetailPaneActive by remember { mutableStateOf(false) }

    val subTabs = remember { VideoAudioSubTab.values() }
    val subTabFocusRequesters = remember { List(subTabs.size) { FocusRequester() } }
    val rightFirstFocusRequester = remember { FocusRequester() }

    BackHandler {
        if (isDetailPaneActive) {
            isDetailPaneActive = false
            try {
                subTabFocusRequesters.getOrNull(selectedSubTabIndex)?.requestFocus()
            } catch (_: Exception) {}
        } else {
            onClose()
        }
    }

    LaunchedEffect(Unit) {
        delay(60)
        try {
            subTabFocusRequesters.getOrNull(0)?.requestFocus()
        } catch (_: Exception) {}
    }

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
                .padding(horizontal = 64.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Titre supérieur centré
            Text(
                text = "Affichage et son",
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
                // COLONNE GAUCHE (Sous-onglets)
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

                        VideoAudioSubTabItem(
                            title = tab.title,
                            isSelected = isSelected,
                            isDetailPaneActive = isDetailPaneActive,
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

                // COLONNE DROITE (Contenu dynamique)
                Box(
                    modifier = Modifier
                        .weight(1.35f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    when (subTabs[selectedSubTabIndex]) {
                        VideoAudioSubTab.DISPLAY_PERF -> DisplayPerfSubScreen(
                            controller = controller,
                            firstItemFocusRequester = rightFirstFocusRequester,
                            onReturnLeft = {
                                isDetailPaneActive = false
                                soundManager.playFocusSound()
                                try {
                                    subTabFocusRequesters.getOrNull(selectedSubTabIndex)?.requestFocus()
                                } catch (_: Exception) {}
                            }
                        )
                        VideoAudioSubTab.AUDIO -> AudioSubScreen(
                            controller = controller,
                            firstItemFocusRequester = rightFirstFocusRequester,
                            onReturnLeft = {
                                isDetailPaneActive = false
                                soundManager.playFocusSound()
                                try {
                                    subTabFocusRequesters.getOrNull(selectedSubTabIndex)?.requestFocus()
                                } catch (_: Exception) {}
                            }
                        )
                        VideoAudioSubTab.CALIBRATION -> CalibrationSubScreen(
                            controller = controller,
                            firstItemFocusRequester = rightFirstFocusRequester,
                            onReturnLeft = {
                                isDetailPaneActive = false
                                soundManager.playFocusSound()
                                try {
                                    subTabFocusRequesters.getOrNull(selectedSubTabIndex)?.requestFocus()
                                } catch (_: Exception) {}
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VideoAudioSubTabItem(
    title: String,
    isSelected: Boolean,
    isDetailPaneActive: Boolean,
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
        label = "vaSubTabScale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isFocused -> Color.White
            isSelected -> Color(0x35FFFFFF)
            else -> Color(0x18FFFFFF)
        },
        animationSpec = tween(120),
        label = "vaSubTabBg"
    )

    val contentColor by animateColorAsState(
        targetValue = when {
            isFocused -> Color(0xFF0B0C0E)
            isSelected -> Color.White
            else -> Color(0x90FFFFFF)
        },
        animationSpec = tween(120),
        label = "vaSubTabContent"
    )

    val chevronColor by animateColorAsState(
        targetValue = when {
            isFocused -> Color(0xFF0B0C0E)
            isSelected -> Color(0x90FFFFFF)
            else -> Color(0x40FFFFFF)
        },
        animationSpec = tween(120),
        label = "vaSubTabChevron"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isFocused -> Color.White
            isSelected -> Color(0x40FFFFFF)
            else -> Color(0x0EFFFFFF)
        },
        animationSpec = tween(120),
        label = "vaSubTabBorder"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(12.dp))
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
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            color = contentColor,
            fontSize = 14.5.sp,
            fontWeight = if (isSelected || isFocused) FontWeight.SemiBold else FontWeight.Medium
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = chevronColor,
            modifier = Modifier.size(15.dp)
        )
    }
}

// =============================================================================
// 1. AFFICHAGE & RENDU 4K
// =============================================================================

@Composable
private fun DisplayPerfSubScreen(
    controller: SystemSettingsController,
    firstItemFocusRequester: FocusRequester,
    onReturnLeft: () -> Unit
) {
    val soundManager = remember { TaloApplication.instance.soundEffectManager }
    val profileManager = remember { TaloApplication.instance.profileManager }

    var currentProfile by remember { mutableStateOf(profileManager.getCurrentProfileType()) }
    var allmEnabled by rememberSaveable { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
    ) {
        item {
            VideoAudioActionCard(
                title = "Résolution & Fréquence d'actualisation",
                description = "4K Ultra HD (3840 x 2160) à 60 Hz avec sortie HDMI 2.1 eARC pour une fluidité absolue sans saccade.",
                badgeText = "3840x2160 @ 60Hz",
                badgeColor = Color(0xFF34C759),
                focusRequester = firstItemFocusRequester,
                onReturnLeft = onReturnLeft,
                onClick = {
                    soundManager.playSelectSound()
                    controller.openSystemDisplaySettings()
                }
            )
        }

        item {
            VideoAudioSwitchCard(
                title = "Mode Jeu automatique (ALLM)",
                description = "Active le mode Faible Latence Automatique sur votre téléviseur dès qu'une manette de jeu ou un jeu vidéo est détecté.",
                checked = allmEnabled,
                onReturnLeft = onReturnLeft,
                onToggle = { enable ->
                    allmEnabled = enable
                    soundManager.playSelectSound()
                }
            )
        }

        item {
            VideoAudioActionCard(
                title = "Profil de performance graphique Talo",
                description = "Profil actif : ${currentProfile.name}. Adapte dynamiquement la fréquence du processeur et les shaders de l'interface TV.",
                badgeText = currentProfile.name,
                badgeColor = Color(0xFF34C759),
                onReturnLeft = onReturnLeft,
                onClick = {
                    soundManager.playSelectSound()
                    val nextProfile = when (currentProfile) {
                        ProfileType.STANDARD -> ProfileType.ULTRA
                        ProfileType.ULTRA -> ProfileType.ECO
                        ProfileType.ECO -> ProfileType.STANDARD
                        else -> ProfileType.STANDARD
                    }
                    profileManager.setProfileType(nextProfile)
                    currentProfile = nextProfile
                }
            )
        }

        item {
            VideoAudioActionCard(
                title = "Accélération matérielle Vulkan / GLES",
                description = "Composition visuelle accélérée par processeur graphique avec mémoire tampon triple buffering pour 60 i/s constants.",
                badgeText = "Vulkan 1.3",
                badgeColor = Color(0x35FFFFFF),
                onReturnLeft = onReturnLeft
            )
        }
    }
}

// =============================================================================
// 2. AUDIO & SON SURROUND
// =============================================================================

@Composable
private fun AudioSubScreen(
    controller: SystemSettingsController,
    firstItemFocusRequester: FocusRequester,
    onReturnLeft: () -> Unit
) {
    val soundManager = remember { TaloApplication.instance.soundEffectManager }

    val audioOutputs = listOf("Automatique (HDMI eARC)", "Haut-parleurs TV", "Sortie optique S/PDIF", "Bluetooth")
    var selectedOutputIndex by rememberSaveable { mutableIntStateOf(0) }

    val surroundModes = listOf("Automatique (Recommandé)", "Dolby Digital Plus", "Dolby Digital", "PCM Stéréo")
    var selectedSurroundIndex by rememberSaveable { mutableIntStateOf(0) }
    var nightModeEnabled by rememberSaveable { mutableStateOf(false) }
    var navSoundsEnabled by rememberSaveable { mutableStateOf(soundManager.isSoundEnabled()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
    ) {
        item {
            VideoAudioActionCard(
                title = "Sortie audio principale",
                description = "Destination du flux audio : ${audioOutputs[selectedOutputIndex]}. Prise en charge eARC à très haut débit.",
                badgeText = audioOutputs[selectedOutputIndex],
                badgeColor = Color(0xFF34C759),
                focusRequester = firstItemFocusRequester,
                onReturnLeft = onReturnLeft,
                onClick = {
                    soundManager.playSelectSound()
                    selectedOutputIndex = (selectedOutputIndex + 1) % audioOutputs.size
                }
            )
        }

        item {
            VideoAudioActionCard(
                title = "Format audio Surround",
                description = "Format actif : ${surroundModes[selectedSurroundIndex]}. Détection et décodage multicanal automatique par HDMI eARC ou optique.",
                badgeText = surroundModes[selectedSurroundIndex],
                badgeColor = Color(0xFF34C759),
                onReturnLeft = onReturnLeft,
                onClick = {
                    soundManager.playSelectSound()
                    selectedSurroundIndex = (selectedSurroundIndex + 1) % surroundModes.size
                    controller.setSurroundMode(surroundModes[selectedSurroundIndex])
                }
            )
        }

        item {
            VideoAudioSwitchCard(
                title = "Mode Nuit (Atténuation des bruits forts)",
                description = "Compresse la plage dynamique audio pour rendre les chuchotements intelligibles et adoucir les explosions nocturnes.",
                checked = nightModeEnabled,
                onReturnLeft = onReturnLeft,
                onToggle = { enable ->
                    nightModeEnabled = enable
                    soundManager.playSelectSound()
                }
            )
        }

        item {
            VideoAudioSwitchCard(
                title = "Sons de navigation de l'interface",
                description = "Retour sonore discret lors du déplacement du curseur D-Pad et de la validation des éléments.",
                checked = navSoundsEnabled,
                onReturnLeft = onReturnLeft,
                onToggle = { enable ->
                    navSoundsEnabled = enable
                    soundManager.setSoundEnabled(enable)
                    controller.setNavigationSounds(enable)
                    if (enable) soundManager.playSelectSound()
                }
            )
        }

        item {
            VideoAudioActionCard(
                title = "Égaliseur et réglages audio système",
                description = "Ouvre les paramètres avancés de la sortie audio, du volume absolu et de la synchronisation labiale (A/V sync).",
                badgeText = "Paramètres",
                badgeColor = Color(0x35FFFFFF),
                onReturnLeft = onReturnLeft,
                onClick = {
                    soundManager.playSelectSound()
                    controller.openSystemSoundSettings()
                }
            )
        }
    }
}

// =============================================================================
// 3. ÉTALONNAGE TV & HDR
// =============================================================================

@Composable
private fun CalibrationSubScreen(
    controller: SystemSettingsController,
    firstItemFocusRequester: FocusRequester,
    onReturnLeft: () -> Unit
) {
    val soundManager = remember { TaloApplication.instance.soundEffectManager }

    var matchFrameRate by rememberSaveable { mutableStateOf(true) }
    var hdrEnabled by rememberSaveable { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
    ) {
        item {
            VideoAudioSwitchCard(
                title = "Ajustement de la fréquence au contenu (Match Frame Rate)",
                description = "Synchronise automatiquement la fréquence de la dalle (24 Hz cinéma, 50 Hz européen, 60 Hz) avec la source du film pour éliminer le judder.",
                checked = matchFrameRate,
                focusRequester = firstItemFocusRequester,
                onReturnLeft = onReturnLeft,
                onToggle = { enable ->
                    matchFrameRate = enable
                    soundManager.playSelectSound()
                }
            )
        }

        item {
            VideoAudioSwitchCard(
                title = "Plage dynamique étendue (HDR10+ / Dolby Vision)",
                description = "Transmet les métadonnées dynamiques au téléviseur pour des noirs profonds et des pics lumineux éclatants.",
                checked = hdrEnabled,
                onReturnLeft = onReturnLeft,
                onToggle = { enable ->
                    hdrEnabled = enable
                    soundManager.playSelectSound()
                }
            )
        }

        item {
            VideoAudioActionCard(
                title = "Mire d'étalonnage et surbalayage (Overscan)",
                description = "Vérifier que les 4 coins de l'image coïncident exactement avec les bords physiques de votre dalle TV (pixel matching 1:1).",
                badgeText = "Mire 1:1",
                badgeColor = Color(0x35FFFFFF),
                onReturnLeft = onReturnLeft,
                onClick = {
                    soundManager.playSelectSound()
                    controller.openSystemDisplaySettings()
                }
            )
        }
    }
}

// =============================================================================
// COMPOSANTS CARTE HARMONISÉS
// =============================================================================

@Composable
private fun VideoAudioActionCard(
    title: String,
    description: String,
    badgeText: String? = null,
    badgeColor: Color = Color(0x30FFFFFF),
    focusRequester: FocusRequester? = null,
    onReturnLeft: () -> Unit,
    onClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    var isFocused by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.02f else 1.0f,
        animationSpec = tween(120),
        label = "vaCardScale"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isFocused) Color(0xFF2C303A) else Color(0x20FFFFFF),
        animationSpec = tween(120),
        label = "vaCardBg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isFocused) Color.White else Color(0x14FFFFFF),
        animationSpec = tween(120),
        label = "vaCardBorder"
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
                        Key.DirectionRight -> true
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
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isFocused && onClick != null) Color.White else badgeColor)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badgeText,
                    color = if (isFocused && onClick != null) Color(0xFF0B0C0E) else Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun VideoAudioSwitchCard(
    title: String,
    description: String,
    checked: Boolean,
    focusRequester: FocusRequester? = null,
    onReturnLeft: () -> Unit,
    onToggle: (Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    var isFocused by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.02f else 1.0f,
        animationSpec = tween(120),
        label = "vaSwitchCardScale"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isFocused) Color(0xFF2C303A) else Color(0x20FFFFFF),
        animationSpec = tween(120),
        label = "vaSwitchCardBg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isFocused) Color.White else Color(0x14FFFFFF),
        animationSpec = tween(120),
        label = "vaSwitchCardBorder"
    )

    val knobOffset by animateDpAsState(
        targetValue = if (checked) 22.dp else 3.dp,
        animationSpec = tween(120),
        label = "knobOffset"
    )

    val switchBgColor by animateColorAsState(
        targetValue = if (checked) Color(0xFF34C759) else Color(0x35FFFFFF),
        animationSpec = tween(120),
        label = "switchBg"
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
                        Key.DirectionRight -> true
                        Key.Enter, Key.NumPadEnter, Key.DirectionCenter -> {
                            onToggle(!checked)
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
                onToggle(!checked)
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

        Spacer(modifier = Modifier.width(16.dp))

        Box(
            modifier = Modifier
                .width(48.dp)
                .height(28.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(switchBgColor)
                .padding(vertical = 3.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .offset(x = knobOffset)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
    }
}
