package com.talotv.os.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.talotv.os.R
import com.talotv.os.TaloApplication
import kotlinx.coroutines.delay

/**
 * 9 Onglets Principaux des Réglages Talo OS
 */
enum class SettingsCategory(val title: String) {
    GENERAL("Général"),
    USERS("Utilisateurs et comptes"),
    NETWORK("Réseau"),
    VIDEO_AUDIO("Vidéo et audio"),
    APPS("Applications"),
    NOTIFICATIONS("Notifications"),
    REMOTES("Télécommandes et appareils"),
    SYSTEM("Système"),
    HELP("Aide")
}

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val soundManager = remember { TaloApplication.instance.soundEffectManager }

    val versionName = remember {
        try {
            val name = context.packageManager.getPackageInfo(context.packageName, 0).versionName
            if (name.isNullOrBlank()) "0.0.1" else name
        } catch (e: Exception) {
            "0.0.1"
        }
    }

    var selectedIndex by remember { mutableIntStateOf(0) }
    var showWikiViewer by remember { mutableStateOf(false) }

    val categories = SettingsCategory.values()
    val focusRequesters = remember { List(categories.size) { FocusRequester() } }

    // Si le lecteur Wiki intégré est ouvert, on l'affiche en plein écran
    if (showWikiViewer) {
        WikiViewerScreen(
            url = "https://github.com/sudo-aptttt/talo-tv/wiki",
            onClose = {
                soundManager.playSelectSound()
                showWikiViewer = false
            }
        )
        return
    }

    // Interception de la touche Retour vers l'écran d'accueil
    BackHandler {
        onNavigateBack()
    }

    // Mise au point automatique sur le premier élément à l'ouverture
    LaunchedEffect(Unit) {
        delay(60)
        try {
            focusRequesters.getOrNull(selectedIndex)?.requestFocus()
        } catch (_: Exception) {}
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF14171D),
                        Color(0xFF0C0E12),
                        Color(0xFF07080A)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 64.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Titre supérieur centré
            Text(
                text = "Réglages",
                color = Color(0xCCFFFFFF),
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Section principale : Gauche (Badge Logo + Version) | Droite (Liste avec chevrons)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // COLONNE GAUCHE : Boîte Logo Squircle avec version en-dessous
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 56.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Carte Squircle avec le logo Talo OS
                    Box(
                        modifier = Modifier
                            .size(260.dp)
                            .clip(RoundedCornerShape(36.dp))
                            .background(Color(0xFF1E2127))
                            .border(
                                BorderStroke(1.dp, Color(0x18FFFFFF)),
                                RoundedCornerShape(36.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.talo_os_logo_white),
                            contentDescription = "Talo OS",
                            modifier = Modifier
                                .width(160.dp)
                                .height(70.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Version du système en-dessous
                    Text(
                        text = "v$versionName",
                        color = Color(0x80FFFFFF),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp
                    )
                }

                // COLONNE DROITE : Liste verticale des 9 catégories avec flèches
                LazyColumn(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    itemsIndexed(categories) { index, category ->
                        SettingsMenuItem(
                            title = category.title,
                            isFocused = (selectedIndex == index),
                            focusRequester = focusRequesters[index],
                            onFocused = {
                                selectedIndex = index
                                soundManager.playFocusSound()
                            },
                            onClick = {
                                soundManager.playSelectSound()
                                when (category) {
                                    SettingsCategory.HELP -> {
                                        // Ouvre directement le Wiki GitHub dans le lecteur intégré à Talo OS
                                        showWikiViewer = true
                                    }
                                    else -> {
                                        // À développer progressivement
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Élément de menu de réglage individuel avec indicateur chevron
 */
@Composable
private fun SettingsMenuItem(
    title: String,
    isFocused: Boolean,
    focusRequester: FocusRequester,
    onFocused: () -> Unit,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val actualFocus by interactionSource.collectIsFocusedAsState()

    LaunchedEffect(actualFocus) {
        if (actualFocus) onFocused()
    }

    val scale by animateFloatAsState(
        targetValue = if (actualFocus) 1.02f else 1.0f,
        animationSpec = tween(120),
        label = "menuScale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (actualFocus) Color.White else Color(0x24FFFFFF),
        animationSpec = tween(120),
        label = "menuBg"
    )

    val contentColor by animateColorAsState(
        targetValue = if (actualFocus) Color(0xFF0B0C0E) else Color(0xFFF2F2F7),
        animationSpec = tween(120),
        label = "menuContent"
    )

    val chevronColor by animateColorAsState(
        targetValue = if (actualFocus) Color(0xFF0B0C0E) else Color(0x60FFFFFF),
        animationSpec = tween(120),
        label = "chevronColor"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .scale(scale)
            .clip(RoundedCornerShape(13.dp))
            .background(backgroundColor)
            .border(
                BorderStroke(
                    width = 1.dp,
                    color = if (actualFocus) Color.White else Color(0x10FFFFFF)
                ),
                shape = RoundedCornerShape(13.dp)
            )
            .focusRequester(focusRequester)
            .focusable(interactionSource = interactionSource)
            .clickable { onClick() }
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.key) {
                        Key.DirectionRight, Key.Enter, Key.NumPadEnter, Key.DirectionCenter -> {
                            onClick()
                            true
                        }
                        else -> false
                    }
                } else false
            }
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = title,
            color = contentColor,
            fontSize = 16.sp,
            fontWeight = if (actualFocus) FontWeight.SemiBold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Icon(
            painter = painterResource(id = R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = chevronColor,
            modifier = Modifier.size(18.dp)
        )
    }
}

/**
 * Lecteur Wiki GitHub intégré à Talo TV
 * Permet de consulter la documentation directement sur le téléviseur même si aucun navigateur n'est installé.
 */
@Composable
private fun WikiViewerScreen(
    url: String,
    onClose: () -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    val closeFocusRequester = remember { FocusRequester() }

    BackHandler {
        if (webViewRef?.canGoBack() == true) {
            webViewRef?.goBack()
        } else {
            onClose()
        }
    }

    LaunchedEffect(Unit) {
        delay(80)
        try {
            closeFocusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C0E12))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Barre d'en-tête supérieure avec marque et bouton Fermer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(Color(0xFF161920))
                    .padding(horizontal = 32.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.talo_os_logo_white),
                        contentDescription = "Talo OS",
                        modifier = Modifier
                            .width(80.dp)
                            .height(35.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Wiki & Documentation officielle",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = url,
                            color = Color(0x70FFFFFF),
                            fontSize = 11.sp
                        )
                    }
                }

                // Bouton Fermer (Retour) avec mise au point TV D-Pad
                val interactionSource = remember { MutableInteractionSource() }
                val isCloseFocused by interactionSource.collectIsFocusedAsState()

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isCloseFocused) Color.White else Color(0x28FFFFFF))
                        .border(
                            BorderStroke(1.dp, if (isCloseFocused) Color.White else Color(0x18FFFFFF)),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .focusRequester(closeFocusRequester)
                        .focusable(interactionSource = interactionSource)
                        .clickable { onClose() }
                        .onKeyEvent { keyEvent ->
                            if (keyEvent.type == KeyEventType.KeyDown) {
                                when (keyEvent.key) {
                                    Key.Enter, Key.NumPadEnter, Key.DirectionCenter, Key.Back -> {
                                        onClose()
                                        true
                                    }
                                    else -> false
                                }
                            } else false
                        }
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Fermer (Retour)",
                        color = if (isCloseFocused) Color(0xFF0A0A0C) else Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Indicateur de chargement discret
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(Color(0xFF0071E3))
                )
            }

            // WebView native affichant le Wiki GitHub
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            loadWithOverviewMode = true
                            useWideViewPort = true
                            cacheMode = WebSettings.LOAD_DEFAULT
                            userAgentString =
                                "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                        }
                        setBackgroundColor(android.graphics.Color.parseColor("#0C0E12"))
                        isFocusable = true
                        isFocusableInTouchMode = true
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isLoading = false
                            }
                        }
                        webViewRef = this
                        loadUrl(url)
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            )
        }
    }
}
