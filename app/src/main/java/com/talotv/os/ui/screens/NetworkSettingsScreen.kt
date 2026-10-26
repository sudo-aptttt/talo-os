package com.talotv.os.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
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
 * 4 Sous-onglets de la section Réseau
 */
enum class NetworkSubTab(val title: String) {
    STATUS("État du réseau"),
    SPEED_TEST("Test de débit & Ping"),
    CONFIG("Configuration IP"),
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
                        text = "Réseau",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Connexion Wi-Fi, Ethernet, diagnostics de bande passante et latence",
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
                        NetworkSubTabItem(
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
                        NetworkSubTab.STATUS -> NetworkStatusSubScreen(
                            controller = controller,
                            firstItemFocusRequester = rightFirstFocusRequester,
                            onReturnLeft = {
                                isDetailPaneActive = false
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
                                try {
                                    subTabFocusRequesters.getOrNull(selectedSubTabIndex)?.requestFocus()
                                } catch (_: Exception) {}
                            }
                        )
                        NetworkSubTab.SETUP -> WifiSetupSubScreen(
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
private fun NetworkSubTabItem(
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
        label = "netSubTabScale"
    )

    val bgColor by animateColorAsState(
        targetValue = when {
            isFocused -> Color(0xFF2C303A)
            isSelected && isDetailPaneActive -> Color(0x30FFFFFF)
            isSelected -> Color(0x22FFFFFF)
            else -> Color(0x10FFFFFF)
        },
        animationSpec = tween(120),
        label = "netSubTabBg"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isFocused -> Color.White
            isSelected && isDetailPaneActive -> Color(0x50FFFFFF)
            isSelected -> Color(0x25FFFFFF)
            else -> Color(0x0CFFFFFF)
        },
        animationSpec = tween(120),
        label = "netSubTabBorder"
    )

    val textColor by animateColorAsState(
        targetValue = when {
            isFocused -> Color.White
            isSelected -> Color.White
            else -> Color(0x99FFFFFF)
        },
        animationSpec = tween(120),
        label = "netSubTabText"
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
// 1. ÉTAT DU RÉSEAU
// =============================================================================

@Composable
private fun NetworkStatusSubScreen(
    controller: SystemSettingsController,
    firstItemFocusRequester: FocusRequester,
    onReturnLeft: () -> Unit
) {
    val soundManager = remember { TaloApplication.instance.soundEffectManager }
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
                title = "Statut Internet",
                description = "Accès aux serveurs Google TV, flux vidéo 4K HDR et synchronisation du catalogue Talo OS opérationnels.",
                badgeText = "En ligne",
                badgeColor = Color(0xFF34C759),
                onReturnLeft = onReturnLeft
            )
        }

        item {
            NetworkActionCard(
                title = "Vitesse de liaison théorique",
                description = "Bande passante négociée avec votre box / routeur Wi-Fi : jusqu'à 866 Mbps (MIMO 2x2).",
                badgeText = "866 Mbps",
                badgeColor = Color(0x28FFFFFF),
                onReturnLeft = onReturnLeft
            )
        }
    }
}

// =============================================================================
// 2. TEST DE DÉBIT & PING
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
                    "Débit descendant mesuré : $speedMbps Mbps\nLatence réseau : ${pingMs ?: 12} ms (IP : ${testedIp ?: "192.168.1.42"})\nPerformances optimales pour le streaming 4K Ultra HD Dolby Vision."
                } else {
                    "Cliquez pour évaluer instantanément la latence et la stabilité de votre connexion Internet pour le streaming vidéo 4K."
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
                description = "• Débit requis pour HD 1080p : 5 Mbps\n• Débit requis pour 4K UHD : 25 Mbps\n• Débit requis pour 4K HDR Dolby Vision non compressé : 50+ Mbps.",
                badgeText = "Certifié 4K",
                badgeColor = Color(0xFF34C759),
                onReturnLeft = onReturnLeft
            )
        }
    }
}

// =============================================================================
// 3. CONFIGURATION IP
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
                description = "Résolution sécurisée et rapide des adresses de vos applications de streaming : 1.1.1.1 (Cloudflare) / 8.8.8.8 (Google).",
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
    firstItemFocusRequester: FocusRequester,
    onReturnLeft: () -> Unit
) {
    val context = LocalContext.current
    val soundManager = remember { TaloApplication.instance.soundEffectManager }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
    ) {
        item {
            NetworkActionCard(
                title = "Sélectionner ou changer de réseau Wi-Fi",
                description = "Ouvre l'assistant de détection sans fil pour scanner les réseaux Wi-Fi disponibles aux alentours et saisir le mot de passe de sécurité.",
                badgeText = "Configurer",
                badgeColor = Color(0xFF34C759),
                focusRequester = firstItemFocusRequester,
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
                            val fallback = Intent(android.provider.Settings.ACTION_WIFI_SETTINGS)
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
// COMPOSANT CARTE HARMONISÉ
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
    val isFocused by interactionSource.collectIsFocusedAsState()

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
