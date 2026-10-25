package com.talotv.os.ui.components

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.talotv.os.TaloApplication
import com.talotv.os.core.apps.InstalledApp
import com.talotv.os.core.media.DockIconType

@Composable
fun FullAppGrid(
    apps: List<InstalledApp>,
    onAppSelected: (InstalledApp) -> Unit,
    onNavigateLeftFromFirstColumn: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (apps.isEmpty()) return

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 44.dp)
    ) {
        // Chunk apps into rows of 5 (Apple tvOS 5-column full-width grid layout)
        val rows = apps.chunked(5)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            rows.forEachIndexed { rowIndex, rowApps ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    rowApps.forEachIndexed { colIndex, app ->
                        GridAppTile(
                            app = app,
                            onNavigateLeft = if (colIndex == 0) onNavigateLeftFromFirstColumn else null,
                            onClick = { onAppSelected(app) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GridAppTile(
    app: InstalledApp,
    onNavigateLeft: (() -> Unit)? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    LaunchedEffect(isFocused) {
        if (isFocused) {
            TaloApplication.instance.soundEffectManager.playFocusSound()
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.12f else 1.0f,
        animationSpec = tween(durationMillis = 180),
        label = "GridAppScale"
    )

    val elevation by animateFloatAsState(
        targetValue = if (isFocused) 22f else 0f,
        animationSpec = tween(durationMillis = 180),
        label = "GridAppElevation"
    )

    // In Apple tvOS, app names only appear under the focused tile
    val labelAlpha by animateFloatAsState(
        targetValue = if (isFocused) 1f else 0f,
        animationSpec = tween(durationMillis = 150),
        label = "GridLabelAlpha"
    )

    val cornerRadius = 22.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .scale(scale)
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
    ) {
        // Squircle App Tile (Full-width Apple tvOS 5-column proportions: 158dp x 100dp)
        Box(
            modifier = Modifier
                .shadow(
                    elevation = elevation.dp,
                    shape = RoundedCornerShape(cornerRadius),
                    ambientColor = Color.Black.copy(alpha = 0.5f),
                    spotColor = Color.Black.copy(alpha = 0.7f)
                )
                .size(width = 158.dp, height = 100.dp)
                .clip(RoundedCornerShape(cornerRadius))
                .border(
                    BorderStroke(
                        width = if (isFocused) 3.dp else 1.dp,
                        color = if (isFocused) Color.White else Color(0x22FFFFFF)
                    ),
                    RoundedCornerShape(cornerRadius)
                )
                .background(
                    if (app.isStore) Brush.verticalGradient(listOf(Color(0xFF2C2C2E), Color(0xFF1C1C1E)))
                    else if (app.isSettings) Brush.verticalGradient(listOf(Color.White, Color.White))
                    else Brush.verticalGradient(listOf(Color(0xFF242632), Color(0xFF161720)))
                ),
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
            } else if (app.isSettings) {
                AppleTvAppIcon(iconType = DockIconType.SETTINGS, size = 62.dp)
            } else if (app.isStore) {
                AppleTvAppIcon(iconType = DockIconType.STORE, size = 46.dp)
            } else {
                AppleTvAppIcon(iconType = DockIconType.TV, size = 46.dp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // App Label below: ONLY visible when focused, exactly matching Apple tvOS!
        Text(
            text = app.name,
            color = Color.White.copy(alpha = labelAlpha),
            fontSize = 13.5.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.size(width = 158.dp, height = 20.dp)
        )
    }
}
