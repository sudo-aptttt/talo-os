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
fun TaloAppIcon(
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
            DockIconType.FITNESS -> FitnessAppIcon(size = size)
            DockIconType.CALLS -> VideoCallAppIcon(size = size)
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
            DockIconType.STORE -> StoreAppIcon(size = size)
        }
    }
}

@Composable
fun TvAppIcon(size: Dp, tint: Color = Color.White) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Talo TV House Silhouette
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
fun FitnessAppIcon(size: Dp) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = w * 0.09f

        // Dumbbell / Fitness Graphic
        // Left Weight
        drawRoundRect(
            color = Color(0xFF34C759),
            topLeft = Offset(w * 0.20f, h * 0.30f),
            size = Size(w * 0.12f, h * 0.40f),
            cornerRadius = CornerRadius(w * 0.04f, w * 0.04f)
        )

        // Right Weight
        drawRoundRect(
            color = Color(0xFF34C759),
            topLeft = Offset(w * 0.68f, h * 0.30f),
            size = Size(w * 0.12f, h * 0.40f),
            cornerRadius = CornerRadius(w * 0.04f, w * 0.04f)
        )

        // Center Bar
        drawLine(
            color = Color.White,
            start = Offset(w * 0.30f, h * 0.50f),
            end = Offset(w * 0.70f, h * 0.50f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun VideoCallAppIcon(size: Dp) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Video Camera body
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(w * 0.18f, h * 0.28f),
            size = Size(w * 0.44f, h * 0.44f),
            cornerRadius = CornerRadius(w * 0.08f, w * 0.08f)
        )

        // Camera lens triangle
        val trianglePath = Path().apply {
            moveTo(w * 0.65f, h * 0.38f)
            lineTo(w * 0.82f, h * 0.28f)
            lineTo(w * 0.82f, h * 0.72f)
            lineTo(w * 0.65f, h * 0.62f)
            close()
        }
        drawPath(trianglePath, color = Color.White)
    }
}

@Composable
fun MusicNoteIcon(size: Dp) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = w * 0.09f

        // Note 1 Head
        drawCircle(
            color = Color.White,
            radius = w * 0.12f,
            center = Offset(w * 0.32f, h * 0.70f)
        )
        // Note 2 Head
        drawCircle(
            color = Color.White,
            radius = w * 0.12f,
            center = Offset(w * 0.70f, h * 0.60f)
        )

        // Left Stem
        drawLine(
            color = Color.White,
            start = Offset(w * 0.41f, h * 0.70f),
            end = Offset(w * 0.41f, h * 0.28f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )

        // Right Stem
        drawLine(
            color = Color.White,
            start = Offset(w * 0.79f, h * 0.60f),
            end = Offset(w * 0.79f, h * 0.20f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )

        // Top Connecting Beam
        drawLine(
            color = Color.White,
            start = Offset(w * 0.40f, h * 0.28f),
            end = Offset(w * 0.80f, h * 0.20f),
            strokeWidth = stroke * 1.5f,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun DisneyPlusIcon(size: Dp) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Disney Arc Swirl
        drawArc(
            color = Color.White,
            startAngle = 180f,
            sweepAngle = 120f,
            useCenter = false,
            topLeft = Offset(w * 0.15f, h * 0.20f),
            size = Size(w * 0.7f, h * 0.65f),
            style = Stroke(width = w * 0.08f, cap = StrokeCap.Round)
        )

        // Plus symbol
        val plusX = w * 0.75f
        val plusY = h * 0.40f
        val arm = w * 0.10f
        val stroke = w * 0.08f

        drawLine(
            color = Color(0xFF1CD6F6),
            start = Offset(plusX - arm, plusY),
            end = Offset(plusX + arm, plusY),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color(0xFF1CD6F6),
            start = Offset(plusX, plusY - arm),
            end = Offset(plusX, plusY + arm),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun NetflixRibbonIcon(size: Dp) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val colW = w * 0.16f
        val redPrimary = Color(0xFFE50914)
        val redShadow = Color(0xFF9E060E)

        // Left vertical leg
        drawRect(
            color = redShadow,
            topLeft = Offset(w * 0.25f, h * 0.18f),
            size = Size(colW, h * 0.64f)
        )

        // Right vertical leg
        drawRect(
            color = redShadow,
            topLeft = Offset(w * 0.59f, h * 0.18f),
            size = Size(colW, h * 0.64f)
        )

        // Center diagonal ribbon (brighter, casting shadow)
        val diagPath = Path().apply {
            moveTo(w * 0.25f, h * 0.18f)
            lineTo(w * 0.25f + colW, h * 0.18f)
            lineTo(w * 0.59f + colW, h * 0.82f)
            lineTo(w * 0.59f, h * 0.82f)
            close()
        }
        drawPath(diagPath, color = redPrimary)
    }
}

@Composable
fun PrimeVideoIcon(size: Dp) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Smile Arrow Arc
        drawArc(
            color = Color(0xFF00A8E1),
            startAngle = 20f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(w * 0.16f, h * 0.36f),
            size = Size(w * 0.68f, h * 0.36f),
            style = Stroke(width = w * 0.08f, cap = StrokeCap.Round)
        )

        // Smile Arrowhead
        val arrowPath = Path().apply {
            moveTo(w * 0.75f, h * 0.52f)
            lineTo(w * 0.86f, h * 0.58f)
            lineTo(w * 0.80f, h * 0.44f)
            close()
        }
        drawPath(arrowPath, color = Color(0xFF00A8E1))
    }
}

@Composable
fun YouTubeAppIcon(size: Dp) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Red Rounded Screen Badge
        drawRoundRect(
            color = Color(0xFFFF0000),
            topLeft = Offset(w * 0.15f, h * 0.26f),
            size = Size(w * 0.70f, h * 0.48f),
            cornerRadius = CornerRadius(w * 0.14f, w * 0.14f)
        )

        // Centered White Triangle Play Button
        val playPath = Path().apply {
            moveTo(w * 0.44f, h * 0.38f)
            lineTo(w * 0.62f, h * 0.50f)
            lineTo(w * 0.44f, h * 0.62f)
            close()
        }
        drawPath(playPath, color = Color.White)
    }
}

@Composable
fun OqeeTvIcon(size: Dp) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // OQEE Vibrant Magenta/Red Dual Rings
        drawCircle(
            color = Color(0xFFE20040),
            radius = w * 0.34f,
            center = Offset(w * 0.40f, h * 0.50f),
            style = Stroke(width = w * 0.10f)
        )
        drawCircle(
            color = Color.White,
            radius = w * 0.22f,
            center = Offset(w * 0.62f, h * 0.50f),
            style = Stroke(width = w * 0.08f)
        )
    }
}

@Composable
fun SettingsGearIcon(size: Dp, tint: Color = Color(0xFF343538)) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val center = Offset(w * 0.5f, h * 0.5f)
        val radius = w * 0.46f

        val darkGear = tint
        val holeColor = Color(0xFF1E1E22)

        // Outer Gear Teeth Rim
        val numTeeth = 18
        for (i in 0 until numTeeth) {
            val angle = (i * (360f / numTeeth)) * (Math.PI / 180.0)
            val cosA = Math.cos(angle).toFloat()
            val sinA = Math.sin(angle).toFloat()
            val startX = center.x + (radius * 0.78f) * cosA
            val startY = center.y + (radius * 0.78f) * sinA
            val endX = center.x + (radius * 0.98f) * cosA
            val endY = center.y + (radius * 0.98f) * sinA

            drawLine(
                color = darkGear,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = radius * 0.11f,
                cap = StrokeCap.Round
            )
        }

        // Main Solid Outer Wheel
        drawCircle(
            color = darkGear,
            radius = radius * 0.80f,
            center = center
        )

        // Inner Cutout Ring
        drawCircle(
            color = holeColor,
            radius = radius * 0.60f,
            center = center
        )

        // Center Solid Hub
        drawCircle(
            color = darkGear,
            radius = radius * 0.32f,
            center = center
        )

        // Center Hole
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

        // Radial Spokes
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

        // Concentric Mid-Wheel Ring connecting the spokes
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
fun StoreAppIcon(size: Dp) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = w * 0.09f

        // Clean Modern Shopping / Store Bag
        // Bag Body
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(w * 0.22f, h * 0.36f),
            size = Size(w * 0.56f, h * 0.48f),
            cornerRadius = CornerRadius(w * 0.08f, w * 0.08f),
            style = Stroke(width = stroke)
        )

        // Bag Handle (Arch)
        drawArc(
            color = Color.White,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.34f, h * 0.20f),
            size = Size(w * 0.32f, h * 0.32f),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
    }
}
