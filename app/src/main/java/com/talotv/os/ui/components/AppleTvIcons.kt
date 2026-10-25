package com.talotv.os.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.talotv.os.core.media.DockIconType

@Composable
fun AppleTvAppIcon(
    iconType: DockIconType,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    tint: Color? = null
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        when (iconType) {
            DockIconType.TV -> TvAppIcon(size = size, tint = tint ?: Color.White)
            DockIconType.ARCADE -> ArcadeAppIcon(size = size)
            DockIconType.FITNESS -> FitnessRingsIcon(size = size)
            DockIconType.FACETIME -> FaceTimeAppIcon(size = size)
            DockIconType.MUSIC -> MusicNoteIcon(size = size)
            DockIconType.DISNEY -> DisneyPlusIcon(size = size)
            DockIconType.NETFLIX -> NetflixRibbonIcon(size = size)
            DockIconType.PRIME -> PrimeVideoIcon(size = size)
            DockIconType.YOUTUBE -> YouTubeAppIcon(size = size)
            DockIconType.OQEE -> OqeeTvIcon(size = size)
            DockIconType.SETTINGS -> SettingsGearIcon(size = size, tint = tint ?: Color(0xFF343538))
            DockIconType.SEARCH -> SearchGlassIcon(size = size, tint = tint ?: Color.White)
            DockIconType.PHOTOS -> PhotosFlowerIcon(size = size)
            DockIconType.PODCASTS -> PodcastsWaveIcon(size = size)
            DockIconType.STORE -> AppStoreIcon(size = size)
        }
    }
}

@Composable
fun TvAppIcon(size: Dp, tint: Color = Color.White) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Apple / Talo House Silhouette
        val housePath = Path().apply {
            moveTo(w * 0.5f, h * 0.22f)
            lineTo(w * 0.78f, h * 0.44f)
            lineTo(w * 0.78f, h * 0.78f)
            lineTo(w * 0.22f, h * 0.78f)
            lineTo(w * 0.22f, h * 0.44f)
            close()
        }
        drawPath(housePath, color = tint)

        // TV Stand / Antenna accent
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.35f, h * 0.82f),
            size = Size(w * 0.3f, h * 0.06f),
            cornerRadius = CornerRadius(4f, 4f)
        )
    }
}

@Composable
fun ArcadeAppIcon(size: Dp) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Joystick Base & Stick
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(w * 0.2f, h * 0.58f),
            size = Size(w * 0.6f, h * 0.22f),
            cornerRadius = CornerRadius(w * 0.08f, w * 0.08f)
        )

        // Joystick Stick
        drawLine(
            color = Color.White,
            start = Offset(w * 0.5f, h * 0.58f),
            end = Offset(w * 0.5f, h * 0.32f),
            strokeWidth = w * 0.08f,
            cap = StrokeCap.Round
        )

        // Joystick Ball
        drawCircle(
            color = Color.White,
            radius = w * 0.15f,
            center = Offset(w * 0.5f, h * 0.26f)
        )
    }
}

@Composable
fun FitnessRingsIcon(size: Dp) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val center = Offset(w * 0.5f, h * 0.5f)
        val strokeW = w * 0.075f

        // Red Outer Ring (Move)
        drawCircle(
            color = Color(0xFFFA114F),
            radius = w * 0.40f,
            center = center,
            style = Stroke(width = strokeW, cap = StrokeCap.Round)
        )

        // Green Middle Ring (Exercise)
        drawCircle(
            color = Color(0xFFAAFE07),
            radius = w * 0.28f,
            center = center,
            style = Stroke(width = strokeW, cap = StrokeCap.Round)
        )

        // Cyan Inner Ring (Stand)
        drawCircle(
            color = Color(0xFF00F0FF),
            radius = w * 0.16f,
            center = center,
            style = Stroke(width = strokeW, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun FaceTimeAppIcon(size: Dp) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Video Camera Body
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(w * 0.18f, h * 0.3f),
            size = Size(w * 0.44f, h * 0.4f),
            cornerRadius = CornerRadius(w * 0.08f, w * 0.08f)
        )

        // Camera Lens Triangle
        val lensPath = Path().apply {
            moveTo(w * 0.66f, h * 0.38f)
            lineTo(w * 0.84f, h * 0.28f)
            lineTo(w * 0.84f, h * 0.72f)
            lineTo(w * 0.66f, h * 0.62f)
            close()
        }
        drawPath(lensPath, color = Color.White)
    }
}

@Composable
fun MusicNoteIcon(size: Dp) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Music Eighth Note
        val notePath = Path().apply {
            moveTo(w * 0.45f, h * 0.65f)
            lineTo(w * 0.45f, h * 0.22f)
            lineTo(w * 0.75f, h * 0.15f)
            lineTo(w * 0.75f, h * 0.58f)
        }
        drawPath(notePath, color = Color.White, style = Stroke(width = w * 0.08f, cap = StrokeCap.Round))

        // Note Heads
        drawCircle(color = Color.White, radius = w * 0.12f, center = Offset(w * 0.36f, h * 0.68f))
        drawCircle(color = Color.White, radius = w * 0.12f, center = Offset(w * 0.66f, h * 0.62f))
    }
}

@Composable
fun DisneyPlusIcon(size: Dp) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Iconic Disney Swoosh Arc
        drawArc(
            color = Color.White,
            startAngle = 180f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(w * 0.15f, h * 0.22f),
            size = Size(w * 0.7f, h * 0.6f),
            style = Stroke(width = w * 0.08f, cap = StrokeCap.Round)
        )

        // Plus Sign
        drawLine(
            color = Color(0xFF38BDF8),
            start = Offset(w * 0.78f, h * 0.42f),
            end = Offset(w * 0.78f, h * 0.58f),
            strokeWidth = w * 0.06f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color(0xFF38BDF8),
            start = Offset(w * 0.70f, h * 0.50f),
            end = Offset(w * 0.86f, h * 0.50f),
            strokeWidth = w * 0.06f,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun NetflixRibbonIcon(size: Dp) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Netflix 'N' Ribbon
        drawLine(Color.White, Offset(w * 0.3f, h * 0.2f), Offset(w * 0.3f, h * 0.8f), strokeWidth = w * 0.12f, cap = StrokeCap.Square)
        drawLine(Color.White, Offset(w * 0.7f, h * 0.2f), Offset(w * 0.7f, h * 0.8f), strokeWidth = w * 0.12f, cap = StrokeCap.Square)
        drawLine(Color(0xFFE50914), Offset(w * 0.3f, h * 0.2f), Offset(w * 0.7f, h * 0.8f), strokeWidth = w * 0.13f, cap = StrokeCap.Square)
    }
}

@Composable
fun PrimeVideoIcon(size: Dp) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Play Triangle
        val playPath = Path().apply {
            moveTo(w * 0.42f, h * 0.32f)
            lineTo(w * 0.65f, h * 0.48f)
            lineTo(w * 0.42f, h * 0.64f)
            close()
        }
        drawPath(playPath, color = Color.White)

        // Amazon Smile Arc
        drawArc(
            color = Color(0xFF00A8E1),
            startAngle = 20f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(w * 0.25f, h * 0.6f),
            size = Size(w * 0.5f, h * 0.22f),
            style = Stroke(width = w * 0.06f, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun YouTubeAppIcon(size: Dp) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Red Outer Pill
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(w * 0.15f, h * 0.26f),
            size = Size(w * 0.7f, h * 0.48f),
            cornerRadius = CornerRadius(w * 0.14f, w * 0.14f)
        )

        // Red Play Triangle inside
        val playPath = Path().apply {
            moveTo(w * 0.44f, h * 0.38f)
            lineTo(w * 0.60f, h * 0.50f)
            lineTo(w * 0.44f, h * 0.62f)
            close()
        }
        drawPath(playPath, color = Color(0xFFFF0000))
    }
}

@Composable
fun OqeeTvIcon(size: Dp) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        drawCircle(color = Color.White, radius = w * 0.35f, center = Offset(w * 0.5f, h * 0.5f))
        drawCircle(color = Color(0xFFCC0000), radius = w * 0.20f, center = Offset(w * 0.5f, h * 0.5f))
    }
}

@Composable
fun SettingsGearIcon(size: Dp, tint: Color = Color(0xFF343538)) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val center = Offset(w * 0.5f, h * 0.5f)
        val radius = (minOf(w, h) * 0.46f)
        val darkGear = tint
        val holeColor = if (darkGear == Color.White) Color(0xFF161720) else Color.White

        // 1. Outer Fine Gear Teeth (32 interlocking mechanical teeth)
        val numTeeth = 32
        for (i in 0 until numTeeth) {
            val angle = (i * (360f / numTeeth)) * (Math.PI / 180.0)
            val cosA = Math.cos(angle).toFloat()
            val sinA = Math.sin(angle).toFloat()
            val startX = center.x + (radius * 0.88f) * cosA
            val startY = center.y + (radius * 0.88f) * sinA
            val endX = center.x + radius * cosA
            val endY = center.y + radius * sinA

            drawLine(
                color = darkGear,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = radius * 0.055f,
                cap = StrokeCap.Round
            )
        }

        // 2. Outer Solid Rim Ring holding the teeth
        drawCircle(
            color = darkGear,
            radius = radius * 0.88f,
            center = center,
            style = Stroke(width = radius * 0.08f)
        )

        // Outer concentric groove track
        drawCircle(
            color = darkGear.copy(alpha = 0.55f),
            radius = radius * 0.78f,
            center = center,
            style = Stroke(width = radius * 0.035f)
        )

        // 3. Central Solid Mechanical Hub
        drawCircle(
            color = darkGear,
            radius = radius * 0.32f,
            center = center
        )

        // 4. Center Hole (inner aperture showing background)
        drawCircle(
            color = holeColor,
            radius = radius * 0.13f,
            center = center
        )

        // Center axle pin
        drawCircle(
            color = darkGear,
            radius = radius * 0.05f,
            center = center
        )

        // 5. Three Heavy Radial Spokes at 120° (0°, 120°, 240° matching Apple tvOS reference)
        for (i in 0 until 3) {
            val angle = (0f + i * 120f) * (Math.PI / 180.0)
            val cosA = Math.cos(angle).toFloat()
            val sinA = Math.sin(angle).toFloat()
            val startX = center.x + (radius * 0.28f) * cosA
            val startY = center.y + (radius * 0.28f) * sinA
            val endX = center.x + (radius * 0.84f) * cosA
            val endY = center.y + (radius * 0.84f) * sinA

            drawLine(
                color = darkGear,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = radius * 0.15f,
                cap = StrokeCap.Round
            )
        }

        // 6. Concentric Mid-Wheel Ring connecting the spokes
        drawCircle(
            color = darkGear,
            radius = radius * 0.62f,
            center = center,
            style = Stroke(width = radius * 0.05f)
        )
    }
}

@Composable
fun SearchGlassIcon(size: Dp, tint: Color = Color.White) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        drawCircle(
            color = tint,
            radius = w * 0.26f,
            center = Offset(w * 0.42f, h * 0.42f),
            style = Stroke(width = w * 0.08f)
        )
        drawLine(
            color = tint,
            start = Offset(w * 0.60f, h * 0.60f),
            end = Offset(w * 0.82f, h * 0.82f),
            strokeWidth = w * 0.09f,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun PhotosFlowerIcon(size: Dp) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val center = Offset(w * 0.5f, h * 0.5f)
        val petalColors = listOf(
            Color(0xFFFFFFFF), Color(0xFFE5E5EA), Color(0xFFD1D1D6),
            Color(0xFFA1A1A6), Color(0xFF8E8E93), Color(0xFF636366)
        )
        petalColors.forEachIndexed { idx, color ->
            val angle = (idx * 60f) * (Math.PI / 180.0)
            val px = (center.x + (w * 0.18f) * Math.cos(angle)).toFloat()
            val py = (center.y + (h * 0.18f) * Math.sin(angle)).toFloat()
            drawCircle(color = color, radius = w * 0.14f, center = Offset(px, py))
        }
    }
}

@Composable
fun PodcastsWaveIcon(size: Dp) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val center = Offset(w * 0.5f, h * 0.5f)

        drawCircle(color = Color.White, radius = w * 0.1f, center = center)
        drawArc(
            color = Color.White,
            startAngle = 135f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(w * 0.25f, h * 0.25f),
            size = Size(w * 0.5f, h * 0.5f),
            style = Stroke(width = w * 0.07f, cap = StrokeCap.Round)
        )
        drawArc(
            color = Color.White,
            startAngle = 315f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(w * 0.25f, h * 0.25f),
            size = Size(w * 0.5f, h * 0.5f),
            style = Stroke(width = w * 0.07f, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun AppStoreIcon(size: Dp) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = w * 0.11f

        // Iconic crossed sticks of Apple App Store forming an "A"
        // 1. Diagonal Left stick
        drawLine(
            color = Color.White,
            start = Offset(w * 0.22f, h * 0.80f),
            end = Offset(w * 0.50f, h * 0.20f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )

        // 2. Diagonal Right stick
        drawLine(
            color = Color.White,
            start = Offset(w * 0.78f, h * 0.80f),
            end = Offset(w * 0.50f, h * 0.20f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )

        // 3. Horizontal Crossbar stick
        drawLine(
            color = Color.White,
            start = Offset(w * 0.20f, h * 0.62f),
            end = Offset(w * 0.80f, h * 0.62f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}
