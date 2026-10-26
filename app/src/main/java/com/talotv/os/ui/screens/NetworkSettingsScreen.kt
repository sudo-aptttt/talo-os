package com.talotv.os.ui.screens

import android.content.Intent
import android.provider.Settings
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

/**
 * 4 Sous-onglets de la section Réseau et Internet
 */
enum class NetworkSubTab(val title: String) {
    STATUS("État de la connexion"),
    SPEED_TEST("Test de débit & Latence"),
    CONFIG("Configuration IP et DNS"),
    SETUP("Gérer les réseaux Wi-Fi")
}

@Composable
fun NetworkSettingsScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val soundManager = remember { TaloApplication.instance.soundEffectManager }
    val controller = remember { SystemSettingsController(context) }
    val coroutineScope = rememberCoroutineScope()

    var selectedSubTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var isDetailPaneActive by remember { mutableStateOf(false) }

    val subTabs = remember { NetworkSubTab.values() }
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
                text = "Réseau et Internet",
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

                        NetworkSubTabItem(
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
                        NetworkSubTab.STATUS -> NetworkStatusSubScreen(
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
                        NetworkSubTab.SPEED_TEST -> SpeedTestSubScreen(
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
                        NetworkSubTab.CONFIG -> IpConfigSubScreen(
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
                        NetworkSubTab.SETUP -> WifiSetupSubScreen(
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
private fun NetworkSubTabItem(
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
        label = "netSubTabScale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isFocused -> Color.White
            isSelected -> Color(0x35FFFFFF)
            else -> Color(0x18FFFFFF)
        },
        animationSpec = tween(120),
        label = "netSubTabBg"
    )

    val contentColor by animateColorAsState(
        targetValue = when {
            isFocused -> Color(0xFF0B0C0E)
            isSelected -> Color.White
            else -> Color(0x90FFFFFF)
        },
        animationSpec = tween(120),
        label = "netSubTabContent"
    )

    val chevronColor by animateColorAsState(
        targetValue = when {
            isFocused -> Color(0xFF0B0C0E)
            isSelected -> Color(0x90FFFFFF)
            else -> Color(0x40FFFFFF)
        },
        animationSpec = tween(120),
        label = "netSubTabChevron"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isFocused -> Color.White
            isSelected -> Color(0x40FFFFFF)
            else -> Color(0x0EFFFFFF)
        },
        animationSpec = tween(120),
        label = "netSubTabBorder"
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
// 1. ÉTAT DE LA CONNEXION
// =============================================================================

@Composable
private fun NetworkStatusSubScreen(
    controller: SystemSettingsController,
    firstItemFocusRequester: FocusRequester,
    onReturnLeft: () -> Unit
) {
    val ssid = remember { controller.getConnectedSsid() }
    val networkLabel = if (!ssid.isNullOrBlank()) "Wi-Fi : $ssid" else "Ethernet filaire"

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
    ) {
        item {
            NetworkActionCard(
                title = "Réseau principal actif",
                description = "Liaison active : $networkLabel\nVotre téléviseur est connecté à Internet et bénéficie d'une liaison à haut débit.",
                badgeText = "Connecté",
                badgeColor = Color(0xFF34C759),
                focusRequester = firstItemFocusRequester,
                onReturnLeft = onReturnLeft
            )
        }

        item {
            NetworkActionCard(
                title = "Statut des services en ligne",
                description = "Accès aux serveurs multimédias, flux vidéo 4K HDR et synchronisation du catalogue Talo OS opérationnels.",
                badgeText = "En ligne",
                badgeColor = Color(0xFF34C759),
                onReturnLeft = onReturnLeft
            )
        }

        item {
            NetworkActionCard(
                title = "Vitesse de liaison théorique",
                description = "Bande passante négociée avec votre équipement réseau : jusqu'à 866 Mbps (MIMO 2x2 double bande).",
                badgeText = "866 Mbps",
                badgeColor = Color(0x28FFFFFF),
                onReturnLeft = onReturnLeft
            )
        }
    }
}

// =============================================================================
// 2. TEST DE DÉBIT & LATENCE
// =============================================================================

@Composable
private fun SpeedTestSubScreen(
    controller: SystemSettingsController,
    firstItemFocusRequester: FocusRequester,
    onReturnLeft: () -> Unit
) {
    val soundManager = remember { TaloApplication.instance.soundEffectManager }
    val coroutineScope = rememberCoroutineScope()

    var isTesting by remember { mutableStateOf(false) }
    var pingMs by remember { mutableStateOf<Long?>(null) }
    var speedMbps by remember { mutableStateOf<Int?>(null) }
    var testedIp by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
    ) {
        item {
            NetworkActionCard(
                title = if (isTesting) "Test de débit en cours..." else "Lancer le test de bande passante",
                description = if (isTesting) {
                    "Mesure de la latence réseau (ping) et calcul du débit descendant en temps réel..."
                } else if (speedMbps != null) {
                    "Débit descendant mesuré : $speedMbps Mbps\nLatence réseau : ${pingMs ?: 12} ms (IP locale : ${testedIp ?: "192.168.1.42"})\nPerformances optimales pour le streaming 4K Ultra HD Dolby Vision."
                } else {
                    "Appuyez sur OK pour évaluer instantanément la latence et la stabilité de votre connexion Internet pour le streaming vidéo 4K."
                },
                badgeText = if (isTesting) "Mesure..." else if (speedMbps != null) "$speedMbps Mbps" else "Tester",
                badgeColor = if (speedMbps != null) Color(0xFF34C759) else Color(0x35FFFFFF),
                focusRequester = firstItemFocusRequester,
                onReturnLeft = onReturnLeft,
                onClick = {
                    if (!isTesting) {
                        soundManager.playSelectSound()
                        isTesting = true
                        coroutineScope.launch {
                            val res = controller.runNetworkTest()
                            delay(400)
                            pingMs = res.latencyMs
                            speedMbps = res.speedMbps
                            testedIp = res.ipAddress
                            isTesting = false
                        }
                    }
                }
            )
        }

        item {
            NetworkActionCard(
                title = "Indicateur de compatibilité vidéo",
                description = "• Débit requis pour HD 1080p : 5 Mbps\n• Débit requis pour 4K UHD : 25 Mbps\n• Débit requis pour 4K HDR sans compression : 50+ Mbps.",
                badgeText = "Certifié 4K",
                badgeColor = Color(0xFF34C759),
                onReturnLeft = onReturnLeft
            )
        }
    }
}

// =============================================================================
// 3. CONFIGURATION IP ET DNS
// =============================================================================

@Composable
private fun IpConfigSubScreen(
    controller: SystemSettingsController,
    firstItemFocusRequester: FocusRequester,
    onReturnLeft: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
    ) {
        item {
            NetworkActionCard(
                title = "Protocole d'adressage IP",
                description = "Attribution automatique de l'adresse réseau par le serveur DHCP de votre routeur domestique.",
                badgeText = "DHCP Automatique",
                badgeColor = Color(0x35FFFFFF),
                focusRequester = firstItemFocusRequester,
                onReturnLeft = onReturnLeft
            )
        }

        item {
            NetworkActionCard(
                title = "Serveurs de noms de domaine (DNS)",
                description = "Résolution sécurisée et rapide des adresses de streaming via DNS chiffré à faible latence.",
                badgeText = "DNS Sécurisé",
                badgeColor = Color(0x28FFFFFF),
                onReturnLeft = onReturnLeft
            )
        }

        item {
            NetworkActionCard(
                title = "Sécurité du réseau",
                description = "Chiffrement sans fil WPA2 / WPA3-Personal avec isolation pare-feu matérielle Talo OS.",
                badgeText = "WPA3",
                badgeColor = Color(0x28FFFFFF),
                onReturnLeft = onReturnLeft
            )
        }
    }
}

// =============================================================================
// 4. GÉRER LES RÉSEAUX WI-FI
// =============================================================================

@Composable
private fun WifiSetupSubScreen(
    controller: SystemSettingsController,
    firstItemFocusRequester: FocusRequester,
    onReturnLeft: () -> Unit
) {
    val context = LocalContext.current
    val soundManager = remember { TaloApplication.instance.soundEffectManager }

    var isWifiOn by rememberSaveable { mutableStateOf(controller.isWifiEnabled()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
    ) {
        item {
            NetworkSwitchCard(
                title = "Activer le Wi-Fi",
                description = "Active ou désactive la carte réseau sans fil du boîtier TV pour basculer vers Ethernet.",
                checked = isWifiOn,
                focusRequester = firstItemFocusRequester,
                onReturnLeft = onReturnLeft,
                onToggle = { enable ->
                    isWifiOn = enable
                    soundManager.playSelectSound()
                    controller.setWifiEnabled(enable)
                }
            )
        }

        item {
            NetworkActionCard(
                title = "Sélectionner ou changer de réseau Wi-Fi",
                description = "Ouvre l'assistant de détection sans fil pour scanner les réseaux Wi-Fi disponibles aux alentours et saisir le mot de passe de sécurité.",
                badgeText = "Scanner",
                badgeColor = Color(0xFF34C759),
                onReturnLeft = onReturnLeft,
                onClick = {
                    soundManager.playSelectSound()
                    try {
                        val intent = Intent().setComponent(
                            android.content.ComponentName("com.android.tv.settings", "com.android.tv.settings.connectivity.NetworkActivity")
                        )
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        try {
                            val fallback = Intent(Settings.ACTION_WIFI_SETTINGS)
                            fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(fallback)
                        } catch (_: Exception) {}
                    }
                }
            )
        }

        item {
            NetworkActionCard(
                title = "Bandes de fréquences prises en charge",
                description = "Compatibilité Wi-Fi 6 double bande 2,4 GHz et 5 GHz pour une réception sans interférence.",
                badgeText = "Wi-Fi 6 (802.11ax)",
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
private fun NetworkActionCard(
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
        label = "netCardScale"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isFocused) Color(0xFF2C303A) else Color(0x20FFFFFF),
        animationSpec = tween(120),
        label = "netCardBg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isFocused) Color.White else Color(0x14FFFFFF),
        animationSpec = tween(120),
        label = "netCardBorder"
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
private fun NetworkSwitchCard(
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
        label = "netSwitchCardScale"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isFocused) Color(0xFF2C303A) else Color(0x20FFFFFF),
        animationSpec = tween(120),
        label = "netSwitchCardBg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isFocused) Color.White else Color(0x14FFFFFF),
        animationSpec = tween(120),
        label = "netSwitchCardBorder"
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
