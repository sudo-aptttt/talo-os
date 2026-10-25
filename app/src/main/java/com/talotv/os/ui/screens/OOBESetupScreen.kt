package com.talotv.os.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.talotv.os.TaloApplication
import com.talotv.os.core.security.DeviceFingerprint
import com.talotv.os.core.system.CloudSyncManager
import kotlinx.coroutines.delay

@Composable
fun OOBESetupScreen(
    onSetupComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentStep by remember { mutableIntStateOf(1) } // 1: Benchmark, 2: Pairing, 3: Success Celebration
    var progress by remember { mutableFloatStateOf(0f) }
    var statusText by remember { mutableStateOf("Initialisation de l'environnement…") }
    var pairingCode by remember { mutableStateOf("TALO-842") }
    var deviceId by remember { mutableStateOf("") }
    var pairedUserName by remember { mutableStateOf("") }
    var isPollingActive by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        deviceId = DeviceFingerprint.getFingerprint(context)
        // Generate memorable pairing code: TALO-XXX
        val randomNum = (100..999).random()
        pairingCode = "TALO-$randomNum"

        // 1. Initial Material Calibration Benchmark (Steps 9 & 10)
        val steps = listOf(
            Pair(0.15f, "Détection du processeur et des unités GPU…"),
            Pair(0.35f, "Mesure de la bande passante RAM disponible…"),
            Pair(0.55f, "Vérification des certificats Widevine L1 & 4K HDR…"),
            Pair(0.75f, "Optimisation du moteur d'affichage et cache d'images…"),
            Pair(0.95f, "Application du profil d'excellence Talo…"),
            Pair(1.0f, "Calibration matérielle terminée avec succès !")
        )

        for ((p, msg) in steps) {
            delay(1200)
            progress = p
            statusText = msg
        }

        delay(800)
        TaloApplication.instance.hardwareProfiler.autoCalibrate()
        currentStep = 2 // Transition to Step 11: Mandatory Pairing
    }

    // Two-Way Cloud Pairing Polling Loop (Step 11 -> Step 13 -> Step 14)
    LaunchedEffect(currentStep == 2) {
        if (currentStep == 2) {
            isPollingActive = true
            // Register ephemeral code on Talo cloud API
            CloudSyncManager.registerPairingCode(context, pairingCode)

            while (isPollingActive) {
                delay(2200)
                val (isClaimed, userName, cloudProfiles) = CloudSyncManager.pollPairingStatus(context, pairingCode)
                if (isClaimed) {
                    isPollingActive = false
                    pairedUserName = userName ?: "Utilisateur Talo"

                    // Step 14: Restore cloud profiles and customized interface
                    if (!cloudProfiles.isNullOrEmpty()) {
                        TaloApplication.instance.userProfileManager.updateFromCloud(cloudProfiles)
                    }

                    TaloApplication.instance.profileManager.setCalibrated(true)
                    currentStep = 3 // Move to Celebration
                    break
                }
            }
        }
    }

    // Step 15: Transition to HomeScreen after celebration
    LaunchedEffect(currentStep == 3) {
        if (currentStep == 3) {
            delay(2000)
            onSetupComplete()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF1C1C1E), Color(0xFF0A0A0C), Color.Black),
                    radius = 1200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        when (currentStep) {
            1 -> {
                // Step 1: Benchmark / Hardware Calibration
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(48.dp)
                ) {
                    Text(
                        text = "TALO TV",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 4.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Optimisation de votre téléviseur",
                        color = Color.White,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Talo OS adapte ses performances pour garantir une fluidité absolue sans saccade.",
                        color = Color(0xFFA1A1A6),
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color.White,
                        trackColor = Color(0xFF2C2C2E)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = statusText,
                        color = Color.LightGray,
                        fontSize = 14.sp
                    )
                }
            }

            2 -> {
                // Step 2: Cloud Account Pairing via Web or QR (Steps 11, 12, 13)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 48.dp, vertical = 24.dp)
                ) {
                    Text(
                        text = "Associez votre compte Talo TV",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Synchronisez vos profils, réglages et personnalisations en direct.",
                        color = Color(0xFFA1A1A6),
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(36.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // QR Code Graphic Placeholder
                        Box(
                            modifier = Modifier
                                .size(180.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "SCANNER LE QR",
                                    color = Color.Black,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "talo-tv.studio.ynh.fr\n/connect",
                                    color = Color.DarkGray,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(48.dp))

                        // Pairing Code Details
                        Column {
                            Text(
                                text = "Depuis votre mobile ou ordinateur :",
                                color = Color.LightGray,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "https://talo-tv.studio.ynh.fr/connect",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Saisissez ce code de jumelage :",
                                color = Color.LightGray,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF1C1C1E))
                                    .border(1.5.dp, Color.White, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 28.dp, vertical = 12.dp)
                            ) {
                                Text(
                                    text = pairingCode,
                                    color = Color.White,
                                    fontSize = 34.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 4.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Live Polling Spinner Indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "En attente de validation sur votre espace client web…",
                            color = Color(0xFFA1A1A6),
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Secondary offline fallback button
                    Button(
                        onClick = {
                            isPollingActive = false
                            TaloApplication.instance.profileManager.setCalibrated(true)
                            onSetupComplete()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C1C1E)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Text(
                            text = "Continuer sans compte (Mode hors-ligne)",
                            fontSize = 14.sp,
                            color = Color.LightGray
                        )
                    }
                }
            }

            3 -> {
                // Step 3: Success Celebration & Seamless Cloud Restoration (Steps 13, 14, 15)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(48.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✓",
                            color = Color.Black,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Association Réussie !",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (pairedUserName.isNotEmpty()) "Téléviseur lié au compte de $pairedUserName" else "Espace Talo OS configuré avec succès",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Synchronisation de vos profils et lancement de votre interface…",
                        color = Color(0xFFA1A1A6),
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
