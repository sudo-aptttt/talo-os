package com.talotv.os.ui.components

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.talotv.os.TaloApplication
import com.talotv.os.core.profile.UserProfile
import com.talotv.os.ui.theme.AppleGlassBackground
import com.talotv.os.ui.theme.AppleGlassBorder
import com.talotv.os.ui.theme.AppleGreen
import com.talotv.os.ui.theme.AppleTextPrimary
import com.talotv.os.ui.theme.AppleTextSecondary

@Composable
fun ProfileSwitcherDialog(
    profiles: List<UserProfile>,
    activeProfile: UserProfile,
    onSelectProfile: (UserProfile) -> Unit,
    onAddProfileClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Apple TV Floating Frosted Glass Card (matching User Image 4)
    Box(
        modifier = modifier
            .width(360.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xE61C1D24))
            .border(BorderStroke(1.2.dp, AppleGlassBorder), RoundedCornerShape(28.dp))
            .padding(24.dp)
    ) {
        Column {
            // Header with Back Arrow
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onDismiss() }
                    .padding(vertical = 4.dp, horizontal = 6.dp)
            ) {
                Text(
                    text = "‹",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Light
                )
                Text(
                    text = "Changer d'utilisateur",
                    color = AppleTextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2x2 Grid of User Avatars
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                items(profiles) { profile ->
                    val isActive = profile.id == activeProfile.id
                    ProfileAvatarItem(
                        profile = profile,
                        isActive = isActive,
                        onClick = {
                            TaloApplication.instance.soundEffectManager.playSelectSound()
                            onSelectProfile(profile)
                        }
                    )
                }

                // Add Profile Item ("+ Ajouter un profil")
                item {
                    AddProfileItem(
                        onClick = {
                            TaloApplication.instance.soundEffectManager.playSelectSound()
                            onAddProfileClick()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileAvatarItem(
    profile: UserProfile,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(if (isFocused) 1.08f else 1.0f)
            .clip(RoundedCornerShape(16.dp))
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .focusable(interactionSource = interactionSource)
            .padding(6.dp)
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            // Avatar Circle with Custom Color from Web Account
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(Color(profile.avatarColorHex))
                    .border(
                        BorderStroke(
                            width = if (isFocused) 3.5.dp else if (isActive) 2.dp else 1.dp,
                            color = if (isFocused) Color.White else if (isActive) AppleGreen else Color(0x33FFFFFF)
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = profile.name.take(1).uppercase(),
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Green Checkmark Badge for Active Profile (matching User Image 4)
            if (isActive) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(AppleGreen)
                        .border(BorderStroke(2.dp, Color(0xFF1C1D24)), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = profile.name,
            color = if (isFocused) Color.White else AppleTextSecondary,
            fontSize = 13.5.sp,
            fontWeight = if (isActive || isFocused) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun AddProfileItem(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(if (isFocused) 1.08f else 1.0f)
            .clip(RoundedCornerShape(16.dp))
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .focusable(interactionSource = interactionSource)
            .padding(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(Color(0x33FFFFFF))
                .border(
                    BorderStroke(
                        width = if (isFocused) 3.5.dp else 1.5.dp,
                        color = if (isFocused) Color.White else Color(0x44FFFFFF)
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Light
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Nouveau profil",
            color = if (isFocused) Color.White else AppleTextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
