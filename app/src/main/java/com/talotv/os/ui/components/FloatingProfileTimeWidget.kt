package com.talotv.os.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.talotv.os.core.profile.UserProfile
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Floating Top-Right Widget for Talo TV layout:
 * - Current Time on the left (HH:mm)
 * - Current Profile Picture (PP) on the right (clickable to switch profile)
 * - D-Pad down navigates to dock, D-Pad left opens sidebar drawer
 */
@Composable
fun FloatingProfileTimeWidget(
    activeProfile: UserProfile,
    onProfileClick: () -> Unit,
    onNavigateDown: (() -> Unit)? = null,
    onNavigateLeft: (() -> Unit)? = null,
    focusRequester: FocusRequester? = null,
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf("09:41") }

    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        while (true) {
            currentTime = timeFormat.format(Date())
            delay(10_000)
        }
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.15f else 1.0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "ProfileAvatarScale"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0x33000000))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        // 1. Current Time on the Left
        Text(
            text = currentTime,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.SansSerif,
            letterSpacing = 0.4.sp,
            style = TextStyle(
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.8f),
                    offset = Offset(0f, 2f),
                    blurRadius = 6f
                )
            )
        )

        // 2. Profile Picture (PP) on the Right
        var boxModifier = Modifier
            .scale(scale)
            .size(36.dp)
            .shadow(
                elevation = if (isFocused) 14.dp else 4.dp,
                shape = CircleShape,
                ambientColor = Color.Black.copy(alpha = 0.6f),
                spotColor = if (isFocused) Color.White.copy(alpha = 0.8f) else Color.Transparent
            )
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(activeProfile.avatarColorHex),
                        Color(activeProfile.avatarColorHex).copy(alpha = 0.8f)
                    )
                )
            )
            .border(
                BorderStroke(
                    width = if (isFocused) 2.dp else 1.dp,
                    color = if (isFocused) Color.White else Color(0x66FFFFFF)
                ),
                CircleShape
            )

        if (focusRequester != null) {
            boxModifier = boxModifier.focusRequester(focusRequester)
        }

        boxModifier = boxModifier
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    val code = event.nativeKeyEvent.keyCode
                    if (event.key == Key.DirectionLeft || code == android.view.KeyEvent.KEYCODE_DPAD_LEFT) {
                        if (onNavigateLeft != null) {
                            onNavigateLeft()
                            return@onKeyEvent true
                        }
                    } else if (event.key == Key.DirectionDown || code == android.view.KeyEvent.KEYCODE_DPAD_DOWN) {
                        if (onNavigateDown != null) {
                            onNavigateDown()
                            return@onKeyEvent true
                        }
                    }
                }
                false
            }
            .focusable(interactionSource = interactionSource)
            .clickable { onProfileClick() }

        Box(
            modifier = boxModifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = activeProfile.name.take(1).uppercase(),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.5f),
                        offset = Offset(0f, 1f),
                        blurRadius = 3f
                    )
                )
            )
        }
    }
}
