package com.talotv.os.ui.screens

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.talotv.os.TaloApplication
import com.talotv.os.core.media.ContentRepository
import com.talotv.os.core.media.DockAppItem
import com.talotv.os.core.media.MediaSpotlight
import com.talotv.os.core.profile.UserProfile
import com.talotv.os.ui.components.FloatingAppDock
import com.talotv.os.ui.components.FloatingProfileTimeWidget
import com.talotv.os.ui.components.FullAppGrid
import com.talotv.os.ui.components.NavDestination
import com.talotv.os.ui.components.ProfileSwitcherDialog
import com.talotv.os.ui.components.TopShelfSpotlight
import com.talotv.os.ui.components.UpwardChevronIndicator
import com.talotv.os.ui.components.TvSidebarDrawer

@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToStore: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val profileManager = remember { TaloApplication.instance.profileManager }
    val userProfileManager = remember { com.talotv.os.core.profile.UserProfileManager(context) }
    val installedAppsManager = remember { TaloApplication.instance.installedAppsManager }

    val activeProfile by userProfileManager.activeProfile.collectAsState()
    val allProfiles by userProfileManager.profiles.collectAsState()
    val installedApps by installedAppsManager.installedApps.collectAsState()
    val dockApps = remember(installedApps) { installedAppsManager.getDockApps() }
    val gridApps = remember(installedApps, dockApps) {
        val dockPackageNames = dockApps.map { it.packageName }.toSet()
        val dockIds = dockApps.map { it.id }.toSet()
        val remaining = installedApps.filter { app ->
            app.packageName !in dockPackageNames && app.id !in dockIds
        }
        // Place Store first, other apps in middle, and Settings last (matching Apple tvOS layout)
        remaining.sortedWith(
            compareBy<com.talotv.os.core.apps.InstalledApp> { app ->
                when {
                    app.isStore -> 0
                    app.isSettings -> 2
                    else -> 1
                }
            }.thenBy { it.name }
        )
    }

    var activeSpotlight by remember {
        mutableStateOf<MediaSpotlight>(dockApps.firstOrNull()?.spotlight ?: ContentRepository.fallbackSpotlight)
    }
    var isProfileSwitcherOpen by remember { mutableStateOf(false) }
    var isSidebarOpen by remember { mutableStateOf(false) }

    LaunchedEffect(dockApps) {
        if (dockApps.isNotEmpty()) {
            activeSpotlight = dockApps.first().spotlight
        }
    }

    val dockFirstFocusRequester = remember { FocusRequester() }
    val profileFocusRequester = remember { FocusRequester() }

    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Initial focus on the first dock item to avoid being trapped on profile avatar, scrolled to top
    LaunchedEffect(Unit) {
        scrollState.scrollTo(0)
        delay(120)
        try {
            dockFirstFocusRequester.requestFocus()
        } catch (e: Exception) { }
    }

    fun launchAppPackage(pkg: String, name: String) {
        if (pkg == "com.android.vending") {
            installedAppsManager.launchStore(context)
            return
        }
        val pm: PackageManager = context.packageManager
        val launchIntent = pm.getLeanbackLaunchIntentForPackage(pkg) ?: pm.getLaunchIntentForPackage(pkg)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
        } else {
            Toast.makeText(context, "Lancement de $name (Talo OS)", Toast.LENGTH_SHORT).show()
        }
    }

    // Intercept Back button: closes dialogs or toggles sidebar
    BackHandler(enabled = true) {
        if (isProfileSwitcherOpen) {
            isProfileSwitcherOpen = false
        } else if (isSidebarOpen) {
            isSidebarOpen = false
        } else {
            isSidebarOpen = true
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Main Scrollable TV Experience
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // 1. Top Shelf Showcase (Hero Section with Dynamic Spotlight Backdrop matching Apple tvOS full screen)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(490.dp)
            ) {
                TopShelfSpotlight(
                    spotlight = activeSpotlight,
                    modifier = Modifier.fillMaxSize()
                )

                // The Signature Apple TV Floating Glass Dock sitting at bottom of Top Shelf
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                ) {
                    // Apple TV Upward Chevron Indicator right above dock (user reference)
                    UpwardChevronIndicator()

                    Spacer(modifier = Modifier.height(7.dp))

                    FloatingAppDock(
                        apps = dockApps,
                        firstItemFocusRequester = dockFirstFocusRequester,
                        onNavigateLeftFromFirst = { isSidebarOpen = true },
                        onNavigateUpToProfile = {
                            try { profileFocusRequester.requestFocus() } catch (e: Exception) { }
                        },
                        onAppFocused = { app ->
                            activeSpotlight = app.spotlight
                            coroutineScope.launch {
                                scrollState.animateScrollTo(0)
                            }
                        },
                        onAppSelected = { app ->
                            if (app.id == "app-store" || app.packageName == "com.android.vending") {
                                onNavigateToStore()
                            } else if (app.id == "dock-settings" || app.id == "app-settings" || app.packageName == "com.talotv.settings") {
                                onNavigateToSettings()
                            } else {
                                launchAppPackage(app.packageName, app.name)
                            }
                        }
                    )
                }
            }

            if (gridApps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))

                // Full App Library Grid (Real Installed Apps Only, No Duplicates from Dock)
                FullAppGrid(
                    apps = gridApps,
                    onNavigateLeftFromFirstColumn = { isSidebarOpen = true },
                    onAppSelected = { app ->
                        if (app.id == "app-store" || app.isStore || app.packageName == "com.android.vending") {
                            onNavigateToStore()
                        } else if (app.id == "app-settings" || app.isSettings || app.packageName == "com.talotv.settings") {
                            onNavigateToSettings()
                        } else {
                            launchAppPackage(app.packageName, app.name)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(60.dp))
            }
        }

        // ============================================================
        // FLOATING TOP-RIGHT: Time on left, Current Profile Picture (PP) on right
        // ============================================================
        FloatingProfileTimeWidget(
            activeProfile = activeProfile,
            focusRequester = profileFocusRequester,
            onProfileClick = { isProfileSwitcherOpen = !isProfileSwitcherOpen },
            onNavigateDown = {
                try { dockFirstFocusRequester.requestFocus() } catch (e: Exception) { }
            },
            onNavigateLeft = { isSidebarOpen = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 28.dp, end = 44.dp)
        )

        // ============================================================
        // OVERLAY 1: FLOATING LEFT SIDEBAR DRAWER (User Image 1)
        // ============================================================
        AnimatedVisibility(
            visible = isSidebarOpen,
            enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut(),
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            TvSidebarDrawer(
                currentDestination = NavDestination.HOME,
                activeProfile = activeProfile,
                onNavigate = { dest ->
                    isSidebarOpen = false
                    when (dest) {
                        NavDestination.SEARCH -> onNavigateToSearch()
                        NavDestination.SETTINGS -> onNavigateToSettings()
                        NavDestination.HOME -> { /* already on home */ }
                        NavDestination.CINEMA -> {
                            activeSpotlight = dockApps.getOrNull(0)?.spotlight ?: ContentRepository.fallbackSpotlight
                        }
                        NavDestination.LIVE_TV -> {
                            launchAppPackage("net.freebox.oqee", "Freebox OQEE")
                        }
                        NavDestination.ARCADE -> {
                            activeSpotlight = dockApps.getOrNull(1)?.spotlight ?: ContentRepository.fallbackSpotlight
                        }
                    }
                },
                onAppClick = { app ->
                    isSidebarOpen = false
                    if (app.id == "app-store" || app.packageName == "com.android.vending") {
                        onNavigateToStore()
                    } else if (app.id == "dock-settings" || app.id == "app-settings" || app.packageName == "com.talotv.settings") {
                        onNavigateToSettings()
                    } else {
                        launchAppPackage(app.packageName, app.name)
                    }
                },
                onProfileClick = {
                    isSidebarOpen = false
                    isProfileSwitcherOpen = true
                },
                onDismiss = { isSidebarOpen = false }
            )
        }

        // ============================================================
        // OVERLAY 2: APPLE PROFILE SWITCHER DIALOG (User Image 4)
        // ============================================================
        AnimatedVisibility(
            visible = isProfileSwitcherOpen,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 44.dp, top = 76.dp)
        ) {
            ProfileSwitcherDialog(
                profiles = allProfiles,
                activeProfile = activeProfile,
                onSelectProfile = { profile ->
                    userProfileManager.switchProfile(profile)
                    isProfileSwitcherOpen = false
                    Toast.makeText(context, "Profil « ${profile.name} » activé", Toast.LENGTH_SHORT).show()
                },
                onAddProfileClick = {
                    isProfileSwitcherOpen = false
                    onNavigateToSettings()
                },
                onDismiss = { isProfileSwitcherOpen = false }
            )
        }
    }
}
