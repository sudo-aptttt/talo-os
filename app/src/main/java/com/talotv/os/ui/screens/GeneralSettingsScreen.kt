package com.talotv.os.ui.screens

import android.content.Intent
import android.provider.Settings
import android.widget.Toast
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
import com.talotv.os.core.system.SystemSettingsController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * 6 Sous-onglets de la section Général
 */
enum class GeneralSubTab(val title: String) {
    TIME_DATE("Date et heure"),
    LANGUAGE("Langue et saisie"),
    DEVICE_NAME("Nom de l'appareil"),
    SLEEP("Veille et alimentation"),
    ACCESSIBILITY("Accessibilité"),
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
    val coroutineScope = rememberCoroutineScope()

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
                .padding(horizontal = 64.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Titre supérieur centré
            Text(
                text = "Général",
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

                        GeneralSubTabItem(
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
                        GeneralSubTab.TIME_DATE -> TimeDateSubScreen(
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
                        GeneralSubTab.LANGUAGE -> LanguageSubScreen(
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
                        GeneralSubTab.DEVICE_NAME -> DeviceNameSubScreen(
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
                        GeneralSubTab.SLEEP -> SleepSubScreen(
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
                        GeneralSubTab.ACCESSIBILITY -> AccessibilitySubScreen(
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
                        GeneralSubTab.SOUNDS -> NavigationSoundsSubScreen(
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
private fun GeneralSubTabItem(
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
        label = "genSubTabScale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isFocused -> Color.White
            isSelected -> Color(0x35FFFFFF)
            else -> Color(0x18FFFFFF)
        },
        animationSpec = tween(120),
        label = "genSubTabBg"
    )

    val contentColor by animateColorAsState(
        targetValue = when {
            isFocused -> Color(0xFF0B0C0E)
            isSelected -> Color.White
            else -> Color(0x90FFFFFF)
        },
        animationSpec = tween(120),
        label = "genSubTabContent"
    )

    val chevronColor by animateColorAsState(
        targetValue = when {
            isFocused -> Color(0xFF0B0C0E)
            isSelected -> Color(0x90FFFFFF)
            else -> Color(0x40FFFFFF)
        },
        animationSpec = tween(120),
        label = "genSubTabChevron"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isFocused -> Color.White
            isSelected -> Color(0x40FFFFFF)
            else -> Color(0x0EFFFFFF)
        },
        animationSpec = tween(120),
        label = "genSubTabBorder"
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
                description = "Bascule entre l'affichage en 24 heures (ex: 20:30) et 12 heures (ex: 08:30 PM).",
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
                description = "Région : $timeZone ($timeZoneOffset)\nLe fuseau horaire détermine les heures de diffusion de vos programmes en direct.",
                badgeText = timeZoneOffset,
                badgeColor = Color(0x28FFFFFF),
                onReturnLeft = onReturnLeft
            )
        }

        item {
            GeneralActionCard(
                title = "Synchronisation automatique",
                description = "L'horloge système est calibrée automatiquement en continu via la connexion Internet.",
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
    val context = LocalContext.current
    val soundManager = remember { TaloApplication.instance.soundEffectManager }
    var currentName by remember { mutableStateOf(controller.getDeviceName()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
    ) {
        item {
            GeneralActionCard(
                title = "Nom du téléviseur",
                description = "Nom public affiché sur votre réseau local, lors de la diffusion Cast et pour les connexions Bluetooth.",
                badgeText = currentName,
                badgeColor = Color(0xFF34C759),
                focusRequester = firstItemFocusRequester,
                onReturnLeft = onReturnLeft,
                onClick = {
                    soundManager.playSelectSound()
                    val newName = if (currentName.contains("Salon")) "Talo TV Chambre" else "Talo TV Salon"
                    controller.setDeviceName(newName)
                    currentName = newName
                    Toast.makeText(context, "Nom changé en : $newName", Toast.LENGTH_SHORT).show()
                }
            )
        }

        item {
            GeneralActionCard(
                title = "Protocole de diffusion Cast",
                description = "Diffusez instantanément vos vidéos, musiques et photos depuis votre smartphone ou tablette vers ce téléviseur.",
                badgeText = "Google Cast Actif",
                badgeColor = Color(0x35FFFFFF),
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
    val context = LocalContext.current
    val soundManager = remember { TaloApplication.instance.soundEffectManager }

    val sleepIntervals = listOf("15 minutes", "30 minutes", "1 heure", "3 heures", "Jamais")
    var selectedSleepIndex by rememberSaveable { mutableIntStateOf(1) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
    ) {
        item {
            GeneralActionCard(
                title = "Mise en veille automatique",
                description = "Délai d'inactivité avant extinction de l'écran et mise en veille basse consommation du processeur.",
                badgeText = sleepIntervals[selectedSleepIndex],
                badgeColor = Color(0xFF34C759),
                focusRequester = firstItemFocusRequester,
                onReturnLeft = onReturnLeft,
                onClick = {
                    soundManager.playSelectSound()
                    selectedSleepIndex = (selectedSleepIndex + 1) % sleepIntervals.size
                }
            )
        }

        item {
            GeneralActionCard(
                title = "Économiseur d'écran (Mode Ambiant)",
                description = "Active le diaporama de paysages ou d'œuvres d'art haute définition pour préserver votre dalle TV pendant les pauses.",
                badgeText = "Ambiant 4K",
                badgeColor = Color(0x35FFFFFF),
                onReturnLeft = onReturnLeft,
                onClick = {
                    soundManager.playSelectSound()
                    try {
                        val intent = Intent(Settings.ACTION_DREAM_SETTINGS)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    } catch (_: Exception) {}
                }
            )
        }

        item {
            GeneralActionCard(
                title = "Mettre en veille maintenant",
                description = "Éteindre l'affichage et basculer instantanément le boîtier TV en veille éco-énergétique.",
                badgeText = "Veille",
                badgeColor = Color(0x35FFFFFF),
                onReturnLeft = onReturnLeft,
                onClick = {
                    soundManager.playSelectSound()
                    controller.triggerSleep()
                }
            )
        }
    }
}

// =============================================================================
// 5. ACCESSIBILITÉ
// =============================================================================

@Composable
private fun AccessibilitySubScreen(
    controller: SystemSettingsController,
    firstItemFocusRequester: FocusRequester,
    onReturnLeft: () -> Unit
) {
    val context = LocalContext.current
    val soundManager = remember { TaloApplication.instance.soundEffectManager }
    var highContrastEnabled by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
    ) {
        item {
            GeneralSwitchCard(
                title = "Texte à contraste élevé",
                description = "Accentue la netteté et les contours de l'ensemble des polices de l'interface pour une lecture confortable de loin.",
                checked = highContrastEnabled,
                focusRequester = firstItemFocusRequester,
                onReturnLeft = onReturnLeft,
                onToggle = { enable ->
                    highContrastEnabled = enable
                    soundManager.playSelectSound()
                }
            )
        }

        item {
            GeneralActionCard(
                title = "Sous-titres & Audiodescription",
                description = "Activer et personnaliser l'affichage des sous-titres (taille du texte, arrière-plan contrasté, langue préférée).",
                badgeText = "Sous-titres",
                badgeColor = Color(0xFF34C759),
                onReturnLeft = onReturnLeft,
                onClick = {
                    soundManager.playSelectSound()
                    try {
                        val intent = Intent(Settings.ACTION_CAPTIONING_SETTINGS)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    } catch (_: Exception) {}
                }
            )
        }

        item {
            GeneralActionCard(
                title = "Services d'accessibilité Android TV",
                description = "Gérer les lecteurs d'écran (TalkBack) et outils d'assistance installés sur l'appareil.",
                badgeText = "Système",
                badgeColor = Color(0x35FFFFFF),
                onReturnLeft = onReturnLeft,
                onClick = {
                    soundManager.playSelectSound()
                    try {
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    } catch (_: Exception) {}
                }
            )
        }
    }
}

// =============================================================================
// 6. SONS DE NAVIGATION
// =============================================================================

@Composable
private fun NavigationSoundsSubScreen(
    controller: SystemSettingsController,
    firstItemFocusRequester: FocusRequester,
    onReturnLeft: () -> Unit
) {
    val soundManager = remember { TaloApplication.instance.soundEffectManager }
    var navSoundsEnabled by rememberSaveable { mutableStateOf(soundManager.isSoundEnabled()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
    ) {
        item {
            GeneralSwitchCard(
                title = "Bruits de navigation de l'interface",
                description = "Retour sonore discret lors du déplacement du curseur D-Pad et de la sélection des éléments.",
                checked = navSoundsEnabled,
                focusRequester = firstItemFocusRequester,
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
            GeneralActionCard(
                title = "Thème acoustique",
                description = "Sons discrets conçus pour le confort d'écoute dans le salon.",
                badgeText = "Talo Frosted Glass",
                badgeColor = Color(0x28FFFFFF),
                onReturnLeft = onReturnLeft
            )
        }
    }
}

// =============================================================================
// COMPOSANTS CARTE HARMONISÉS
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
    var isFocused by remember { mutableStateOf(false) }

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
private fun GeneralSwitchCard(
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
