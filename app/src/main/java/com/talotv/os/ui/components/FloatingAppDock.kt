package com.talotv.os.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import com.talotv.os.TaloApplication
import com.talotv.os.core.media.ContentRepository
import com.talotv.os.core.media.DockAppItem
import com.talotv.os.core.media.DockIconType
import com.talotv.os.ui.theme.TaloGlassBackground
import com.talotv.os.ui.theme.TaloGlassBorder

@Composable
fun FloatingAppDock(
    apps: List<DockAppItem> = ContentRepository.dockApps,
    firstItemFocusRequester: FocusRequester? = null,
    onNavigateLeftFromFirst: (() -> Unit)? = null,
    onNavigateUpToProfile: (() -> Unit)? = null,
    onAppFocused: (DockAppItem) -> Unit,
    onAppSelected: (DockAppItem) -> Unit,
    modifier: Modifier = Modifier
) {
    // Talo Floating Glass Capsule Container spanning full width
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(32.dp))
            .background(TaloGlassBackground)
            .border(
                BorderStroke(1.2.dp, TaloGlassBorder),
                RoundedCornerShape(32.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            apps.forEachIndexed { index, app ->
                val isFirst = index == 0
                val isLast = index == apps.lastIndex
                DockAppTile(
                    app = app,
                    focusRequester = if (isFirst) firstItemFocusRequester else null,
                    onNavigateLeft = if (isFirst) onNavigateLeftFromFirst else null,
                    onNavigateUp = if (isLast) onNavigateUpToProfile else null,
                    onFocus = { onAppFocused(app) },
                    onClick = { onAppSelected(app) }
                )
            }
        }
    }
}

@Composable
fun DockAppTile(
    app: DockAppItem,
    focusRequester: FocusRequester? = null,
    onNavigateLeft: (() -> Unit)? = null,
    onNavigateUp: (() -> Unit)? = null,
    onFocus: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    LaunchedEffect(isFocused) {
        if (isFocused) {
            TaloApplication.instance.soundEffectManager.playFocusSound()
            onFocus()
        }
    }

    // Parallax 3D scale animation matching focused state
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.12f else 1.0f,
        animationSpec = tween(durationMillis = 180),
        label = "DockAppScale"
    )

    val elevation by animateFloatAsState(
        targetValue = if (isFocused) 24f else 0f,
        animationSpec = tween(durationMillis = 180),
        label = "DockAppElevation"
    )

    val cornerRadius = 22.dp

    var tileModifier = modifier
        .scale(scale)
        .shadow(
            elevation = elevation.dp,
            shape = RoundedCornerShape(cornerRadius),
            ambientColor = Color.Black.copy(alpha = 0.5f),
            spotColor = Color.Black.copy(alpha = 0.7f)
        )
        .size(width = 158.dp, height = 100.dp)
        .clip(RoundedCornerShape(cornerRadius))
        .border(
            border = BorderStroke(
                width = if (isFocused) 3.dp else 1.dp,
                color = if (isFocused) Color.White else Color(0x22FFFFFF)
            ),
            shape = RoundedCornerShape(cornerRadius)
        )
        .background(
            Brush.verticalGradient(
                colors = if (app.iconType == DockIconType.SETTINGS || app.id == "app-settings" || app.packageName == "com.talotv.settings") {
                    listOf(Color.White, Color.White)
                } else {
                    app.bgGradient.map { Color(it) }
                }
            )
        )

    if (focusRequester != null) {
        tileModifier = tileModifier.focusRequester(focusRequester)
    }

    tileModifier = tileModifier
        .clickable(interactionSource = interactionSource, indication = null) {
            TaloApplication.instance.soundEffectManager.playSelectSound()
            onClick()
        }
        .onKeyEvent { event ->
            if (event.type == KeyEventType.KeyDown) {
                val code = event.nativeKeyEvent.keyCode
                if ((event.key == Key.DirectionLeft || code == android.view.KeyEvent.KEYCODE_DPAD_LEFT) && onNavigateLeft != null) {
                    onNavigateLeft()
                    return@onKeyEvent true
                } else if ((event.key == Key.DirectionUp || code == android.view.KeyEvent.KEYCODE_DPAD_UP) && onNavigateUp != null) {
                    onNavigateUp()
                    return@onKeyEvent true
                } else if (event.key == Key.DirectionCenter || event.key == Key.Enter ||
                    code == android.view.KeyEvent.KEYCODE_DPAD_CENTER || code == android.view.KeyEvent.KEYCODE_ENTER
                ) {
                    TaloApplication.instance.soundEffectManager.playSelectSound()
                    onClick()
                    return@onKeyEvent true
                }
            }
            false
        }
        .focusable(interactionSource = interactionSource)

    Box(
        modifier = tileModifier,
        contentAlignment = Alignment.Center
    ) {
        if (app.iconBitmap != null) {
            if (app.isBanner) {
                Image(
                    bitmap = app.iconBitmap,
                    contentDescription = app.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Image(
                    bitmap = app.iconBitmap,
                    contentDescription = app.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                )
            }
        } else {
            TaloAppIcon(
                iconType = app.iconType,
                size = if (app.iconType == DockIconType.SETTINGS) 58.dp else 46.dp
            )
        }
    }
}
