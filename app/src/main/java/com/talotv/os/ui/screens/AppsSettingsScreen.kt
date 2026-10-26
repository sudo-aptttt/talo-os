package com.talotv.os.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
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
import com.talotv.os.core.apps.InstalledApp
import kotlinx.coroutines.delay

/**
 * 3 Sous-onglets de la section Applications
 */
enum class AppsSubTab(val title: String) {
    INSTALLED("Applications installées"),
    STORE("Obtenir des applications"),
    PERMISSIONS("Autorisations et vie privée")
}

@Composable
fun AppsSettingsScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val soundManager = remember { TaloApplication.instance.soundEffectManager }
    val appsManager = remember { TaloApplication.instance.installedAppsManager }

    var selectedSubTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var isDetailPaneActive by remember { mutableStateOf(false) }

    val subTabs = remember { AppsSubTab.values() }
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
                        text = "Applications",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Gestion des applications installées, magasin Google Play et autorisations",
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
                        AppsSubTabItem(
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
                        AppsSubTab.INSTALLED -> InstalledAppsSubScreen(
                            firstItemFocusRequester = rightFirstFocusRequester,
                            onReturnLeft = {
                                isDetailPaneActive = false
                                try {
                                    subTabFocusRequesters.getOrNull(selectedSubTabIndex)?.requestFocus()
                                } catch (_: Exception) {}
                            }
                        )
                        AppsSubTab.STORE -> StoreSubScreen(
                            firstItemFocusRequester = rightFirstFocusRequester,
                            onReturnLeft = {
                                isDetailPaneActive = false
                                try {
                                    subTabFocusRequesters.getOrNull(selectedSubTabIndex)?.requestFocus()
                                } catch (_: Exception) {}
                            }
                        )
                        AppsSubTab.PERMISSIONS -> AppPermissionsSubScreen(
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
private fun AppsSubTabItem(
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
        label = "appsSubTabScale"
    )

    val bgColor by animateColorAsState(
        targetValue = when {
            isFocused -> Color(0xFF2C303A)
            isSelected && isDetailPaneActive -> Color(0x30FFFFFF)
            isSelected -> Color(0x22FFFFFF)
            else -> Color(0x10FFFFFF)
        },
        animationSpec = tween(120),
        label = "appsSubTabBg"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isFocused -> Color.White
            isSelected && isDetailPaneActive -> Color(0x50FFFFFF)
            isSelected -> Color(0x25FFFFFF)
            else -> Color(0x0CFFFFFF)
        },
        animationSpec = tween(120),
        label = "appsSubTabBorder"
    )

    val textColor by animateColorAsState(
        targetValue = when {
            isFocused -> Color.White
            isSelected -> Color.White
            else -> Color(0x99FFFFFF)
        },
        animationSpec = tween(120),
        label = "appsSubTabText"
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
// 1. APPLICATIONS INSTALLÉES
// =============================================================================

@Composable
private fun InstalledAppsSubScreen(
    firstItemFocusRequester: FocusRequester,
    onReturnLeft: () -> Unit
) {
    val context = LocalContext.current
    val soundManager = remember { TaloApplication.instance.soundEffectManager }
    val appsManager = remember { TaloApplication.instance.installedAppsManager }
    val apps = remember { appsManager.reloadInstalledApps().filter { it.packageName != context.packageName } }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
    ) {
        item {
            AppsActionCard(
                title = "Total des applications",
                description = "${apps.size} applications installées prêtes à l'emploi sur Talo OS. Cliquez sur une application pour l'ouvrir ou gérer son stockage.",
                badgeText = "${apps.size} Apps",
                badgeColor = Color(0x35FFFFFF),
                focusRequester = firstItemFocusRequester,
                onReturnLeft = onReturnLeft
            )
        }

        items(apps) { app ->
            AppsActionCard(
                title = app.name,
                description = "Identifiant de package : ${app.packageName}\nStatut : Opérationnelle",
                badgeText = "Ouvrir",
                badgeColor = Color(0xFF34C759),
                onReturnLeft = onReturnLeft,
                onClick = {
                    soundManager.playSelectSound()
                    appsManager.launchPackage(context, app.packageName, app.name)
                }
            )
        }
    }
}

// =============================================================================
// 2. OBTENIR DES APPLICATIONS
// =============================================================================

@Composable
private fun StoreSubScreen(
    firstItemFocusRequester: FocusRequester,
    onReturnLeft: () -> Unit
) {
    val context = LocalContext.current
    val soundManager = remember { TaloApplication.instance.soundEffectManager }
    val appsManager = remember { TaloApplication.instance.installedAppsManager }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
    ) {
        item {
            AppsActionCard(
                title = "Magasin Google Play Store TV",
                description = "Téléchargez des milliers d'applications de streaming (Netflix, Prime Video, Disney+, Canal+, Max, YouTube), de jeux vidéo et d'utilitaires certifiés Android TV.",
                badgeText = "Ouvrir le Store",
                badgeColor = Color(0xFF34C759),
                focusRequester = firstItemFocusRequester,
                onReturnLeft = onReturnLeft,
                onClick = {
                    soundManager.playSelectSound()
                    appsManager.launchStore(context)
                }
            )
        }

        item {
            AppsActionCard(
                title = "Mises à jour automatiques des applications",
                description = "Vos applications se mettent à jour automatiquement en arrière-plan lorsque le téléviseur est en veille pour vous garantir la meilleure sécurité.",
                badgeText = "Actif",
                badgeColor = Color(0x28FFFFFF),
                onReturnLeft = onReturnLeft
            )
        }
    }
}

// =============================================================================
// 3. AUTORISATIONS ET VIE PRIVÉE
// =============================================================================

@Composable
private fun AppPermissionsSubScreen(
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
            AppsActionCard(
                title = "Gestionnaire d'autorisations Android",
                description = "Consulter et révoquer les accès accordés aux applications tierces (Microphone pour la recherche vocale, Stockage, Bluetooth).",
                badgeText = "Gérer",
                badgeColor = Color(0x35FFFFFF),
                focusRequester = firstItemFocusRequester,
                onReturnLeft = onReturnLeft,
                onClick = {
                    soundManager.playSelectSound()
                    try {
                        val intent = Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    } catch (_: Exception) {}
                }
            )
        }

        item {
            AppsActionCard(
                title = "Protection des données Talo OS (Zéro Télémétrie)",
                description = "Vos habitudes de visionnage restent strictement locales sur la puce de votre téléviseur. Aucune donnée d'audience n'est vendue ni transmise.",
                badgeText = "Protégé",
                badgeColor = Color(0xFF34C759),
                onReturnLeft = onReturnLeft
            )
        }
    }
}

// =============================================================================
// COMPOSANT CARTE HARMONISÉ
// =============================================================================

@Composable
private fun AppsActionCard(
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
        label = "appsCardScale"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isFocused) Color(0xFF2C303A) else Color(0x20FFFFFF),
        animationSpec = tween(120),
        label = "appsCardBg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isFocused) Color.White else Color(0x14FFFFFF),
        animationSpec = tween(120),
        label = "appsCardBorder"
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
