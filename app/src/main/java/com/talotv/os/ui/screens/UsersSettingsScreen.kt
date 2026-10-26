package com.talotv.os.ui.screens

import android.accounts.AccountManager
import android.content.Intent
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.talotv.os.core.profile.UserProfile
import com.talotv.os.core.profile.UserProfileManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 3 Sous-onglets de la section Comptes et profils
 */
enum class UsersSubTab(val title: String) {
    PROFILES("Profils Talo OS"),
    PARENTAL("Contrôle parental"),
    ACCOUNTS("Comptes et Cloud Vault")
}

@Composable
fun UsersSettingsScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val soundManager = remember { TaloApplication.instance.soundEffectManager }
    val userProfileManager = remember { UserProfileManager(context) }
    val coroutineScope = rememberCoroutineScope()

    var selectedSubTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var isDetailPaneActive by remember { mutableStateOf(false) }

    val subTabs = remember { UsersSubTab.values() }
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
                text = "Comptes et profils",
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

                        UsersSubTabItem(
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
                        UsersSubTab.PROFILES -> ProfilesListSubScreen(
                            manager = userProfileManager,
                            firstItemFocusRequester = rightFirstFocusRequester,
                            onReturnLeft = {
                                isDetailPaneActive = false
                                soundManager.playFocusSound()
                                try {
                                    subTabFocusRequesters.getOrNull(selectedSubTabIndex)?.requestFocus()
                                } catch (_: Exception) {}
                            }
                        )
                        UsersSubTab.PARENTAL -> ParentalSubScreen(
                            manager = userProfileManager,
                            firstItemFocusRequester = rightFirstFocusRequester,
                            onReturnLeft = {
                                isDetailPaneActive = false
                                soundManager.playFocusSound()
                                try {
                                    subTabFocusRequesters.getOrNull(selectedSubTabIndex)?.requestFocus()
                                } catch (_: Exception) {}
                            }
                        )
                        UsersSubTab.ACCOUNTS -> AccountsSubScreen(
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
private fun UsersSubTabItem(
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
        label = "userSubTabScale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isFocused -> Color.White
            isSelected -> Color(0x35FFFFFF)
            else -> Color(0x18FFFFFF)
        },
        animationSpec = tween(120),
        label = "userSubTabBg"
    )

    val contentColor by animateColorAsState(
        targetValue = when {
            isFocused -> Color(0xFF0B0C0E)
            isSelected -> Color.White
            else -> Color(0x90FFFFFF)
        },
        animationSpec = tween(120),
        label = "userSubTabContent"
    )

    val chevronColor by animateColorAsState(
        targetValue = when {
            isFocused -> Color(0xFF0B0C0E)
            isSelected -> Color(0x90FFFFFF)
            else -> Color(0x40FFFFFF)
        },
        animationSpec = tween(120),
        label = "userSubTabChevron"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isFocused -> Color.White
            isSelected -> Color(0x40FFFFFF)
            else -> Color(0x0EFFFFFF)
        },
        animationSpec = tween(120),
        label = "userSubTabBorder"
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
// 1. PROFILS TALO OS
// =============================================================================

@Composable
private fun ProfilesListSubScreen(
    manager: UserProfileManager,
    firstItemFocusRequester: FocusRequester,
    onReturnLeft: () -> Unit
) {
    val soundManager = remember { TaloApplication.instance.soundEffectManager }
    val allProfiles by manager.profiles.collectAsState()
    val activeProfile by manager.activeProfile.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
    ) {
        item {
            UsersActionCard(
                title = "Créer un nouveau profil",
                description = "Ajouter un membre du foyer avec ses propres recommandations personnalisées et son historique de visionnage.",
                badgeText = "Ajouter",
                badgeColor = Color(0xFF34C759),
                focusRequester = firstItemFocusRequester,
                onReturnLeft = onReturnLeft,
                onClick = {
                    soundManager.playSelectSound()
                    val newIndex = allProfiles.size + 1
                    manager.addProfile(
                        name = "Membre $newIndex",
                        ageTier = "18 - 24",
                        colorHex = 0xFF5856D6,
                        pin = ""
                    )
                }
            )
        }

        items(allProfiles) { profile ->
            val isActive = profile.id == activeProfile.id
            UsersActionCard(
                title = profile.name,
                description = "Tranche d'âge : ${profile.ageTier} • ${if (profile.hasPinLock) "Verrouillé par code PIN" else "Accès libre"}${if (profile.isPrimary) " (Profil Administrateur)" else ""}",
                badgeText = if (isActive) "Actif" else "Bascule",
                badgeColor = if (isActive) Color(0xFF34C759) else Color(0x35FFFFFF),
                onReturnLeft = onReturnLeft,
                onClick = {
                    soundManager.playSelectSound()
                    manager.switchProfile(profile)
                }
            )
        }
    }
}

// =============================================================================
// 2. CONTRÔLE PARENTAL
// =============================================================================

@Composable
private fun ParentalSubScreen(
    manager: UserProfileManager,
    firstItemFocusRequester: FocusRequester,
    onReturnLeft: () -> Unit
) {
    val soundManager = remember { TaloApplication.instance.soundEffectManager }
    val allProfiles by manager.profiles.collectAsState()
    val kidsProfile = allProfiles.find { it.hasPinLock || it.name.lowercase().contains("enfant") }

    val screenTimeLimits = listOf("Illimité", "30 minutes", "1 heure", "2 heures")
    var selectedScreenTimeIndex by rememberSaveable { mutableIntStateOf(0) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
    ) {
        item {
            UsersActionCard(
                title = "Code PIN de sécurité",
                description = "Protège les profils adultes et empêche l'accès aux contenus restreints depuis un profil junior. Code PIN à 4 chiffres.",
                badgeText = if (kidsProfile?.hasPinLock == true) "PIN Activé" else "Désactivé",
                badgeColor = if (kidsProfile?.hasPinLock == true) Color(0xFF34C759) else Color(0x35FFFFFF),
                focusRequester = firstItemFocusRequester,
                onReturnLeft = onReturnLeft,
                onClick = {
                    soundManager.playSelectSound()
                }
            )
        }

        item {
            UsersActionCard(
                title = "Classification maximale autorisée",
                description = "Limite l'affichage des affiches et bandes-annonces aux catégories Tout Public et -10 ans sur les profils enfants.",
                badgeText = "Tout Public / -10",
                badgeColor = Color(0x28FFFFFF),
                onReturnLeft = onReturnLeft
            )
        }

        item {
            UsersActionCard(
                title = "Temps d'écran quotidien",
                description = "Fixe une durée maximale d'utilisation quotidienne pour les profils enfants avant verrouillage par code PIN.",
                badgeText = screenTimeLimits[selectedScreenTimeIndex],
                badgeColor = if (selectedScreenTimeIndex > 0) Color(0xFF34C759) else Color(0x35FFFFFF),
                onReturnLeft = onReturnLeft,
                onClick = {
                    soundManager.playSelectSound()
                    selectedScreenTimeIndex = (selectedScreenTimeIndex + 1) % screenTimeLimits.size
                }
            )
        }

        item {
            UsersActionCard(
                title = "Restriction d'achat et d'installation",
                description = "Exige systématiquement le code PIN administrateur avant tout téléchargement d'application sur le Store.",
                badgeText = "Verrouillé",
                badgeColor = Color(0x28FFFFFF),
                onReturnLeft = onReturnLeft
            )
        }
    }
}

// =============================================================================
// 3. COMPTES ET CLOUD VAULT
// =============================================================================

@Composable
private fun AccountsSubScreen(
    firstItemFocusRequester: FocusRequester,
    onReturnLeft: () -> Unit
) {
    val context = LocalContext.current
    val soundManager = remember { TaloApplication.instance.soundEffectManager }

    val accounts = remember {
        try {
            val am = AccountManager.get(context)
            am.accounts.map { it.name }
        } catch (e: Exception) {
            emptyList()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
    ) {
        item {
            UsersActionCard(
                title = "Gérer les comptes système",
                description = "Gérer vos comptes de messagerie, services de streaming et synchronisation d'applications.",
                badgeText = "Gérer",
                badgeColor = Color(0xFF34C759),
                focusRequester = firstItemFocusRequester,
                onReturnLeft = onReturnLeft,
                onClick = {
                    soundManager.playSelectSound()
                    try {
                        val intent = Intent(Settings.ACTION_SYNC_SETTINGS)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    } catch (_: Exception) {}
                }
            )
        }

        if (accounts.isNotEmpty()) {
            items(accounts) { accountName ->
                UsersActionCard(
                    title = accountName,
                    description = "Compte synchronisé sur cet appareil Talo OS.",
                    badgeText = "Connecté",
                    badgeColor = Color(0x35FFFFFF),
                    onReturnLeft = onReturnLeft
                )
            }
        }

        item {
            UsersActionCard(
                title = "Sauvegarde Cloud Vault Talo",
                description = "Vos profils, dispositions d'applications favorites et préférences sont chiffrés sur le matériel local et synchronisés en toute sécurité.",
                badgeText = "Chiffré AES",
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
private fun UsersActionCard(
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
        label = "userCardScale"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isFocused) Color(0xFF2C303A) else Color(0x20FFFFFF),
        animationSpec = tween(120),
        label = "userCardBg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isFocused) Color.White else Color(0x14FFFFFF),
        animationSpec = tween(120),
        label = "userCardBorder"
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
