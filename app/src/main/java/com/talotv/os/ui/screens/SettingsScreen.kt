package com.talotv.os.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.talotv.os.R
import com.talotv.os.TaloApplication
import kotlinx.coroutines.delay

/**
 * 9 Onglets Principaux des Réglages Talo OS
 */
enum class SettingsCategory(val title: String) {
    GENERAL("Général"),
    USERS("Utilisateurs et comptes"),
    NETWORK("Réseau"),
    VIDEO_AUDIO("Vidéo et audio"),
    APPS("Applications"),
    NOTIFICATIONS("Notifications"),
    REMOTES("Télécommandes et appareils"),
    SYSTEM("Système"),
    HELP("Aide")
}

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val soundManager = remember { TaloApplication.instance.soundEffectManager }

    val versionName = remember {
        try {
            val name = context.packageManager.getPackageInfo(context.packageName, 0).versionName
            if (name.isNullOrBlank()) "0.0.1" else name
        } catch (e: Exception) {
            "0.0.1"
        }
    }

    var selectedIndex by remember { mutableIntStateOf(0) }
    var showHelpHub by remember { mutableStateOf(false) }

    val categories = SettingsCategory.values()
    val focusRequesters = remember { List(categories.size) { FocusRequester() } }

    // Interception de la touche Retour vers l'écran d'accueil quand le hub n'est pas ouvert
    BackHandler(enabled = !showHelpHub) {
        onNavigateBack()
    }

    // Gestion du focus sur la liste des réglages (à l'ouverture initiale et lors du retour du Help Hub)
    LaunchedEffect(showHelpHub) {
        if (!showHelpHub) {
            delay(50)
            try {
                focusRequesters.getOrNull(selectedIndex)?.requestFocus()
            } catch (_: Exception) {}
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF14171D),
                        Color(0xFF0C0E12),
                        Color(0xFF07080A)
                    )
                )
            )
    ) {
        // ÉCRAN PRINCIPAL DES RÉGLAGES
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 64.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Titre supérieur centré
            Text(
                text = "Réglages",
                color = Color(0xCCFFFFFF),
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Section principale : Gauche (Badge Logo + Version) | Droite (Liste avec chevrons)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // COLONNE GAUCHE : Boîte Logo Squircle avec version en-dessous
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 56.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Carte Squircle avec le logo Talo OS
                    Box(
                        modifier = Modifier
                            .size(260.dp)
                            .clip(RoundedCornerShape(36.dp))
                            .background(Color(0xFF1E2127))
                            .border(
                                BorderStroke(1.dp, Color(0x18FFFFFF)),
                                RoundedCornerShape(36.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.talo_os_logo_white),
                            contentDescription = "Talo OS",
                            modifier = Modifier
                                .width(160.dp)
                                .height(70.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Version du système en-dessous
                    Text(
                        text = "v$versionName",
                        color = Color(0x80FFFFFF),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp
                    )
                }

                // COLONNE DROITE : Liste verticale des 9 catégories avec flèches
                LazyColumn(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    itemsIndexed(categories) { index, category ->
                        SettingsMenuItem(
                            title = category.title,
                            isFocused = (selectedIndex == index),
                            focusRequester = focusRequesters[index],
                            onFocused = {
                                selectedIndex = index
                                soundManager.playFocusSound()
                            },
                            onClick = {
                                soundManager.playSelectSound()
                                when (category) {
                                    SettingsCategory.HELP -> {
                                        // Entre dans le Hub d'Aide TV
                                        showHelpHub = true
                                    }
                                    else -> {
                                        // À développer progressivement
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        // COUCHE DU HUB D'AIDE TV (Superposition fluide)
        if (showHelpHub) {
            HelpHubScreen(
                versionName = versionName,
                onClose = {
                    soundManager.playSelectSound()
                    showHelpHub = false
                }
            )
        }
    }
}

/**
 * Élément de menu de réglage individuel avec indicateur chevron
 * - Flèche Droite ou Bouton OK / Centre entre dans la section
 */
@Composable
private fun SettingsMenuItem(
    title: String,
    isFocused: Boolean,
    focusRequester: FocusRequester,
    onFocused: () -> Unit,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val actualFocus by interactionSource.collectIsFocusedAsState()

    LaunchedEffect(actualFocus) {
        if (actualFocus) onFocused()
    }

    val scale by animateFloatAsState(
        targetValue = if (actualFocus) 1.02f else 1.0f,
        animationSpec = tween(120),
        label = "menuScale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (actualFocus) Color.White else Color(0x24FFFFFF),
        animationSpec = tween(120),
        label = "menuBg"
    )

    val contentColor by animateColorAsState(
        targetValue = if (actualFocus) Color(0xFF0B0C0E) else Color(0xFFF2F2F7),
        animationSpec = tween(120),
        label = "menuContent"
    )

    val chevronColor by animateColorAsState(
        targetValue = if (actualFocus) Color(0xFF0B0C0E) else Color(0x60FFFFFF),
        animationSpec = tween(120),
        label = "chevronColor"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .scale(scale)
            .clip(RoundedCornerShape(13.dp))
            .background(backgroundColor)
            .border(
                BorderStroke(
                    width = 1.dp,
                    color = if (actualFocus) Color.White else Color(0x10FFFFFF)
                ),
                shape = RoundedCornerShape(13.dp)
            )
            .focusRequester(focusRequester)
            .focusable(interactionSource = interactionSource)
            .clickable { onClick() }
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.key) {
                        Key.DirectionRight, Key.Enter, Key.NumPadEnter, Key.DirectionCenter -> {
                            onClick()
                            true
                        }
                        else -> false
                    }
                } else false
            }
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = title,
            color = contentColor,
            fontSize = 16.sp,
            fontWeight = if (actualFocus) FontWeight.SemiBold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Icon(
            painter = painterResource(id = R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = chevronColor,
            modifier = Modifier.size(18.dp)
        )
    }
}

/**
 * Hub d'Aide 100% Natif TV :
 * - Navigation TV D-Pad : Flèche Gauche ou Touche Retour ressort immédiatement
 * - Pas de bouton virtuel à l'écran conformément aux directives Android TV
 */
@Composable
private fun HelpHubScreen(
    versionName: String,
    onClose: () -> Unit
) {
    val soundManager = remember { TaloApplication.instance.soundEffectManager }
    val cardFocusRequesters = remember { List(4) { FocusRequester() } }

    // Interception de la touche physique Retour de la télécommande
    BackHandler {
        onClose()
    }

    // Mise au point immédiate sur la première carte au D-Pad
    LaunchedEffect(Unit) {
        delay(60)
        try {
            cardFocusRequesters.getOrNull(0)?.requestFocus()
        } catch (_: Exception) {}
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF14171E),
                        Color(0xFF0C0E13),
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
            // EN-TÊTE : Titre du Hub & Indication de navigation TV épurée (pas de faux bouton)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.talo_os_logo_white),
                        contentDescription = "Talo OS",
                        modifier = Modifier
                            .width(88.dp)
                            .height(38.dp)
                    )
                    Spacer(modifier = Modifier.width(18.dp))
                    Column {
                        Text(
                            text = "Centre d'Aide & Documentation",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Talo OS v$versionName • Conçu pour Google TV Streamer 4K",
                            color = Color(0x80FFFFFF),
                            fontSize = 12.sp
                        )
                    }
                }

                // Indication discrète du contrôle TV (conforme aux guidelines Android TV)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x18FFFFFF))
                        .border(BorderStroke(1.dp, Color(0x10FFFFFF)), RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "← Flèche Gauche ou Retour pour quitter",
                        color = Color(0x99FFFFFF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // CORPS : 2 Colonnes équilibrées
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(36.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // COLONNE GAUCHE : Compagnon Mobile avec QR Code Wiki
                Column(
                    modifier = Modifier
                        .weight(0.9f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(28.dp))
                            .background(Color(0xFF1B1E24))
                            .border(BorderStroke(1.dp, Color(0x18FFFFFF)), RoundedCornerShape(28.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Wiki GitHub Officiel",
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Scannez avec votre smartphone pour lire la documentation complète.",
                                color = Color(0x80FFFFFF),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Boîte blanche contrastée pour le QR Code
                            Box(
                                modifier = Modifier
                                    .size(190.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White)
                                    .padding(10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.qr_wiki),
                                    contentDescription = "QR Code Wiki",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Badges pratiques
                            Text(
                                text = "github.com/sudo-aptttt/talo-tv/wiki",
                                color = Color(0xFF0071E3),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Code Downloader : 6790525",
                                color = Color(0xFF8E8E93),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // COLONNE DROITE : 4 Cartes d'aide natives navigables au D-Pad
                // Flèche Gauche ou Touche Retour ressort du Hub
                LazyColumn(
                    modifier = Modifier
                        .weight(1.3f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        HelpCardItem(
                            title = "Raccourcis Télécommande",
                            description = "• Accueil (Home) : Revenir directement à Talo OS.\n• Retour (Back) : Quitter l'application en cours.\n• D-Pad (Flèches) : Défilement fluide de l'interface.",
                            focusRequester = cardFocusRequesters[0],
                            onFocused = { soundManager.playFocusSound() },
                            onExit = onClose
                        )
                    }

                    item {
                        HelpCardItem(
                            title = "Mises à jour du système",
                            description = "Talo OS vérifie automatiquement les mises à jour au démarrage. Vous pouvez également réinstaller ou mettre à jour via l'application Downloader avec le code permanent 6790525.",
                            focusRequester = cardFocusRequesters[1],
                            onFocused = { soundManager.playFocusSound() },
                            onExit = onClose
                        )
                    }

                    item {
                        HelpCardItem(
                            title = "Confidentialité & Zéro Publicité",
                            description = "Le bouclier Talo Privacy Shield est actif en temps réel. Il neutralise les traqueurs et SDK publicitaires en arrière-plan sans impacter la qualité de streaming ni le DRM Widevine L1.",
                            focusRequester = cardFocusRequesters[2],
                            onFocused = { soundManager.playFocusSound() },
                            onExit = onClose
                        )
                    }

                    item {
                        HelpCardItem(
                            title = "Assistance & Signalement de bogue",
                            description = "En cas de dysfonctionnement, redémarrez la box depuis Réglages > Système. Pour suggérer des fonctionnalités ou rapporter un souci, rendez-vous sur le dépôt GitHub du projet.",
                            focusRequester = cardFocusRequesters[3],
                            onFocused = { soundManager.playFocusSound() },
                            onExit = onClose
                        )
                    }
                }
            }
        }
    }
}

/**
 * Carte d'information native optimisée pour l'écran TV avec navigation D-Pad
 * - Flèche Gauche ou Touche Retour ressort immédiatement du Hub
 */
@Composable
private fun HelpCardItem(
    title: String,
    description: String,
    focusRequester: FocusRequester,
    onFocused: () -> Unit,
    onExit: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    LaunchedEffect(isFocused) {
        if (isFocused) onFocused()
    }

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.02f else 1.0f,
        animationSpec = tween(120),
        label = "helpCardScale"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isFocused) Color(0xFF2C303A) else Color(0x20FFFFFF),
        animationSpec = tween(120),
        label = "helpCardBg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isFocused) Color.White else Color(0x14FFFFFF),
        animationSpec = tween(120),
        label = "helpCardBorder"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .background(bgColor)
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(18.dp))
            .focusRequester(focusRequester)
            .focusable(interactionSource = interactionSource)
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.key) {
                        Key.DirectionLeft, Key.Back -> {
                            onExit()
                            true
                        }
                        else -> false
                    }
                } else false
            }
            .padding(horizontal = 22.dp, vertical = 16.dp)
    ) {
        Text(
            text = title,
            color = if (isFocused) Color.White else Color(0xFFF2F2F7),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = description,
            color = if (isFocused) Color(0xFFD1D1D6) else Color(0x99FFFFFF),
            fontSize = 13.sp,
            lineHeight = 18.sp
        )
    }
}
