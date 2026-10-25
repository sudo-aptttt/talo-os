package com.talotv.os.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.runtime.collectAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.talotv.os.TaloApplication
import com.talotv.os.core.media.DockAppItem
import com.talotv.os.core.media.DockIconType
import com.talotv.os.core.profile.UserProfile
import com.talotv.os.ui.theme.AppleGlassBorder
import com.talotv.os.ui.theme.AppleTextPrimary
import com.talotv.os.ui.theme.AppleTextSecondary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class NavDestination {
    SEARCH,
    HOME,
    SETTINGS,
    CINEMA,
    LIVE_TV,
    ARCADE
}

@Composable
fun TvSidebarDrawer(
    currentDestination: NavDestination,
    activeProfile: UserProfile,
    onNavigate: (NavDestination) -> Unit,
    onAppClick: (DockAppItem) -> Unit,
    onProfileClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf("09:41") }
    val sidebarManager = remember { TaloApplication.instance.sidebarAppsManager }
    val installedAppsManager = remember { TaloApplication.instance.installedAppsManager }
    val installedApps by installedAppsManager.installedApps.collectAsState()
    val dockApps = remember(installedApps) { installedAppsManager.getDockApps() }
    val sidebarApps = remember(installedApps, dockApps) {
        sidebarManager.getSidebarApps(installedApps, dockApps)
    }

    // Adapt number of frequent apps based on TV screen height
    val configuration = LocalConfiguration.current
    val maxFrequentApps = when {
        configuration.screenHeightDp <= 560 -> 2 // Standard 1080p Android TV (540dp) & 720p
        configuration.screenHeightDp <= 720 -> 3
        else -> 4
    }
    val displayedApps = remember(sidebarApps, maxFrequentApps) {
        sidebarApps.take(maxFrequentApps)
    }

    val homeFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        while (true) {
            currentTime = timeFormat.format(Date())
            delay(10_000)
        }
    }

    // Auto focus "Accueil" when sidebar opens
    LaunchedEffect(Unit) {
        delay(120)
        try {
            homeFocusRequester.requestFocus()
        } catch (e: Exception) { }
    }

    // Apple tvOS Floating Frosted Glass Sidebar
    Box(
        modifier = modifier
            .width(280.dp)
            .fillMaxHeight()
            .padding(start = 20.dp, top = 16.dp, bottom = 16.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xF2161720))
            .border(BorderStroke(1.2.dp, AppleGlassBorder), RoundedCornerShape(28.dp))
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    val code = event.nativeKeyEvent.keyCode
                    if (event.key == Key.DirectionRight || code == android.view.KeyEvent.KEYCODE_DPAD_RIGHT) {
                        onDismiss()
                        return@onKeyEvent true
                    } else if (event.key == Key.Back || code == android.view.KeyEvent.KEYCODE_BACK) {
                        onDismiss()
                        return@onKeyEvent true
                    }
                }
                false
            }
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ============================================================
            // 1. TOUT EN HAUT : COMPTE AVEC LE NOM ET L'HEURE ACTUELLE À DROITE
            // ============================================================
            val headerInteraction = remember { MutableInteractionSource() }
            val isHeaderFocused by headerInteraction.collectIsFocusedAsState()

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isHeaderFocused) Color(0x33FFFFFF) else Color.Transparent)
                    .border(
                        BorderStroke(1.dp, if (isHeaderFocused) Color.White else Color.Transparent),
                        RoundedCornerShape(14.dp)
                    )
                    .onKeyEvent { event ->
                        if (event.type == KeyEventType.KeyDown) {
                            val code = event.nativeKeyEvent.keyCode
                            if (event.key == Key.DirectionRight || code == android.view.KeyEvent.KEYCODE_DPAD_RIGHT) {
                                onDismiss()
                                return@onKeyEvent true
                            } else if (event.key == Key.DirectionUp || code == android.view.KeyEvent.KEYCODE_DPAD_UP) {
                                onDismiss()
                                return@onKeyEvent true
                            }
                        }
                        false
                    }
                    .clickable(interactionSource = headerInteraction, indication = null) {
                        TaloApplication.instance.soundEffectManager.playSelectSound()
                        onProfileClick()
                    }
                    .focusable(interactionSource = headerInteraction)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                // Photo de Profil (PP) ronde
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(activeProfile.avatarColorHex))
                        .border(BorderStroke(1.dp, Color(0x66FFFFFF)), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = activeProfile.name.take(1).uppercase(),
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Nom du profil
                Text(
                    text = activeProfile.name,
                    color = AppleTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Heure actuelle sur sa droite
                Text(
                    text = currentTime,
                    color = AppleTextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.SansSerif
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Ligne de séparation discrète façon tvOS
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0x18FFFFFF))
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ============================================================
            // 2. EN DESSOUS : BOUTON RECHERCHER
            // ============================================================
            SidebarNavItem(
                title = "Rechercher",
                iconType = DockIconType.SEARCH,
                isSelected = currentDestination == NavDestination.SEARCH,
                onDismiss = onDismiss,
                onClick = { onNavigate(NavDestination.SEARCH) }
            )

            Spacer(modifier = Modifier.height(3.dp))

            // ============================================================
            // 3. EN DESSOUS : LA PAGE HOME (PAGE PRINCIPALE)
            // ============================================================
            SidebarNavItem(
                title = "Accueil",
                iconType = DockIconType.TV,
                isSelected = currentDestination == NavDestination.HOME,
                focusRequester = homeFocusRequester,
                onDismiss = onDismiss,
                onClick = { onNavigate(NavDestination.HOME) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // ============================================================
            // 4. EN DESSOUS : SÉLECTION D'APPS FRÉQUENTES (RÉDUITE SUR PETITS ÉCRANS)
            // ============================================================
            if (displayedApps.isNotEmpty()) {
                Text(
                    text = if (sidebarManager.getSidebarMode() == com.talotv.os.core.media.SidebarAppsManager.MODE_CUSTOM) {
                        "APPLICATIONS SÉLECTIONNÉES"
                    } else {
                        "APPLICATIONS FRÉQUENTES"
                    },
                    color = AppleTextSecondary.copy(alpha = 0.55f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 12.dp, bottom = 6.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    displayedApps.forEach { app ->
                        SidebarAppItem(
                            app = app,
                            onDismiss = onDismiss,
                            onClick = {
                                sidebarManager.recordAppLaunch(app.id)
                                onAppClick(app)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Ligne de séparation au bas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0x18FFFFFF))
            )

            Spacer(modifier = Modifier.height(6.dp))

            // ============================================================
            // 5. EN BAS DU MENU : LA PAGE RÉGLAGES
            // ============================================================
            SidebarNavItem(
                title = "Réglages",
                iconType = DockIconType.SETTINGS,
                isSelected = currentDestination == NavDestination.SETTINGS,
                onDismiss = onDismiss,
                onClick = { onNavigate(NavDestination.SETTINGS) }
            )
        }
    }
}

@Composable
fun SidebarAppItem(
    app: DockAppItem,
    onDismiss: () -> Unit,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.03f else 1.0f,
        label = "SidebarAppScale"
    )

    val bg = if (isFocused) Color(0x33FFFFFF) else Color.Transparent

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(
                BorderStroke(1.dp, if (isFocused) Color(0x88FFFFFF) else Color.Transparent),
                RoundedCornerShape(16.dp)
            )
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    val code = event.nativeKeyEvent.keyCode
                    if (event.key == Key.DirectionRight || code == android.view.KeyEvent.KEYCODE_DPAD_RIGHT) {
                        onDismiss()
                        return@onKeyEvent true
                    }
                }
                false
            }
            .clickable(interactionSource = interactionSource, indication = null) {
                TaloApplication.instance.soundEffectManager.playSelectSound()
                onClick()
            }
            .focusable(interactionSource = interactionSource)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        // App Icon Squircle
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(app.bgGradient.first())),
            contentAlignment = Alignment.Center
        ) {
            if (app.iconBitmap != null) {
                Image(
                    bitmap = app.iconBitmap,
                    contentDescription = app.name,
                    contentScale = if (app.isBanner) ContentScale.Crop else ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp))
                )
            } else {
                AppleTvAppIcon(
                    iconType = app.iconType,
                    size = 18.dp
                )
            }
        }

        Text(
            text = app.name,
            color = Color.White,
            fontSize = 13.5.sp,
            fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SidebarNavItem(
    title: String,
    iconType: DockIconType,
    isSelected: Boolean,
    focusRequester: FocusRequester? = null,
    onNavigateUp: (() -> Unit)? = null,
    onNavigateDown: (() -> Unit)? = null,
    onDismiss: () -> Unit = {},
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val bg = if (isSelected) Color.White else if (isFocused) Color(0x33FFFFFF) else Color.Transparent
    val contentColor = if (isSelected) Color.Black else Color.White

    var rowModifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .background(bg)
        .border(
            BorderStroke(1.dp, if (isFocused && !isSelected) Color(0x88FFFFFF) else Color.Transparent),
            RoundedCornerShape(16.dp)
        )

    if (focusRequester != null) {
        rowModifier = rowModifier.focusRequester(focusRequester)
    }

    rowModifier = rowModifier
        .onKeyEvent { event ->
            if (event.type == KeyEventType.KeyDown) {
                val code = event.nativeKeyEvent.keyCode
                if (event.key == Key.DirectionRight || code == android.view.KeyEvent.KEYCODE_DPAD_RIGHT) {
                    onDismiss()
                    return@onKeyEvent true
                } else if ((event.key == Key.DirectionDown || code == android.view.KeyEvent.KEYCODE_DPAD_DOWN) && onNavigateDown != null) {
                    onNavigateDown()
                    return@onKeyEvent true
                } else if ((event.key == Key.DirectionUp || code == android.view.KeyEvent.KEYCODE_DPAD_UP) && onNavigateUp != null) {
                    onNavigateUp()
                    return@onKeyEvent true
                }
            }
            false
        }
        .clickable(interactionSource = interactionSource, indication = null) {
            TaloApplication.instance.soundEffectManager.playSelectSound()
            onClick()
        }
        .focusable(interactionSource = interactionSource)
        .padding(horizontal = 12.dp, vertical = 8.dp)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = rowModifier
    ) {
        AppleTvAppIcon(
            iconType = iconType,
            size = 20.dp,
            tint = contentColor
        )

        Text(
            text = title,
            color = contentColor,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
