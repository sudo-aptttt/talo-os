package com.talotv.os.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.talotv.os.core.security.DeviceFingerprint
import com.talotv.os.core.security.TaloSecurityVault
import kotlinx.coroutines.delay

/**
 * Talo OS — Fortress Lost Mode Screen (Mode Perdu Inviolable)
 * Strict Apple Monochrome design system (Black & White).
 * Unbreachable Kiosk Lock: blocks all navigation, menus, and system controls.
 * Adapts to Apple tvOS split layout when emergency keypad is open.
 */
@Composable
fun LostModeScreen(
    lockMessage: String,
    onEmergencyUnlock: () -> Unit
) {
    val context = LocalContext.current

    // Block the hardware back button completely
    BackHandler(enabled = true) {
        // Absolute consumption
    }

    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }
    var showPinKeypad by remember { mutableStateOf(false) }
    var attemptsCount by remember { mutableIntStateOf(0) }
    var lockCoolDownSeconds by remember { mutableIntStateOf(0) }

    val fingerprint = remember { DeviceFingerprint.getFingerprint(context) }

    // Cooldown timer for anti-brute-force
    LaunchedEffect(lockCoolDownSeconds) {
        if (lockCoolDownSeconds > 0) {
            delay(1000)
            lockCoolDownSeconds -= 1
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (!showPinKeypad) {
            // Fullscreen Single View
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 64.dp, vertical = 24.dp)
            ) {
                // Apple Monochrome Padlock Badge
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1C1C1E))
                        .border(BorderStroke(1.5.dp, Color.White), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🔒", fontSize = 38.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "TALO OS",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "APPAREIL VERROUILLÉ",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Owner Custom Message Container (Apple Glass Card)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.65f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1C1C1E))
                        .border(BorderStroke(1.dp, Color(0x33FFFFFF)), RoundedCornerShape(16.dp))
                        .padding(horizontal = 28.dp, vertical = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "MESSAGE DU PROPRIÉTAIRE",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = lockMessage.ifEmpty { "Ce téléviseur a été verrouillé à distance par son propriétaire." },
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Security Beacon Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFF1C1C1E))
                        .border(BorderStroke(1.dp, Color(0x22FFFFFF)), CircleShape)
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Balise active • Signalement chiffré en continu vers Talo Cloud",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Pour déverrouiller cet appareil, connectez-vous sur votre espace web :\ntalo-tv.studio.ynh.fr/account (Identifiant Hub : HUB-$fingerprint)",
                    color = Color.White.copy(alpha = 0.45f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                LockActionButton(
                    text = "Déverrouiller avec le Code PIN de secours",
                    onClick = { showPinKeypad = true }
                )
            }
        } else {
            // Split 16:9 Widescreen Apple tvOS Keypad Layout
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 48.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Column: Lock Status & Owner Info
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 32.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1C1C1E))
                                .border(BorderStroke(1.5.dp, Color.White), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🔒", fontSize = 26.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "TALO OS",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 3.sp
                            )
                            Text(
                                text = "APPAREIL VERROUILLÉ",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF1C1C1E))
                            .border(BorderStroke(1.dp, Color(0x33FFFFFF)), RoundedCornerShape(14.dp))
                            .padding(18.dp)
                    ) {
                        Column {
                            Text(
                                text = "MESSAGE DU PROPRIÉTAIRE",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = lockMessage.ifEmpty { "Ce téléviseur a été verrouillé à distance." },
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Identifiant Hub : HUB-$fingerprint\nDéverrouillable à distance sur talo-tv.studio.ynh.fr/account",
                        color = Color.White.copy(alpha = 0.45f),
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }

                // Right Column: PIN Dots & Keypad
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "CODE PIN DE SECOURS",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // PIN Dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        repeat(4) { index ->
                            val isFilled = index < enteredPin.length
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(if (isFilled) Color.White else Color.Transparent)
                                    .border(BorderStroke(1.5.dp, Color.White), CircleShape)
                            )
                        }
                    }

                    if (pinError) {
                        Text(
                            text = if (lockCoolDownSeconds > 0) "Trop de tentatives. Réessayez dans ${lockCoolDownSeconds}s" else "Code PIN incorrect.",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    if (lockCoolDownSeconds == 0) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                KeypadButton("1") { onPinDigit("1", enteredPin) { enteredPin = it; checkPin(context, enteredPin, onEmergencyUnlock, { pinError = true; attemptsCount++; if (attemptsCount >= 3) lockCoolDownSeconds = 60 }, { enteredPin = "" }) } }
                                KeypadButton("2") { onPinDigit("2", enteredPin) { enteredPin = it; checkPin(context, enteredPin, onEmergencyUnlock, { pinError = true; attemptsCount++; if (attemptsCount >= 3) lockCoolDownSeconds = 60 }, { enteredPin = "" }) } }
                                KeypadButton("3") { onPinDigit("3", enteredPin) { enteredPin = it; checkPin(context, enteredPin, onEmergencyUnlock, { pinError = true; attemptsCount++; if (attemptsCount >= 3) lockCoolDownSeconds = 60 }, { enteredPin = "" }) } }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                KeypadButton("4") { onPinDigit("4", enteredPin) { enteredPin = it; checkPin(context, enteredPin, onEmergencyUnlock, { pinError = true; attemptsCount++; if (attemptsCount >= 3) lockCoolDownSeconds = 60 }, { enteredPin = "" }) } }
                                KeypadButton("5") { onPinDigit("5", enteredPin) { enteredPin = it; checkPin(context, enteredPin, onEmergencyUnlock, { pinError = true; attemptsCount++; if (attemptsCount >= 3) lockCoolDownSeconds = 60 }, { enteredPin = "" }) } }
                                KeypadButton("6") { onPinDigit("6", enteredPin) { enteredPin = it; checkPin(context, enteredPin, onEmergencyUnlock, { pinError = true; attemptsCount++; if (attemptsCount >= 3) lockCoolDownSeconds = 60 }, { enteredPin = "" }) } }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                KeypadButton("7") { onPinDigit("7", enteredPin) { enteredPin = it; checkPin(context, enteredPin, onEmergencyUnlock, { pinError = true; attemptsCount++; if (attemptsCount >= 3) lockCoolDownSeconds = 60 }, { enteredPin = "" }) } }
                                KeypadButton("8") { onPinDigit("8", enteredPin) { enteredPin = it; checkPin(context, enteredPin, onEmergencyUnlock, { pinError = true; attemptsCount++; if (attemptsCount >= 3) lockCoolDownSeconds = 60 }, { enteredPin = "" }) } }
                                KeypadButton("9") { onPinDigit("9", enteredPin) { enteredPin = it; checkPin(context, enteredPin, onEmergencyUnlock, { pinError = true; attemptsCount++; if (attemptsCount >= 3) lockCoolDownSeconds = 60 }, { enteredPin = "" }) } }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                KeypadButton("Effacer") {
                                    if (enteredPin.isNotEmpty()) enteredPin = enteredPin.dropLast(1)
                                    pinError = false
                                }
                                KeypadButton("0") { onPinDigit("0", enteredPin) { enteredPin = it; checkPin(context, enteredPin, onEmergencyUnlock, { pinError = true; attemptsCount++; if (attemptsCount >= 3) lockCoolDownSeconds = 60 }, { enteredPin = "" }) } }
                                KeypadButton("Retour") {
                                    showPinKeypad = false
                                    enteredPin = ""
                                    pinError = false
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun onPinDigit(digit: String, current: String, onUpdate: (String) -> Unit) {
    if (current.length < 4) {
        val updated = current + digit
        onUpdate(updated)
    }
}

private fun checkPin(
    context: Context,
    pin: String,
    onSuccess: () -> Unit,
    onError: () -> Unit,
    onReset: () -> Unit
) {
    if (pin.length == 4) {
        val valid = TaloSecurityVault.verifyMasterPin(context, pin)
        if (valid) {
            TaloSecurityVault.setLocked(context, false)
            onSuccess()
        } else {
            onError()
            onReset()
        }
    }
}

@Composable
private fun KeypadButton(
    label: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val bgColor = if (isFocused) Color.White else Color(0xFF1C1C1E)
    val textColor = if (isFocused) Color.Black else Color.White

    Box(
        modifier = Modifier
            .size(width = 80.dp, height = 48.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(BorderStroke(1.dp, if (isFocused) Color.White else Color(0x33FFFFFF)), RoundedCornerShape(10.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .focusable(interactionSource = interactionSource),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = if (label.length > 2) 12.sp else 18.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun LockActionButton(
    text: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val bgColor = if (isFocused) Color.White else Color(0xFF1C1C1E)
    val textColor = if (isFocused) Color.Black else Color.White

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(BorderStroke(1.dp, if (isFocused) Color.White else Color(0x33FFFFFF)), RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .focusable(interactionSource = interactionSource)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
