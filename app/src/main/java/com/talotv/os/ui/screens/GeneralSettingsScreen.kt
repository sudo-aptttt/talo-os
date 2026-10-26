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
import androidx.compose.foundation.lazy.items
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
import com.talotv.os.core.system.SystemSettingsController
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * 5 Sous-onglets de la section Général
 */
enum class GeneralSubTab(val title: String) {
    TIME_DATE("Date et heure"),
    LANGUAGE("Langue et saisie"),
    DEVICE_NAME("Nom de l'appareil"),
    SLEEP("Veille et alimentation"),
    SOUNDS("Sons de navigation")
}

@Composable
fun GeneralSettingsScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val soundManager = remember { TaloApplication.instance.soundEffectManager }
    val controller = remember { SystemSettingsController(context) }

    var selectedSubTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var isDetailPaneActive by remember { mutableStateOf(false) }

    val subTabs = remember { GeneralSubTab.values() }
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
                        text = "Général",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Configuration de la date, langue, veille et comportement de Talo OS",
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
                        GeneralSubTabItem(
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
                        GeneralSubTab.TIME_DATE -> TimeDateSubScreen(
                            controller = controller,
                            firstItemFocusRequester = rightFirstFocusRequester,
                            onReturnLeft = {
                                isDetailPaneActive = false
                                try {
                                    subTabFocusRequesters.getOrNull(selectedSubTabIndex)?.requestFocus()
                                } catch (_: Exception) {}
                            }
                        )
                        GeneralSubTab.LANGUAGE -> LanguageSubScreen(
                            controller = controller,
                            firstItemFocusRequester = rightFirstFocusRequester,
                            onReturnLeft = {
                                isDetailPaneActive = false
                                try {
                                    subTabFocusRequesters.getOrNull(selectedSubTabIndex)?.requestFocus()
                                } catch (_: Exception) {}
                            }
                        )
                        GeneralSubTab.DEVICE_NAME -> DeviceNameSubScreen(
                            controller = controller,
                            firstItemFocusRequester = rightFirstFocusRequester,
                            onReturnLeft = {
                                isDetailPaneActive = false
                                try {
                                    subTabFocusRequesters.getOrNull(selectedSubTabIndex)?.requestFocus()
                                } catch (_: Exception) {}
                            }
                        )
                        GeneralSubTab.SLEEP -> SleepSubScreen(
                            controller = controller,
                            firstItemFocusRequester = rightFirstFocusRequester,
                            onReturnLeft = {
                                isDetailPaneActive = false
                                try {
                                    subTabFocusRequesters.getOrNull(selectedSubTabIndex)?.requestFocus()
                                } catch (_: Exception) {}
                            }
                        )
                        GeneralSubTab.SOUNDS -> NavigationSoundsSubScreen(
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
private fun GeneralSubTabItem(
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
        label = "genSubTabScale"
    )

    val bgColor by animateColorAsState(
        targetValue = when {
            isFocused -> Color(0xFF2C303A)
            isSelected && isDetailPaneActive -> Color(0x30FFFFFF)
            isSelected -> Color(0x22FFFFFF)
            else -> Color(0x10FFFFFF)
        },
        animationSpec = tween(120),
        label = "genSubTabBg"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isFocused -> Color.White
            isSelected && isDetailPaneActive -> Color(0x50FFFFFF)
            isSelected -> Color(0x25FFFFFF)
            else -> Color(0x0CFFFFFF)
        },
        animationSpec = tween(120),
        label = "genSubTabBorder"
    )

    val textColor by animateColorAsState(
        targetValue = when {
            isFocused -> Color.White
            isSelected -> Color.White
            else -> Color(0x99FFFFFF)
        },
        animationSpec = tween(120),
        label = "genSubTabText"
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
// 1. DATE ET HEURE
// =============================================================================

@Composable
private fun TimeDateSubScreen(
    controller: SystemSettingsController,
    firstItemFocusRequester: FocusRequester,
    onReturnLeft: () -> Unit
) {
    val soundManager = remember { TaloApplication.instance.soundEffectManager }
    var timeFormat by remember { mutableStateOf(controller.getTimeFormat()) }
    val timeZone = remember { TimeZone.getDefault().id }
    val timeZoneOffset = remember {
        val offsetMs = TimeZone.getDefault().rawOffset
        val hours = offsetMs / (1000 * 60 * 60)
        String.format(Locale.US, "UTC%+03d:00", hours)
    }

    var currentTimeString by remember {
        mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()))
    }

    LaunchedEffect(timeFormat) {
        val pattern = if (timeFormat == "12h") "hh:mm a" else "HH:mm"
        currentTimeString = SimpleDateFormat(pattern, Locale.getDefault()).format(Date())
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
    ) {
        item {
            GeneralActionCard(
                title = "Format de l'heure",
                description = "Bascule entre l'affichage en 24 heures (ex: 20:30) et 12 heures (ex: 08:30 PM). Ce réglage s'applique instantanément à toute l'interface Talo OS.",
                badgeText = if (timeFormat == "24h") "24 Heures" else "12 Heures",
                badgeColor = Color(0xFF34C759),
                focusRequester = firstItemFocusRequester,
                onReturnLeft = onReturnLeft,
                onClick = {
                    soundManager.playSelectSound()
                    val newFormat = if (timeFormat == "24h") "12h" else "24h"
                    controller.setTimeFormat(newFormat)
                    timeFormat = newFormat
                }
            )
        }

        item {
            GeneralActionCard(
                title = "Heure actuelle",
                description = "Heure précise synchronisée avec les serveurs d'horloge de référence NTP.",
                badgeText = currentTimeString,
                badgeColor = Color(0x35FFFFFF),
                onReturnLeft = onReturnLeft
            )
        }

        item {
            GeneralActionCard(
                title = "Fuseau horaire",
                description = "Région : $timeZone ($timeZoneOffset)\nLe fuseau horaire détermine les heures de diffusion de vos programmes en direct et l'heure du coucher.",
                badgeText = timeZoneOffset,
                badgeColor = Color(0x28FFFFFF),
                onReturnLeft = onReturnLeft
            )
        }

        item {
            GeneralActionCard(
                title = "Synchronisation automatique",
                description = "L'horloge système est calibrée automatiquement en continu via la connexion Internet de votre fournisseur d'accès.",
                badgeText = "NTP Actif",
                badgeColor = Color(0x28FFFFFF),
                onReturnLeft = onReturnLeft
            )
        }
    }
}

// =============================================================================
// 2. LANGUE ET SAISIE
// =============================================================================

@Composable
private fun LanguageSubScreen(
    controller: SystemSettingsController,
    firstItemFocusRequester: FocusRequester,
    onReturnLeft: () -> Unit
) {
    val soundManager = remember { TaloApplication.instance.soundEffectManager }
    val currentLocale = remember { Locale.getDefault() }
    val langDisplayName = remember { currentLocale.getDisplayName(currentLocale).replaceFirstChar { it.uppercase() } }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
    ) {
        item {
            GeneralActionCard(
                title = "Langue du système",
                description = "Langue principale utilisée pour tous les menus, sous-titres automatiques et synthèses vocales de Talo OS.",
                badgeText = langDisplayName,
                badgeColor = Color(0xFF34C759),
                focusRequester = firstItemFocusRequester,
                onReturnLeft = onReturnLeft,
                onClick = {
                    soundManager.playSelectSound()
                }
            )
        }

        item {
            GeneralActionCard(
                title = "Région et normes locales",
                description = "Format métrique, séparateur décimal et calendrier adaptés pour la région : ${currentLocale.displayCountry.ifBlank { "France" }}.",
                badgeText = currentLocale.country.ifBlank { "FR" },
                badgeColor = Color(0x28FFFFFF),
                onReturnLeft = onReturnLeft
            )
        }

        item {
            GeneralActionCard(
                title = "Clavier virtuel TV",
                description = "Clavier virtuel optimisé pour la navigation à la télécommande avec saisie prédictive et dictée vocale.",
                badgeText = "Gboard TV",
                badgeColor = Color(0x35FFFFFF),
                onReturnLeft = onReturnLeft
            )
        }
    }
}

// =============================================================================
// 3. NOM DE L'APPAREIL
// =============================================================================

@Composable
private fun DeviceNameSubScreen(
    controller: SystemSettingsController,
    firstItemFocusRequester: FocusRequester,
    onReturnLeft: () -> Unit
) {
    val soundManager = remember { TaloApplication.instance.soundEffectManager }
    val deviceName = remember { controller.getDeviceName() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
    ) {
        item {
            GeneralActionCard(
                title = "Nom de diffusion du téléviseur",
                description = "Ce nom identifie votre appareil lors des partages d'écran Google Cast, des transferts Bluetooth et des connexions réseau local.",
                badgeText = deviceName,
                badgeColor = Color(0xFF34C759),
                focusRequester = firstItemFocusRequester,
                onReturnLeft = onReturnLeft
            )
        }

        item {
            GeneralActionCard(
                title = "Emplacement de la pièce",
                description = "Assigner ce téléviseur à un espace de la maison pour simplifier l'accès depuis l'application maison connectée.",
                badgeText = "Salon",
                badgeColor = Color(0x28FFFFFF),
                onReturnLeft = onReturnLeft
            )
        }
    }
}

// =============================================================================
// 4. VEILLE ET ALIMENTATION
// =============================================================================

@Composable
private fun SleepSubScreen(
    controller: SystemSettingsController,
    firstItemFocusRequester: FocusRequester,
    onReturnLeft: () -> Unit
) {
    val soundManager = remember { TaloApplication.instance.soundEffectManager }
    val timeouts = remember { listOf(15, 30, 60, 120) }
    var currentTimeoutIndex by rememberSaveable { mutableIntStateOf(1) } // 30 min par défaut

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
    ) {
        item {
            val minutes = timeouts[currentTimeoutIndex]
            val label = if (minutes >= 60) "${minutes / 60} heure${if (minutes > 60) "s" else ""}" else "$minutes minutes"
            GeneralActionCard(
                title = "Délai avant mise en veille",
                description = "Met automatiquement en veille votre écran et vos appareils connectés après une période prolongée d'inactivité. Cliquez pour changer le délai.",
                badgeText = label,
                badgeColor = Color(0xFF34C759),
                focusRequester = firstItemFocusRequester,
                onReturnLeft = onReturnLeft,
                onClick = {
                    soundManager.playSelectSound()
                    currentTimeoutIndex = (currentTimeoutIndex + 1) % timeouts.size
                }
            )
        }

        item {
            GeneralActionCard(
                title = "Mode Écran de veille (Ambient)",
                description = "Affiche de superbes photographies de paysages en 4K Ultra HD avec horloge discrète avant la mise en veille complète.",
                badgeText = "Activé",
                badgeColor = Color(0x28FFFFFF),
                onReturnLeft = onReturnLeft
            )
        }

        item {
            GeneralActionCard(
                title = "Reprise instantanée (Quick Start)",
                description = "Maintient les composants réseau en état de basse consommation pour un allumage immédiat en moins de 1 seconde.",
                badgeText = "Prêt",
                badgeColor = Color(0x28FFFFFF),
                onReturnLeft = onReturnLeft
            )
        }
    }
}

// =============================================================================
// 5. SONS DE NAVIGATION
// =============================================================================

@Composable
private fun NavigationSoundsSubScreen(
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
            GeneralSwitchCard(
                title = "Bruitages de navigation Talo OS",
                description = "Émet un retour audio subtil et doux lors de chaque déplacement du curseur à la télécommande et de chaque sélection de contenu.",
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
            GeneralActionCard(
                title = "Profil acoustique",
                description = "Tonalité feutrée spécialement étalonnée pour ne pas saturer les haut-parleurs du téléviseur lors des défilements rapides.",
                badgeText = "Feutré 48 kHz",
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
private fun GeneralActionCard(
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
        label = "genCardScale"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isFocused) Color(0xFF2C303A) else Color(0x20FFFFFF),
        animationSpec = tween(120),
        label = "genCardBg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isFocused) Color.White else Color(0x14FFFFFF),
        animationSpec = tween(120),
        label = "genCardBorder"
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
private fun GeneralSwitchCard(
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
        label = "switchCardScale"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isFocused) Color(0xFF2C303A) else Color(0x20FFFFFF),
        animationSpec = tween(120),
        label = "switchCardBg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isFocused) Color.White else Color(0x14FFFFFF),
        animationSpec = tween(120),
        label = "switchCardBorder"
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

        // Interrupteur Switch épuré
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
