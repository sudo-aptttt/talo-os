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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.talotv.os.R
import com.talotv.os.TaloApplication
import kotlinx.coroutines.delay

/**
 * 10 Catégories Principales des Réglages Talo OS
 * Architecture inspirée des meilleurs systèmes TV Leanback :
 * - Navigation fluide au D-Pad (Flèche Droite pour entrer, Flèche Gauche ou Retour pour sortir)
 * - Zéro bouton retour virtuel affiché à l'écran
 * - Design Frosted Glass sobre et élégant
 */
enum class SettingsCategory(val title: String) {
    NETWORK("Réseau et Internet"),
    VIDEO_AUDIO("Affichage et son"),
    USERS("Comptes et profils"),
    APPS("Applications et stockage"),
    REMOTES("Télécommandes et appareils"),
    PRIVACY("Confidentialité et sécurité"),
    NOTIFICATIONS("Notifications"),
    GENERAL("Général et préférences"),
    SYSTEM("Système"),
    HELP("Aide et support")
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

    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
    var activeCategory by rememberSaveable { mutableStateOf<SettingsCategory?>(null) }
    var isRestoringFocus by remember { mutableStateOf(false) }

    val categories = remember { SettingsCategory.values() }
    val focusRequesters = remember { List(categories.size) { FocusRequester() } }
    val listState = rememberLazyListState()

    // Touche Retour physique : retourne à l'accueil si aucun sous-menu n'est ouvert
    BackHandler(enabled = activeCategory == null) {
        onNavigateBack()
    }

    // Gestion du focus sur la liste principale
    LaunchedEffect(activeCategory) {
        if (activeCategory == null) {
            try {
                listState.scrollToItem(selectedIndex)
            } catch (_: Exception) {}
            repeat(4) {
                delay(20)
                try {
                    focusRequesters.getOrNull(selectedIndex)?.requestFocus()
                } catch (_: Exception) {}
            }
            delay(50)
            isRestoringFocus = false
        }
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
        // ÉCRAN PRINCIPAL DES RÉGLAGES
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 64.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Titre supérieur centré
            Text(
                text = "Paramètres",
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

                    Text(
                        text = "v$versionName",
                        color = Color(0x80FFFFFF),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp
                    )
                }

                // COLONNE DROITE : Liste verticale des 10 catégories avec flèches
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    itemsIndexed(categories) { index, category ->
                        SettingsMenuItem(
                            title = category.title,
                            focusRequester = focusRequesters[index],
                            onFocused = {
                                if (activeCategory == null && !isRestoringFocus) {
                                    selectedIndex = index
                                    soundManager.playFocusSound()
                                }
                            },
                            onClick = {
                                selectedIndex = index
                                soundManager.playSelectSound()
                                activeCategory = category
                            },
                            onBack = onNavigateBack
                        )
                    }
                }
            }
        }

        // SOUS-ÉCRANS THÉMATIQUES TALO OS (Superposition plein écran fluide)
        when (activeCategory) {
            SettingsCategory.NETWORK -> {
                NetworkSettingsScreen(
                    onClose = {
                        soundManager.playSelectSound()
                        isRestoringFocus = true
                        activeCategory = null
                    }
                )
            }
            SettingsCategory.VIDEO_AUDIO -> {
                VideoAudioSettingsScreen(
                    onClose = {
                        soundManager.playSelectSound()
                        isRestoringFocus = true
                        activeCategory = null
                    }
                )
            }
            SettingsCategory.USERS -> {
                UsersSettingsScreen(
                    onClose = {
                        soundManager.playSelectSound()
                        isRestoringFocus = true
                        activeCategory = null
                    }
                )
            }
            SettingsCategory.APPS -> {
                AppsSettingsScreen(
                    onClose = {
                        soundManager.playSelectSound()
                        isRestoringFocus = true
                        activeCategory = null
                    }
                )
            }
            SettingsCategory.REMOTES -> {
                RemotesSettingsScreen(
                    onClose = {
                        soundManager.playSelectSound()
                        isRestoringFocus = true
                        activeCategory = null
                    }
                )
            }
            SettingsCategory.PRIVACY -> {
                PrivacySettingsScreen(
                    onClose = {
                        soundManager.playSelectSound()
                        isRestoringFocus = true
                        activeCategory = null
                    }
                )
            }
            SettingsCategory.NOTIFICATIONS -> {
                NotificationsSettingsScreen(
                    onClose = {
                        soundManager.playSelectSound()
                        isRestoringFocus = true
                        activeCategory = null
                    }
                )
            }
            SettingsCategory.GENERAL -> {
                GeneralSettingsScreen(
                    onClose = {
                        soundManager.playSelectSound()
                        isRestoringFocus = true
                        activeCategory = null
                    }
                )
            }
            SettingsCategory.SYSTEM -> {
                SystemSettingsScreen(
                    versionName = versionName,
                    onClose = {
                        soundManager.playSelectSound()
                        isRestoringFocus = true
                        activeCategory = null
                    }
                )
            }
            SettingsCategory.HELP -> {
                HelpHubScreen(
                    versionName = versionName,
                    onClose = {
                        soundManager.playSelectSound()
                        isRestoringFocus = true
                        activeCategory = null
                    }
                )
            }
            null -> { /* Aucun sous-menu actif */ }
        }
    }
}

/**
 * Élément de menu de réglage individuel avec indicateur chevron
 * - Flèche Droite ou Bouton OK / Centre entre dans la section
 * - Flèche Gauche ou Bouton Retour retourne à l'écran d'accueil
 */
@Composable
private fun SettingsMenuItem(
    title: String,
    focusRequester: FocusRequester,
    onFocused: () -> Unit,
    onClick: () -> Unit,
    onBack: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    var isFocused by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.02f else 1.0f,
        animationSpec = tween(120),
        label = "menuScale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isFocused) Color.White else Color(0x24FFFFFF),
        animationSpec = tween(120),
        label = "menuBg"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isFocused) Color(0xFF0B0C0E) else Color(0xFFF2F2F7),
        animationSpec = tween(120),
        label = "menuContent"
    )

    val chevronColor by animateColorAsState(
        targetValue = if (isFocused) Color(0xFF0B0C0E) else Color(0x60FFFFFF),
        animationSpec = tween(120),
        label = "chevronColor"
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
                    color = if (isFocused) Color.White else Color(0x10FFFFFF)
                ),
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
                            onClick()
                            true
                        }
                        Key.DirectionLeft, Key.Back -> {
                            onBack()
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
            }
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = title,
            color = contentColor,
            fontSize = 15.sp,
            fontWeight = if (isFocused) FontWeight.SemiBold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Icon(
            painter = painterResource(id = R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = chevronColor,
            modifier = Modifier.size(16.dp)
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

    BackHandler {
        onClose()
    }

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
                .padding(horizontal = 64.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Titre supérieur centré
            Text(
                text = "Aide et support",
                color = Color(0xCCFFFFFF),
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Section principale : Gauche (Compagnon QR Wiki) | Droite (4 Cartes d'aide)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // COLONNE GAUCHE : Compagnon Mobile avec QR Code Wiki
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 56.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(280.dp)
                            .clip(RoundedCornerShape(32.dp))
                            .background(Color(0xFF1E2127))
                            .border(BorderStroke(1.dp, Color(0x18FFFFFF)), RoundedCornerShape(32.dp))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Wiki GitHub",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Scannez avec votre smartphone",
                                color = Color(0x80FFFFFF),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Box(
                                modifier = Modifier
                                    .size(140.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.White)
                                    .padding(6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.qr_wiki),
                                    contentDescription = "QR Code Wiki",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "github.com/sudo-aptttt/talo-tv/wiki",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Code Downloader : 6790525",
                                color = Color(0xFF8E8E93),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // COLONNE DROITE : 4 Cartes d'aide navigables au D-Pad
                LazyColumn(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        HelpCardItem(
                            title = "Raccourcis Télécommande TV",
                            description = "• Flèche Droite ou OK : Entrer dans une section ou valider un paramètre\n• Flèche Gauche ou Touche Retour : Revenir en arrière\n• Touche Accueil (Home) : Revenir au tableau de bord",
                            focusRequester = cardFocusRequesters[0],
                            onFocused = { soundManager.playFocusSound() },
                            onExit = onClose
                        )
                    }

                    item {
                        HelpCardItem(
                            title = "Mises à jour du système",
                            description = "Talo OS vérifie automatiquement les mises à jour au démarrage. Code Downloader permanent : 6790525 pour réinstaller ou mettre à niveau.",
                            focusRequester = cardFocusRequesters[1],
                            onFocused = { soundManager.playFocusSound() },
                            onExit = onClose
                        )
                    }

                    item {
                        HelpCardItem(
                            title = "Confidentialité & Zéro Publicité",
                            description = "Le bouclier Talo Privacy Shield est actif en temps réel. Zéro traqueur, zéro publicité commerciale, DRM Widevine L1 préservé.",
                            focusRequester = cardFocusRequesters[2],
                            onFocused = { soundManager.playFocusSound() },
                            onExit = onClose
                        )
                    }

                    item {
                        HelpCardItem(
                            title = "Assistance & Maintenance",
                            description = "Redémarrez le boîtier ou libérez la mémoire vive depuis Paramètres > Système. En cas de dysfonctionnement, consultez le wiki GitHub.",
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
    var isFocused by remember { mutableStateOf(false) }

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
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(16.dp))
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
                        Key.DirectionLeft, Key.Back -> {
                            onExit()
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
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            text = title,
            color = if (isFocused) Color.White else Color(0xFFF2F2F7),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = description,
            color = if (isFocused) Color(0xFFD1D1D6) else Color(0x99FFFFFF),
            fontSize = 12.5.sp,
            lineHeight = 17.sp
        )
    }
}
