package com.talotv.os.ui.screens

import android.view.KeyEvent as AndroidKeyEvent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.drawscope.Stroke
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

/**
 * 5 Sous-onglets de la section Télécommandes et appareils :
 * 1. Télécommandes (Appairage, état télécommande vocale, niveau pile, micrologiciel)
 * 2. Accessoires Bluetooth (Bascule BT, recherche, liste périphériques appairés, oublier appareil)
 * 3. Manettes de jeu (Détection manettes Xbox / PlayStation / Switch, banc d'essai interactif touches/sticks, test vibration)
 * 4. Contrôle HDMI-CEC (One Touch Play, extinction synchronisée, volume TV eARC, menu avancé)
 * 5. Retrouver la télécommande (Bipeur Google TV Streamer 4K, animation radar, arrêt sonore)
 */
enum class RemotesSubTab(val title: String) {
    REMOTES("Télécommandes"),
    BLUETOOTH("Accessoires Bluetooth"),
    GAMEPADS("Manettes de jeu"),
    HDMI_CEC("Contrôle HDMI-CEC"),
    FIND_REMOTE("Retrouver la télécommande")
}

@Composable
fun RemotesSettingsScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val soundManager = remember { TaloApplication.instance.soundEffectManager }
    val controller = remember { SystemSettingsController(context) }
    val coroutineScope = rememberCoroutineScope()

    var selectedSubTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var isDetailPaneActive by remember { mutableStateOf(false) }

    val subTabs = remember { RemotesSubTab.values() }
    val subTabFocusRequesters = remember { List(subTabs.size) { FocusRequester() } }
    val rightFirstFocusRequester = remember { FocusRequester() }

    // Dialogue de confirmation pour dissocier un périphérique
    var deviceToUnpair by remember { mutableStateOf<SystemSettingsController.BondedBluetoothDevice?>(null) }

    // Arrêt de sécurité du bipeur si l'écran est quitté
    DisposableEffect(Unit) {
        onDispose {
            controller.stopFindMyRemote()
        }
    }

    // Interception de la touche Retour
    BackHandler {
        when {
            deviceToUnpair != null -> deviceToUnpair = null
            isDetailPaneActive -> {
                isDetailPaneActive = false
                try {
                    subTabFocusRequesters.getOrNull(selectedSubTabIndex)?.requestFocus()
                } catch (_: Exception) {}
            }
            else -> onClose()
        }
    }

    // Mise au point initiale sur le premier sous-onglet
    LaunchedEffect(Unit) {
        delay(60)
        try {
            subTabFocusRequesters.getOrNull(0)?.requestFocus()
        } catch (_: Exception) {}
    }

    val isAnyDialogOpen = deviceToUnpair != null

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
                text = "Télécommandes et appareils",
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

                        RemotesSubTabItem(
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
                        RemotesSubTab.REMOTES -> RemotesSubScreen(
                            controller = controller,
                            firstItemFocusRequester = rightFirstFocusRequester,
                            onReturnLeft = {
                                isDetailPaneActive = false
                                soundManager.playFocusSound()
                                subTabFocusRequesters.getOrNull(selectedSubTabIndex)?.requestFocus()
                            }
                        )

                        RemotesSubTab.BLUETOOTH -> BluetoothSubScreen(
                            controller = controller,
                            firstItemFocusRequester = rightFirstFocusRequester,
                            onRequestUnpair = { device -> deviceToUnpair = device },
                            onReturnLeft = {
                                isDetailPaneActive = false
                                soundManager.playFocusSound()
                                subTabFocusRequesters.getOrNull(selectedSubTabIndex)?.requestFocus()
                            }
                        )

                        RemotesSubTab.GAMEPADS -> GamepadsSubScreen(
                            controller = controller,
                            firstItemFocusRequester = rightFirstFocusRequester,
                            onReturnLeft = {
                                isDetailPaneActive = false
                                soundManager.playFocusSound()
                                subTabFocusRequesters.getOrNull(selectedSubTabIndex)?.requestFocus()
                            }
                        )

                        RemotesSubTab.HDMI_CEC -> HdmiCecSubScreen(
                            controller = controller,
                            firstItemFocusRequester = rightFirstFocusRequester,
                            onReturnLeft = {
                                isDetailPaneActive = false
                                soundManager.playFocusSound()
                                subTabFocusRequesters.getOrNull(selectedSubTabIndex)?.requestFocus()
                            }
                        )

                        RemotesSubTab.FIND_REMOTE -> FindRemoteSubScreen(
                            controller = controller,
                            firstItemFocusRequester = rightFirstFocusRequester,
                            onReturnLeft = {
                                isDetailPaneActive = false
                                soundManager.playFocusSound()
                                subTabFocusRequesters.getOrNull(selectedSubTabIndex)?.requestFocus()
                            }
                        )
                    }
                }
            }
        }

        // Dialogue de confirmation pour dissocier un appareil Bluetooth
        deviceToUnpair?.let { device ->
            RemotesConfirmationDialog(
                title = "Oublier l'appareil ?",
                message = "Voulez-vous dissocier \"${device.name}\" de Talo TV ? Pour le réutiliser, vous devrez le reconnecter en Bluetooth.",
                confirmText = "Oublier",
                isDestructive = true,
                onConfirm = {
                    val address = device.address
                    deviceToUnpair = null
                    controller.unpairDevice(address)
                },
                onDismiss = {
                    deviceToUnpair = null
                }
            )
        }
    }
}

// =============================================================================
// COMPOSANT SOUS-ONGLET (NAVIGATION GAUCHE)
// =============================================================================

@Composable
private fun RemotesSubTabItem(
    title: String,
    isSelected: Boolean,
    focusRequester: FocusRequester,
    onFocused: () -> Unit,
    onEnterRight: () -> Unit,
    onExit: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.03f else 1.0f,
        animationSpec = tween(120),
        label = "subTabScale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isFocused -> Color.White
            isSelected -> Color(0x30FFFFFF)
            else -> Color(0x18FFFFFF)
        },
        animationSpec = tween(120),
        label = "subTabBg"
    )

    val contentColor by animateColorAsState(
        targetValue = when {
            isFocused -> Color(0xFF0B0C0E)
            isSelected -> Color.White
            else -> Color(0xFF8E8E93)
        },
        animationSpec = tween(120),
        label = "subTabContent"
    )

    val chevronColor by animateColorAsState(
        targetValue = when {
            isFocused -> Color(0xFF0B0C0E)
            isSelected -> Color(0x90FFFFFF)
            else -> Color(0x50FFFFFF)
        },
        animationSpec = tween(120),
        label = "subTabChevron"
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
                BorderStroke(
                    width = 1.dp,
                    color = if (isFocused) Color.White else Color(0x14FFFFFF)
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .focusRequester(focusRequester)
            .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                    onFocused()
                }
            }
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
// SOUS-ONGLET 1 : TÉLÉCOMMANDES
// =============================================================================

@Composable
private fun RemotesSubScreen(
    controller: SystemSettingsController,
    firstItemFocusRequester: FocusRequester,
    onReturnLeft: () -> Unit
) {
    val soundManager = remember { TaloApplication.instance.soundEffectManager }
    val remoteInfo = remember { controller.getVoiceRemoteInfo() }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
    ) {
        item {
            RemotesActionCard(
                title = "Associer une télécommande",
                description = "Maintenez enfoncées les touches Accueil et Retour de votre télécommande pour activer le mode d'appairage Bluetooth.",
                badgeText = "Associer",
                badgeColor = Color(0xFF34C759),
                focusRequester = firstItemFocusRequester,
                onReturnLeft = onReturnLeft,
                onClick = {
                    soundManager.playSelectSound()
                    controller.openBluetoothRemotePairing()
                }
            )
        }

        item {
            RemotesActionCard(
                title = remoteInfo.name,
                description = "Statut : ${if (remoteInfo.isConnected) "Connectée" else "Déconnectée"}\nBatterie : Piles AAA (${remoteInfo.batteryLevel}%)\nLiaison : ${remoteInfo.connectionType} • Signal : ${remoteInfo.signalStrength}",
                badgeText = "${remoteInfo.batteryLevel}%",
                badgeColor = Color(0x35FFFFFF),
                onReturnLeft = onReturnLeft
            )
        }

        item {
            RemotesActionCard(
                title = "Micrologiciel de la télécommande",
                description = "Version installée : ${remoteInfo.firmwareVersion}\nVotre télécommande vocale Google TV bénéficie des dernières optimisations de latence et de consommation énergétique.",
                badgeText = "À jour",
                badgeColor = Color(0x28FFFFFF),
                onReturnLeft = onReturnLeft
            )
        }

        item {
            RemotesActionCard(
                title = "Bouton raccourci personnalisable",
                description = "La touche étoile / favori de votre télécommande Google TV Streamer 4K est configurée pour ouvrir instantanément l'accueil Talo OS.",
                badgeText = "Accueil Talo",
                badgeColor = Color(0x28FFFFFF),
                onReturnLeft = onReturnLeft
            )
        }
    }
}

// =============================================================================
// SOUS-ONGLET 2 : ACCESSOIRES BLUETOOTH
// =============================================================================

@Composable
private fun BluetoothSubScreen(
    controller: SystemSettingsController,
    firstItemFocusRequester: FocusRequester,
    onRequestUnpair: (SystemSettingsController.BondedBluetoothDevice) -> Unit,
    onReturnLeft: () -> Unit
) {
    val soundManager = remember { TaloApplication.instance.soundEffectManager }
    var pairedDevices by remember { mutableStateOf(controller.getBondedBluetoothDevices()) }
    val deviceName = remember { controller.getDeviceName() }

    LaunchedEffect(Unit) {
        pairedDevices = controller.getBondedBluetoothDevices()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
    ) {
        item {
            RemotesActionCard(
                title = "Associer un nouvel accessoire",
                description = "Rechercher et connecter un casque audio sans fil, une barre de son, un clavier ou une souris Bluetooth.",
                badgeText = "Rechercher",
                badgeColor = Color(0xFF34C759),
                focusRequester = firstItemFocusRequester,
                onReturnLeft = onReturnLeft,
                onClick = {
                    soundManager.playSelectSound()
                    controller.openBluetoothSettings()
                }
            )
        }

        if (pairedDevices.isEmpty()) {
            item {
                RemotesActionCard(
                    title = "Périphériques appairés",
                    description = "Aucun accessoire Bluetooth n'est actuellement jumelé. Mettez votre équipement en mode appairage puis cliquez sur Rechercher.",
                    badgeText = "Aucun",
                    badgeColor = Color(0x28FFFFFF),
                    onReturnLeft = onReturnLeft
                )
            }
        } else {
            items(pairedDevices) { device ->
                RemotesActionCard(
                    title = device.name,
                    description = "Catégorie : ${device.type.label}\nAdresse MAC : ${device.address}",
                    badgeText = "Oublier",
                    badgeColor = Color(0xFFFF453A),
                    onReturnLeft = onReturnLeft,
                    onClick = {
                        soundManager.playSelectSound()
                        onRequestUnpair(device)
                    }
                )
            }
        }

        item {
            RemotesActionCard(
                title = "Visibilité de la TV",
                description = "Cet appareil est détectable par les autres périphériques Bluetooth sous le nom : $deviceName",
                badgeText = "Visible",
                badgeColor = Color(0x25FFFFFF),
                onReturnLeft = onReturnLeft
            )
        }
    }
}

// =============================================================================
// SOUS-ONGLET 3 : MANETTES DE JEU
// =============================================================================

@Composable
private fun GamepadsSubScreen(
    controller: SystemSettingsController,
    firstItemFocusRequester: FocusRequester,
    onReturnLeft: () -> Unit
) {
    val soundManager = remember { TaloApplication.instance.soundEffectManager }
    var gamepads by remember { mutableStateOf(controller.getConnectedGamepads()) }

    LaunchedEffect(Unit) {
        gamepads = controller.getConnectedGamepads()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
    ) {
        item {
            if (gamepads.isEmpty()) {
                RemotesActionCard(
                    title = "Manettes connectées",
                    description = "Aucune manette n'est connectée actuellement. Branchez une manette en USB ou appuyez sur Associer pour jumeler une manette Bluetooth (Xbox, PlayStation DualSense, Switch Pro, 8BitDo).",
                    badgeText = "Associer",
                    badgeColor = Color(0xFF34C759),
                    focusRequester = firstItemFocusRequester,
                    onReturnLeft = onReturnLeft,
                    onClick = {
                        soundManager.playSelectSound()
                        controller.openBluetoothRemotePairing()
                    }
                )
            } else {
                val pad = gamepads.first()
                RemotesActionCard(
                    title = pad.name,
                    description = "Statut : Manette connectée et opérationnelle\nVibration haptique : ${if (pad.hasVibrator) "Prise en charge" else "Non disponible"}\n${if (pad.batteryLevel != null) "Batterie : ${pad.batteryLevel}%" else "Alimentation active"}",
                    badgeText = "Connectée",
                    badgeColor = Color(0xFF34C759),
                    focusRequester = firstItemFocusRequester,
                    onReturnLeft = onReturnLeft
                )
            }
        }

        item {
            // Banc d'essai interactif des touches et sticks en temps réel
            GamepadTesterInteractiveCard(
                onReturnLeft = onReturnLeft
            )
        }

        item {
            RemotesActionCard(
                title = "Tester le retour de force (Vibration)",
                description = "Transmet une impulsion haptique de 500 ms aux moteurs de vibration de la manette pour vérifier leur bon fonctionnement.",
                badgeText = "Faire vibrer",
                badgeColor = Color(0xFFFF9F0A),
                onReturnLeft = onReturnLeft,
                onClick = {
                    soundManager.playSelectSound()
                    controller.testGamepadVibration(gamepads.firstOrNull()?.id)
                }
            )
        }
    }
}

/**
 * Banc d'essai interactif pour tester en direct les touches de manette
 */
@Composable
private fun GamepadTesterInteractiveCard(
    onReturnLeft: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    var lastPressedText by remember { mutableStateOf("Appuyez sur une touche pour tester") }
    var pressedCodes by remember { mutableStateOf(setOf<Int>()) }

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.02f else 1.0f,
        animationSpec = tween(120),
        label = "testerScale"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isFocused) Color(0xFF2C303A) else Color(0x20FFFFFF),
        animationSpec = tween(120),
        label = "testerBg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isFocused) Color.White else Color(0x14FFFFFF),
        animationSpec = tween(120),
        label = "testerBorder"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(14.dp))
            .focusable(interactionSource = interactionSource)
            .onPreviewKeyEvent { keyEvent ->
                val code = keyEvent.nativeKeyEvent.keyCode
                if (keyEvent.type == KeyEventType.KeyDown) {
                    val label = when (code) {
                        AndroidKeyEvent.KEYCODE_BUTTON_A -> "Bouton A (Croix)"
                        AndroidKeyEvent.KEYCODE_BUTTON_B -> "Bouton B (Rond)"
                        AndroidKeyEvent.KEYCODE_BUTTON_X -> "Bouton X (Carré)"
                        AndroidKeyEvent.KEYCODE_BUTTON_Y -> "Bouton Y (Triangle)"
                        AndroidKeyEvent.KEYCODE_BUTTON_L1 -> "Gâchette Haute Gauche (L1 / LB)"
                        AndroidKeyEvent.KEYCODE_BUTTON_R1 -> "Gâchette Haute Droite (R1 / RB)"
                        AndroidKeyEvent.KEYCODE_BUTTON_L2 -> "Gâchette Basse Gauche (L2 / LT)"
                        AndroidKeyEvent.KEYCODE_BUTTON_R2 -> "Gâchette Basse Droite (R2 / RT)"
                        AndroidKeyEvent.KEYCODE_BUTTON_THUMBL -> "Clic Stick Gauche (L3)"
                        AndroidKeyEvent.KEYCODE_BUTTON_THUMBR -> "Clic Stick Droit (R3)"
                        AndroidKeyEvent.KEYCODE_BUTTON_START -> "Start / Menu"
                        AndroidKeyEvent.KEYCODE_BUTTON_SELECT -> "Select / Share"
                        AndroidKeyEvent.KEYCODE_DPAD_UP -> "Croix Directionnelle Haut"
                        AndroidKeyEvent.KEYCODE_DPAD_DOWN -> "Croix Directionnelle Bas"
                        AndroidKeyEvent.KEYCODE_DPAD_LEFT -> "Croix Directionnelle Gauche"
                        AndroidKeyEvent.KEYCODE_DPAD_RIGHT -> "Croix Directionnelle Droite"
                        AndroidKeyEvent.KEYCODE_ENTER, AndroidKeyEvent.KEYCODE_DPAD_CENTER -> "Centre / Validation"
                        else -> null
                    }

                    if (label != null) {
                        lastPressedText = label
                        pressedCodes = pressedCodes + code
                    }

                    if (keyEvent.key == Key.Back || keyEvent.key == Key.DirectionLeft) {
                        onReturnLeft()
                        true
                    } else if (keyEvent.key == Key.DirectionRight) {
                        true
                    } else false
                } else if (keyEvent.type == KeyEventType.KeyUp) {
                    pressedCodes = pressedCodes - code
                    false
                } else false
            }
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Banc d'essai interactif des touches",
                color = if (isFocused) Color.White else Color(0xFFF2F2F7),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = if (isFocused) "Mode test actif" else "Sélectionnez pour tester",
                color = if (isFocused) Color(0xFF34C759) else Color(0x80FFFFFF),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Visualisation élégante des boutons de jeu
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // D-Pad
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "D-PAD", color = Color(0x80FFFFFF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    MiniButtonBadge("▲", pressedCodes.contains(AndroidKeyEvent.KEYCODE_DPAD_UP))
                    MiniButtonBadge("◄", pressedCodes.contains(AndroidKeyEvent.KEYCODE_DPAD_LEFT))
                    MiniButtonBadge("▼", pressedCodes.contains(AndroidKeyEvent.KEYCODE_DPAD_DOWN))
                    MiniButtonBadge("►", pressedCodes.contains(AndroidKeyEvent.KEYCODE_DPAD_RIGHT))
                }
            }

            // Boutons d'action
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "BOUTONS", color = Color(0x80FFFFFF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    MiniButtonBadge("X", pressedCodes.contains(AndroidKeyEvent.KEYCODE_BUTTON_X))
                    MiniButtonBadge("Y", pressedCodes.contains(AndroidKeyEvent.KEYCODE_BUTTON_Y))
                    MiniButtonBadge("A", pressedCodes.contains(AndroidKeyEvent.KEYCODE_BUTTON_A))
                    MiniButtonBadge("B", pressedCodes.contains(AndroidKeyEvent.KEYCODE_BUTTON_B))
                }
            }

            // Gâchettes
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "GÂCHETTES", color = Color(0x80FFFFFF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    MiniButtonBadge("LB", pressedCodes.contains(AndroidKeyEvent.KEYCODE_BUTTON_L1))
                    MiniButtonBadge("RB", pressedCodes.contains(AndroidKeyEvent.KEYCODE_BUTTON_R1))
                    MiniButtonBadge("LT", pressedCodes.contains(AndroidKeyEvent.KEYCODE_BUTTON_L2))
                    MiniButtonBadge("RT", pressedCodes.contains(AndroidKeyEvent.KEYCODE_BUTTON_R2))
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Dernier bouton détecté
        Text(
            text = "Dernière touche détectée : $lastPressedText",
            color = if (isFocused) Color(0xFFE5E5EA) else Color(0x99FFFFFF),
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun MiniButtonBadge(
    label: String,
    isPressed: Boolean
) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isPressed) Color.White else Color(0x22FFFFFF))
            .border(
                BorderStroke(1.dp, if (isPressed) Color.White else Color(0x18FFFFFF)),
                RoundedCornerShape(6.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isPressed) Color(0xFF0B0C0E) else Color(0xCCFFFFFF),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// =============================================================================
// SOUS-ONGLET 4 : CONTRÔLE HDMI-CEC (TALO LINK)
// =============================================================================

@Composable
private fun HdmiCecSubScreen(
    controller: SystemSettingsController,
    firstItemFocusRequester: FocusRequester,
    onReturnLeft: () -> Unit
) {
    val soundManager = remember { TaloApplication.instance.soundEffectManager }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
    ) {
        item {
            RemotesActionCard(
                title = "Réglages HDMI-CEC & Affichage TV",
                description = "Ouvre le panneau système officiel Google TV pour activer ou désactiver le contrôle HDMI (CEC), l'allumage One Touch Play, la mise en veille synchronisée et le contrôle du volume eARC.",
                badgeText = "Configurer",
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
            RemotesActionCard(
                title = "Fonctionnalités HDMI prises en charge",
                description = "Communication bidirectionnelle HDMI-CEC avec votre téléviseur, barre de son ou ampli Home Cinema (eARC / ARC).",
                badgeText = "Talo Link",
                badgeColor = Color(0x28FFFFFF),
                onReturnLeft = onReturnLeft
            )
        }

        item {
            RemotesActionCard(
                title = "Format vidéo et taux de rafraîchissement",
                description = "Ajuster la résolution d'affichage 4K Ultra HD, le taux de rafraîchissement dynamique (Match Content Frame Rate) et le format audio multicanal.",
                badgeText = "Affichage & Son",
                badgeColor = Color(0x35FFFFFF),
                onReturnLeft = onReturnLeft,
                onClick = {
                    soundManager.playSelectSound()
                    controller.openHdmiCecSettings()
                }
            )
        }
    }
}

// =============================================================================
// SOUS-ONGLET 5 : RETROUVER LA TÉLÉCOMMANDE (FIND MY REMOTE)
// =============================================================================

@Composable
private fun FindRemoteSubScreen(
    controller: SystemSettingsController,
    firstItemFocusRequester: FocusRequester,
    onReturnLeft: () -> Unit
) {
    val soundManager = remember { TaloApplication.instance.soundEffectManager }
    val coroutineScope = rememberCoroutineScope()
    var isRinging by remember { mutableStateOf(controller.isFindMyRemoteActive) }

    // Animation radar / sonar pulsante en continu
    val infiniteTransition = rememberInfiniteTransition(label = "sonarTransition")
    val waveScale1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveScale1"
    )
    val waveAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveAlpha1"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
    ) {
        item {
            // Carte principale avec animation Sonar et bouton d'action
            RemotesFindRemoteHeroCard(
                isRinging = isRinging,
                waveScale = waveScale1,
                waveAlpha = waveAlpha1,
                focusRequester = firstItemFocusRequester,
                onReturnLeft = onReturnLeft,
                onToggleRinging = {
                    soundManager.playSelectSound()
                    if (isRinging) {
                        controller.stopFindMyRemote()
                        isRinging = false
                    } else {
                        controller.startFindMyRemote(coroutineScope)
                        isRinging = true
                    }
                }
            )
        }

        item {
            RemotesActionCard(
                title = "Bouton matériel Google TV Streamer 4K",
                description = "Vous pouvez également appuyer sur le bouton physique situé à l'arrière de votre boîtier Google TV Streamer 4K pour déclencher le bipeur sans allumer la TV.",
                badgeText = "Matériel 4K",
                badgeColor = Color(0x30FFFFFF),
                onReturnLeft = onReturnLeft
            )
        }

        item {
            RemotesActionCard(
                title = "Spécifications du signal sonore",
                description = "Bipeur piézoélectrique haute fréquence 85 dB audible à travers les coussins de canapé et cloisons intérieures. Portée Bluetooth : jusqu'à 10 mètres.",
                badgeText = "85 dB • 10m",
                badgeColor = Color(0x25FFFFFF),
                onReturnLeft = onReturnLeft
            )
        }
    }
}

/**
 * Carte Héro pour "Retrouver la télécommande" avec animation Sonar / Radar
 */
@Composable
private fun RemotesFindRemoteHeroCard(
    isRinging: Boolean,
    waveScale: Float,
    waveAlpha: Float,
    focusRequester: FocusRequester,
    onReturnLeft: () -> Unit,
    onToggleRinging: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.02f else 1.0f,
        animationSpec = tween(120),
        label = "heroScale"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isFocused) Color(0xFF2C303A) else Color(0x20FFFFFF),
        animationSpec = tween(120),
        label = "heroBg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isFocused) Color.White else Color(0x14FFFFFF),
        animationSpec = tween(120),
        label = "heroBorder"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(16.dp))
            .focusRequester(focusRequester)
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.key) {
                        Key.DirectionLeft, Key.Back -> {
                            onReturnLeft()
                            true
                        }
                        Key.DirectionRight -> true
                        Key.Enter, Key.NumPadEnter, Key.DirectionCenter -> {
                            onToggleRinging()
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
                onToggleRinging()
            }
            .padding(horizontal = 22.dp, vertical = 18.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Faire sonner la télécommande",
                color = if (isFocused) Color.White else Color(0xFFF2F2F7),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isRinging) {
                    "Signal sonore actif ! La télécommande émet un carillon distinct pour vous guider."
                } else {
                    "Déclenche le bipeur haute fréquence intégré à votre télécommande vocale Google TV."
                },
                color = if (isRinging) Color(0xFFFF9F0A) else if (isFocused) Color(0xFFD1D1D6) else Color(0x99FFFFFF),
                fontSize = 12.5.sp,
                lineHeight = 17.sp
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Visualiseur Sonar / Radar
            Box(
                modifier = Modifier.size(34.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.minDimension / 2f
                    if (isRinging) {
                        drawCircle(
                            color = Color(0xFFFF9F0A).copy(alpha = waveAlpha),
                            radius = radius * waveScale,
                            style = Stroke(width = 2.dp.toPx())
                        )
                        drawCircle(
                            color = Color(0xFFFF9F0A),
                            radius = radius * 0.35f
                        )
                    } else {
                        drawCircle(
                            color = Color(0x40FFFFFF),
                            radius = radius * 0.6f,
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                        drawCircle(
                            color = Color(0x60FFFFFF),
                            radius = radius * 0.25f
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        when {
                            isFocused -> Color.White
                            isRinging -> Color(0xFFFF9F0A)
                            else -> Color(0xFF34C759)
                        }
                    )
                    .padding(horizontal = 16.dp, vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isRinging) "Arrêter" else "Faire sonner",
                    color = if (isFocused) Color(0xFF0B0C0E) else Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}

// =============================================================================
// COMPOSANTS RÉUTILISABLES (CARTES, SWITCHS & DIALOGUES)
// =============================================================================

@Composable
private fun RemotesActionCard(
    title: String,
    description: String,
    badgeText: String? = null,
    badgeColor: Color = Color(0x35FFFFFF),
    focusRequester: FocusRequester? = null,
    onReturnLeft: () -> Unit,
    onClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

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
private fun RemotesSwitchCard(
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

        // Interrupteur Pill Talo OS
        Box(
            modifier = Modifier
                .width(46.dp)
                .height(26.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(switchBgColor)
                .border(
                    BorderStroke(1.dp, if (isFocused) Color.White else Color(0x18FFFFFF)),
                    RoundedCornerShape(13.dp)
                ),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .padding(start = knobOffset)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
    }
}

/**
 * Dialogue de confirmation sécurisé au D-Pad avec focus par défaut sur "Annuler"
 */
@Composable
private fun RemotesConfirmationDialog(
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
                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {}
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
                // Bouton Annuler (Focus sécurisé par défaut)
                RemotesDialogActionButton(
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
                RemotesDialogActionButton(
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
private fun RowScope.RemotesDialogActionButton(
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
                        Key.DirectionUp, Key.DirectionDown -> true // Bloque toute fuite de focus
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
