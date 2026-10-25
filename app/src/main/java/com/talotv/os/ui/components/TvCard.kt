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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.talotv.os.TaloApplication
import com.talotv.os.ui.theme.TaloCardBorder
import com.talotv.os.ui.theme.TaloCardBorderFocused
import com.talotv.os.ui.theme.TaloCardGlass
import com.talotv.os.ui.theme.TaloCardGlassFocused

@Composable
fun TvCard(
    title: String,
    subtitle: String? = null,
    gradientColors: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    // Trigger subtle navigation audio click on focus change
    LaunchedEffect(isFocused) {
        if (isFocused) {
            TaloApplication.instance.soundEffectManager.playFocusSound()
        }
    }

    // Scale animation (subtle focus zoom)
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.08f else 1.0f,
        animationSpec = tween(durationMillis = 200),
        label = "TvCardScale"
    )

    val cornerRadius = 18.dp

    Box(
        modifier = modifier
            .scale(scale)
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(cornerRadius))
            .border(
                border = BorderStroke(
                    width = if (isFocused) 3.dp else 1.dp,
                    color = if (isFocused) TaloCardBorderFocused else TaloCardBorder
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
            .background(
                Brush.linearGradient(
                    colors = if (isFocused) {
                        listOf(gradientColors.first().copy(alpha = 0.85f), gradientColors.last().copy(alpha = 0.95f))
                    } else {
                        listOf(gradientColors.first().copy(alpha = 0.45f), gradientColors.last().copy(alpha = 0.6f))
                    }
                )
            )
            .focusable(interactionSource = interactionSource)
            .clickable {
                TaloApplication.instance.soundEffectManager.playSelectSound()
                onClick()
            }
            .padding(16.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isFocused) TaloCardGlassFocused else TaloCardGlass,
                    RoundedCornerShape(12.dp)
                )
                .padding(12.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            androidx.compose.foundation.layout.Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = if (isFocused) FontWeight.Bold else FontWeight.SemiBold
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        color = Color.LightGray.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}
