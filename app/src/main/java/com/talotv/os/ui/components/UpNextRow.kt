package com.talotv.os.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.talotv.os.TaloApplication
import com.talotv.os.core.media.ContentRepository
import com.talotv.os.core.media.UpNextItem
import com.talotv.os.ui.theme.TaloTextPrimary
import com.talotv.os.ui.theme.TaloTextSecondary

@Composable
fun UpNextRow(
    items: List<UpNextItem> = ContentRepository.upNextList,
    onItemSelected: (UpNextItem) -> Unit,
    onNavigateLeftFromFirst: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "À suivre",
            color = TaloTextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 56.dp, bottom = 14.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(horizontal = 56.dp)
        ) {
            items(items.size) { index ->
                val item = items[index]
                UpNextCard(
                    item = item,
                    onNavigateLeft = if (index == 0) onNavigateLeftFromFirst else null,
                    onClick = { onItemSelected(item) }
                )
            }
        }
    }
}

@Composable
fun UpNextCard(
    item: UpNextItem,
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
        targetValue = if (isFocused) 1.08f else 1.0f,
        animationSpec = tween(durationMillis = 180),
        label = "UpNextScale"
    )

    val cornerRadius = 18.dp

    Column(
        modifier = modifier
            .scale(scale)
            .width(260.dp)
            .clip(RoundedCornerShape(cornerRadius))
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
        // 16:9 Thumbnail Box
        Box(
            modifier = Modifier
                .width(260.dp)
                .height(146.dp)
                .clip(RoundedCornerShape(cornerRadius))
                .border(
                    BorderStroke(
                        width = if (isFocused) 3.dp else 1.dp,
                        color = if (isFocused) Color.White else Color(0x22FFFFFF)
                    ),
                    RoundedCornerShape(cornerRadius)
                )
                .background(
                    Brush.verticalGradient(
                        colors = item.gradientColors.map { Color(it) }
                    )
                ),
            contentAlignment = Alignment.BottomStart
        ) {
            // Atmospheric gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                        )
                    )
            )

            // Play / Continue Badge in bottom right
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                TaloAppIcon(iconType = com.talotv.os.core.media.DockIconType.TV, size = 16.dp)
            }

            // In-Progress Bar (matching CODA card on User Image 1)
            if (item.progress != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                        .fillMaxWidth()
                        .height(3.5.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(alpha = 0.35f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(item.progress)
                            .height(3.5.dp)
                            .background(Color.White)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Titles below Card
        Text(
            text = item.title,
            color = if (isFocused) Color.White else TaloTextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = item.subtitle,
            color = TaloTextSecondary,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Normal
        )
    }
}
