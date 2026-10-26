package com.talotv.os.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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

/**
 * 3 Sous-onglets de la section Vidéo et audio
 */
enum class VideoAudioSubTab(val title: String) {
    DISPLAY_PERF("Affichage & Rendu"),
    AUDIO("Audio & Sonorité"),
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
                .padding(horizontal = 48.dp, vertical = 20.dp)
        ) {
            // EN-TÊTE
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0x18FFFFFF))
                        .clickable { onClose() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "Vidéo et audio",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Profils de rendu graphique Talo OS, sortie acoustique et étalonnage 4K HDR",
                        color = Color(0x88FFFFFF),
                        fontSize = 12.sp
                    )
                }
            }

            // CORPS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                // COLONNE GAUCHE (Sous-onglets)
                Column(
                    modifier = Modifier
                        .weight(1.0f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    subTabs.forEachIndexed { index, subTab ->
                        val isSelected = (index == selectedSubTabIndex)
                        VideoAudioSubTabItem(
                            title = subTab.title,
                            isSelected = isSelected,
                            isDetailPaneActive = isDetailPaneActive,
                            focusRequester = subTabFocusRequesters[index],
                            onFocused = {
                                if (!isDetailPaneActive) {
                                    selectedSubTabIndex = index
                                    soundManager.playFocusSound()
                                }
                            },
                            onEnterDetail = {
                                selectedSubTabIndex = index
                                isDetailPaneActive = true
                                soundManager.playSelectSound()
                                try {
                                    rightFirstFocusRequester.requestFocus()
                                } catch (_: Exception) {}
                            }
                        )
                    }
                }

                // COLONNE DROITE (Contenu)
                Box(
                    modifier = Modifier
                        .weight(1.8f)
                        .fillMaxHeight()
                ) {
                    when (subTabs[selectedSubTabIndex]) {
                        VideoAudioSubTab.DISPLAY_PERF -> DisplayPerfSubScreen(
                            firstItemFocusRequester = rightFirstFocusRequester,
                            onReturnLeft = {
                                isDetailPaneActive = false
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
    onEnterDetail: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.02f else 1.0f,
        animationSpec = tween(120),
        label = "vaSubTabScale"
    )

    val bgColor by animateColorAsState(
        targetValue = when {
            isFocused -> Color(0xFF2C303A)
            isSelected && isDetailPaneActive -> Color(0x30FFFFFF)
            isSelected -> Color(0x22FFFFFF)
            else -> Color(0x10FFFFFF)
        },
        animationSpec = tween(120),
        label = "vaSubTabBg"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isFocused -> Color.White
            isSelected && isDetailPaneActive -> Color(0x50FFFFFF)
            isSelected -> Color(0x25FFFFFF)
            else -> Color(0x0CFFFFFF)
        },
        animationSpec = tween(120),
        label = "vaSubTabBorder"
    )

    val textColor by animateColorAsState(
        targetValue = when {
            isFocused -> Color.White
            isSelected -> Color.White
            else -> Color(0x99FFFFFF)
        },
        animationSpec = tween(120),
        label = "vaSubTabText"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(12.dp))
            .focusRequester(focusRequester)
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.key) {
                        Key.DirectionRight, Key.Enter, Key.NumPadEnter, Key.DirectionCenter -> {
                            onEnterDetail()
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
                onEnterDetail()
            }
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            color = textColor,
            fontSize = 14.5.sp,
            fontWeight = if (isSelected || isFocused) FontWeight.SemiBold else FontWeight.Medium
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = if (isFocused || isSelected) Color.White else Color(0x40FFFFFF),
            modifier = Modifier.size(15.dp)
        )
    }
}

// =============================================================================
// 1. AFFICHAGE & RENDU
// =============================================================================

@Composable
private fun DisplayPerfSubScreen(
    firstItemFocusRequester: FocusRequester,
    onReturnLeft: () -> Unit
) {
    val soundManager = remember { TaloApplication.instance.soundEffectManager }
    val profileManager = remember { TaloApplication.instance.profileManager }
    var currentProfile by remember { mutableStateOf(profileManager.getCurrentProfileType()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
    ) {
        item {
            VideoAudioActionCard(
                title = "Profil de performance Talo OS",
                description = "Sélectionnez le niveau d'optimisation graphique du système : Éco (économie GPU), Standard (optimal 60 FPS avec flou fluide), Ultra (qualité visuelle maximale). Cliquez pour basculer.",
                badgeText = currentProfile.name,
                badgeColor = Color(0xFF34C759),
                focusRequester = firstItemFocusRequester,
                onReturnLeft = onReturnLeft,
                onClick = {
                    soundManager.playSelectSound()
                    val next = when (currentProfile) {
                        ProfileType.ECO -> ProfileType.STANDARD
                        ProfileType.STANDARD -> ProfileType.ULTRA
                        ProfileType.ULTRA -> ProfileType.ECO
                        else -> ProfileType.STANDARD
                    }
                    profileManager.setProfileType(next)
                    currentProfile = next
                }
            )
        }

        item {
            VideoAudioActionCard(
                title = "Effets Frosted Glass (Verre dépoli)",
                description = "Flou gaussien immersif en temps réel sous les fenêtres de navigation et les panneaux de réglages.",
                badgeText = "Activé",
                badgeColor = Color(0x28FFFFFF),
                onReturnLeft = onReturnLeft
            )
        }

        item {
            VideoAudioActionCard(
                title = "Fréquence d'affichage cible de l'UI",
                description = "Moteur de rendu Jetpack Compose cadencé à 60 images par seconde constantes pour une réactivité instantanée à la télécommande.",
                badgeText = "60 FPS",
                badgeColor = Color(0x28FFFFFF),
                onReturnLeft = onReturnLeft
            )
        }
    }
}

// =============================================================================
// 2. AUDIO & SONORITÉ
// =============================================================================

@Composable
private fun AudioSubScreen(
    controller: SystemSettingsController,
    firstItemFocusRequester: FocusRequester,
    onReturnLeft: () -> Unit
) {
    val soundManager = remember { TaloApplication.instance.soundEffectManager }
    var isSoundEnabled by remember { mutableStateOf(soundManager.isSoundEnabled()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
    ) {
        item {
            VideoAudioSwitchCard(
                title = "Effets sonores de navigation",
                description = "Émet un son doux lors de chaque déplacement et de chaque clic dans l'interface de Talo OS.",
                checked = isSoundEnabled,
                focusRequester = firstItemFocusRequester,
                onReturnLeft = onReturnLeft,
                onToggle = { enabled ->
                    soundManager.setSoundEnabled(enabled)
                    controller.setNavigationSounds(enabled)
                    isSoundEnabled = enabled
                    if (enabled) {
                        soundManager.playSelectSound()
                    }
                }
            )
        }

        item {
            VideoAudioActionCard(
                title = "Sortie audio principale",
                description = "Diffusion sonore configurée vers vos haut-parleurs TV ou votre barre de son connectée en HDMI eARC / optique.",
                badgeText = "Auto (eARC)",
                badgeColor = Color(0x35FFFFFF),
                onReturnLeft = onReturnLeft
            )
        }

        item {
            VideoAudioActionCard(
                title = "Formats audio cinéma pris en charge",
                description = "Transmission numérique Passthrough pour Dolby Atmos, Dolby Digital Plus, DTS:X et PCM Multicanal 7.1.",
                badgeText = "Dolby Atmos / DTS",
                badgeColor = Color(0x28FFFFFF),
                onReturnLeft = onReturnLeft
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

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
    ) {
        item {
            VideoAudioActionCard(
                title = "Réglages d'affichage et son de la TV",
                description = "Ouvre directement le menu système officiel de la TV pour calibrer la résolution 4K Ultra HD, le format de couleur et la plage dynamique HDR10+ / Dolby Vision.",
                badgeText = "Ouvrir",
                badgeColor = Color(0xFF34C759),
                focusRequester = firstItemFocusRequester,
                onReturnLeft = onReturnLeft,
                onClick = {
                    soundManager.playSelectSound()
                    controller.openHdmiCecSettings()
                }
            )
        }

        item {
            VideoAudioActionCard(
                title = "Fréquence d'images dynamique (Match Frame Rate)",
                description = "Adapte automatiquement la fréquence de la dalle TV (24 Hz cinéma, 50 Hz TV européenne, 60 Hz) au flux vidéo diffusé.",
                badgeText = "Pris en charge",
                badgeColor = Color(0x28FFFFFF),
                onReturnLeft = onReturnLeft
            )
        }

        item {
            VideoAudioActionCard(
                title = "Mode Faible Latence Automatique (ALLM)",
                description = "Bascule automatiquement votre téléviseur en mode Jeu à très faible latence dès le lancement d'une application ou d'un jeu vidéo.",
                badgeText = "ALLM Actif",
                badgeColor = Color(0x28FFFFFF),
                onReturnLeft = onReturnLeft
            )
        }
    }
}

// =============================================================================
// COMPOSANTS DE CARTES HARMONISÉS
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
    val isFocused by interactionSource.collectIsFocusedAsState()

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
                            if (onClick != null) {
                                onClick()
                                true
                            } else false
                        }
                        else -> false
                    }
                } else false
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = onClick != null
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
                    .background(badgeColor)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badgeText,
                    color = Color.White,
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
    val isFocused by interactionSource.collectIsFocusedAsState()

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
