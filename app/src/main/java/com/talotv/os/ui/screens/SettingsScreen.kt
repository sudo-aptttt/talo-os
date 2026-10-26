package com.talotv.os.ui.screens

import android.widget.Toast
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.talotv.os.R
import com.talotv.os.TaloApplication
import com.talotv.os.core.audio.SoundEffectManager
import com.talotv.os.core.security.DeviceFingerprint
import com.talotv.os.core.system.SystemSettingsController
import kotlinx.coroutines.delay

// =============================================================================
// TALO SETTINGS PALETTE
// =============================================================================
private val TaloDarkBackground = Color(0xFF0A0A0C)
private val TaloCardBackground = Color(0x22FFFFFF)
private val TaloCardBorder = Color(0x1AFFFFFF)
private val TaloTextPrimary = Color(0xFFFFFFFF)
private val TaloTextSecondary = Color(0xFF8E8E93)
private val TaloAccentBlue = Color(0xFF0071E3)

/**
 * 9 Onglets Principaux des Réglages
 */
enum class SettingsCategory(val title: String) {
    GENERAL("Général"),
    USERS("Utilisateurs et comptes"),
    NETWORK("Réseau"),
    VIDEO_AUDIO("Vidéo et Audio"),
    APPS("Applications"),
    NOTIFICATIONS("Notifications"),
    REMOTES("Télécommandes et appareils"),
    SYSTEM("Système"),
    HELP("Aide")
}

data class SettingItem(
    val title: String,
    val value: String? = null,
    val isAction: Boolean = false,
    val actionText: String? = null,
    val onClick: () -> Unit = {}
)

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val soundManager = remember { TaloApplication.instance.soundEffectManager }
    val systemController = remember { SystemSettingsController(context) }
    val installedAppsManager = remember { TaloApplication.instance.installedAppsManager }
    val installedApps by installedAppsManager.installedApps.collectAsState()

    val versionName = remember {
        try {
            val name = context.packageManager.getPackageInfo(context.packageName, 0).versionName
            if (name.isNullOrBlank()) "0.0.1" else name
        } catch (e: Exception) {
            "0.0.1"
        }
    }
    val fingerprint: String = remember { DeviceFingerprint.getFingerprint(context) }

    // Navigation State: 0 = Categories List (Left), 1 = Detail Items (Right)
    var focusColumn by remember { mutableIntStateOf(0) }
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    var selectedItemIndex by remember { mutableIntStateOf(0) }

    val categories = SettingsCategory.values()
    val activeCategory = categories[selectedCategoryIndex]

    val categoryFocusRequesters = remember { List(categories.size) { FocusRequester() } }
    val itemFocusRequesters = remember { List(15) { FocusRequester() } }

    // Build items for the active category cleanly
    val categoryItems: List<SettingItem> = remember(activeCategory, installedApps, versionName) {
        when (activeCategory) {
            SettingsCategory.GENERAL -> listOf(
                SettingItem("Nom de l'appareil", systemController.getDeviceModel()),
                SettingItem("Langue", "Français"),
                SettingItem("Fuseau horaire", "Europe/Paris (UTC+1)")
            )
            SettingsCategory.USERS -> listOf(
                SettingItem("Profil actif", "Principal"),
                SettingItem("Comptes associés", "Compte local Talo TV"),
                SettingItem("Protection parentale", "Désactivée")
            )
            SettingsCategory.NETWORK -> listOf(
                SettingItem("Connexion Wi-Fi", "Connecté"),
                SettingItem("Nom du réseau", "Bbox-Fib-5G (5 GHz)"),
                SettingItem("État Internet", "Optimal • Débit vérifié")
            )
            SettingsCategory.VIDEO_AUDIO -> listOf(
                SettingItem("Profil d'affichage", "Ultra • 4K HDR (60 FPS)"),
                SettingItem("Plage dynamique", "Dolby Vision / HDR10+"),
                SettingItem("Sortie audio", "HDMI eARC Passthrough (Dolby Atmos)")
            )
            SettingsCategory.APPS -> listOf(
                SettingItem("Applications installées", "${installedApps.size} applications détectées"),
                SettingItem("Stockage des apps", "Espace disponible vérifié")
            )
            SettingsCategory.NOTIFICATIONS -> listOf(
                SettingItem("Talo OS 0.0.1", "Système opérationnel et à jour"),
                SettingItem("Mode Perdu", "Bouclier de sécurité infranchissable actif")
            )
            SettingsCategory.REMOTES -> listOf(
                SettingItem("Télécommande principale", "Talo Remote (Connectée)"),
                SettingItem("Niveau de batterie", "92%")
            )
            SettingsCategory.SYSTEM -> listOf(
                SettingItem("Modèle", systemController.getDeviceModel()),
                SettingItem("Version Talo OS", versionName),
                SettingItem("Créateur / Développeur", "sudo-aptttt"),
                SettingItem("Appareil cible", "Google TV Streamer 4K"),
                SettingItem("Licence", "PolyForm Noncommercial 1.0.0"),
                SettingItem(
                    title = "Redémarrer le système",
                    isAction = true,
                    actionText = "Redémarrer",
                    onClick = {
                        soundManager.playSelectSound()
                        Toast.makeText(context, "Redémarrage de Talo OS...", Toast.LENGTH_SHORT).show()
                    }
                )
            )
            SettingsCategory.HELP -> listOf(
                SettingItem("Documentation officielle", "https://github.com/sudo-aptttt/talo-tv/wiki"),
                SettingItem("Code Downloader permanent", "6790525"),
                SettingItem(
                    title = "Ouvrir la documentation",
                    isAction = true,
                    actionText = "Consulter",
                    onClick = {
                        soundManager.playSelectSound()
                        systemController.openWikiWeb()
                    }
                )
            )
        }
    }

    // Back handling
    BackHandler {
        if (focusColumn == 1) {
            soundManager.playSelectSound()
            focusColumn = 0
        } else {
            onNavigateBack()
        }
    }

    // Dynamic focus manager
    LaunchedEffect(focusColumn, selectedCategoryIndex) {
        delay(40)
        try {
            if (focusColumn == 0) {
                categoryFocusRequesters.getOrNull(selectedCategoryIndex)?.requestFocus()
            } else {
                itemFocusRequesters.getOrNull(selectedItemIndex.coerceIn(0, categoryItems.lastIndex))?.requestFocus()
            }
        } catch (_: Exception) {}
    }

    // =========================================================================
    // UI LAYOUT : 2 COLUMNS (Clean & Uncluttered)
    // =========================================================================
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TaloDarkBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 48.dp, end = 48.dp, top = 36.dp, bottom = 36.dp)
        ) {
            // LEFT COLUMN : Brand Header + Primary Category Tabs
            Column(
                modifier = Modifier
                    .width(360.dp)
                    .fillMaxHeight()
            ) {
                // Brand Lockup Header
                Column(
                    modifier = Modifier.padding(bottom = 24.dp, start = 8.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.talo_os_logo_white),
                        contentDescription = "Talo OS",
                        modifier = Modifier
                            .width(160.dp)
                            .height(68.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Réglages • v$versionName",
                        color = TaloTextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Categories List (Primary Tabs)
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(categories) { index, category ->
                        CategoryTabItem(
                            title = category.title,
                            isSelected = (selectedCategoryIndex == index),
                            isFocused = (focusColumn == 0 && selectedCategoryIndex == index),
                            focusRequester = categoryFocusRequesters[index],
                            onFocused = {
                                selectedCategoryIndex = index
                                selectedItemIndex = 0
                            },
                            onClick = {
                                soundManager.playSelectSound()
                                focusColumn = 1
                            },
                            onRight = {
                                soundManager.playSelectSound()
                                focusColumn = 1
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(48.dp))

            // RIGHT COLUMN : Detail Cards for Selected Category
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                // Section Title
                Text(
                    text = activeCategory.title,
                    color = TaloTextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 20.dp, top = 12.dp)
                )

                // Detail Cards List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(categoryItems) { itemIndex, item ->
                        SettingCardItem(
                            item = item,
                            isFocused = (focusColumn == 1 && selectedItemIndex == itemIndex),
                            focusRequester = itemFocusRequesters.getOrNull(itemIndex),
                            onFocused = { selectedItemIndex = itemIndex },
                            onClick = {
                                soundManager.playSelectSound()
                                item.onClick()
                            },
                            onLeft = {
                                soundManager.playSelectSound()
                                focusColumn = 0
                            }
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// CATEGORY TAB COMPOSABLE (Left Column)
// =============================================================================
@Composable
private fun CategoryTabItem(
    title: String,
    isSelected: Boolean,
    isFocused: Boolean,
    focusRequester: FocusRequester,
    onFocused: () -> Unit,
    onClick: () -> Unit,
    onRight: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val actualFocus by interactionSource.collectIsFocusedAsState()

    LaunchedEffect(actualFocus) {
        if (actualFocus) onFocused()
    }

    val scale by animateFloatAsState(
        targetValue = if (actualFocus) 1.02f else 1.0f,
        label = "tabScale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = when {
            actualFocus -> Color.White
            isSelected -> Color(0x28FFFFFF)
            else -> Color(0x0EFFFFFF)
        },
        animationSpec = tween(150),
        label = "tabBg"
    )

    val textColor by animateColorAsState(
        targetValue = when {
            actualFocus -> Color(0xFF0A0A0C)
            isSelected -> Color.White
            else -> TaloTextSecondary
        },
        animationSpec = tween(150),
        label = "tabText"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .border(
                BorderStroke(
                    width = 1.dp,
                    color = if (actualFocus) Color.White else Color(0x14FFFFFF)
                ),
                shape = RoundedCornerShape(14.dp)
            )
            .focusRequester(focusRequester)
            .focusable(interactionSource = interactionSource)
            .clickable { onClick() }
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.key) {
                        Key.DirectionRight, Key.Enter, Key.NumPadEnter, Key.DirectionCenter -> {
                            onRight()
                            true
                        }
                        else -> false
                    }
                } else false
            }
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Text(
            text = title,
            color = textColor,
            fontSize = 15.sp,
            fontWeight = if (isSelected || actualFocus) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// =============================================================================
// SETTING CARD COMPOSABLE (Right Column)
// =============================================================================
@Composable
private fun SettingCardItem(
    item: SettingItem,
    isFocused: Boolean,
    focusRequester: FocusRequester?,
    onFocused: () -> Unit,
    onClick: () -> Unit,
    onLeft: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val actualFocus by interactionSource.collectIsFocusedAsState()

    LaunchedEffect(actualFocus) {
        if (actualFocus) onFocused()
    }

    val scale by animateFloatAsState(
        targetValue = if (actualFocus) 1.02f else 1.0f,
        label = "itemScale"
    )

    val bg by animateColorAsState(
        targetValue = if (actualFocus) Color.White else TaloCardBackground,
        animationSpec = tween(150),
        label = "itemBg"
    )

    val titleColor by animateColorAsState(
        targetValue = if (actualFocus) Color(0xFF0A0A0C) else TaloTextPrimary,
        animationSpec = tween(150),
        label = "itemTitleColor"
    )

    val valColor by animateColorAsState(
        targetValue = when {
            actualFocus -> Color(0xFF2C2C2E)
            item.isAction -> TaloAccentBlue
            else -> TaloTextSecondary
        },
        animationSpec = tween(150),
        label = "itemValColor"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(
                BorderStroke(
                    width = 1.dp,
                    color = if (actualFocus) Color.White else TaloCardBorder
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .then(
                if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier
            )
            .focusable(interactionSource = interactionSource)
            .clickable { onClick() }
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.key) {
                        Key.DirectionLeft, Key.Back -> {
                            onLeft()
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
            .padding(horizontal = 24.dp, vertical = 18.dp)
    ) {
        Text(
            text = item.title,
            color = titleColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )

        val displayText = item.actionText ?: item.value
        if (displayText != null) {
            Text(
                text = displayText,
                color = valColor,
                fontSize = 15.sp,
                fontWeight = if (item.isAction) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}
