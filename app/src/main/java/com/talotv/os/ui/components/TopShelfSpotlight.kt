package com.talotv.os.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.talotv.os.core.media.MediaSpotlight
import com.talotv.os.ui.theme.AppleTextPrimary
import com.talotv.os.ui.theme.AppleTextSecondary

@Composable
fun TopShelfSpotlight(
    spotlight: MediaSpotlight,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {

        // Cinematic Backdrop with Atmospheric Crossfade
        Crossfade(
            targetState = spotlight,
            animationSpec = tween(durationMillis = 400),
            label = "SpotlightBackdropCrossfade"
        ) { item ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(item.gradientColors.first()),
                                Color(item.gradientColors.getOrElse(1) { item.gradientColors.first() }),
                                Color.Black
                            ),
                            center = androidx.compose.ui.geometry.Offset(1200f, 400f),
                            radius = 1800f
                        )
                    )
            )
        }

        // Apple Scrim Overlays (Darken bottom for dock legibility, top for floating status bar)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.55f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.55f),
                            Color.Black.copy(alpha = 0.95f)
                        )
                    )
                )
        )

        // Left Scrim for Text Typography Readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.8f),
                            Color.Black.copy(alpha = 0.4f),
                            Color.Transparent
                        ),
                        startX = 0f,
                        endX = 1400f
                    )
                )
        )

        // Spotlight App Title Typography (Clean Apple TV Large Display Title)
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 56.dp, top = 56.dp)
                .fillMaxWidth(0.65f)
        ) {
            Text(
                text = spotlight.title,
                color = AppleTextPrimary,
                fontSize = 52.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1.5).sp,
                lineHeight = 56.sp
            )
        }
    }
}

/**
 * Apple TV Upward Chevron Indicator placed directly above the floating glass dock.
 */
@Composable
fun UpwardChevronIndicator(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(width = 16.dp, height = 8.dp)) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(0f, h)
            lineTo(w * 0.5f, 0f)
            lineTo(w, h)
        }
        drawPath(
            path = path,
            color = Color.White.copy(alpha = 0.55f),
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}
