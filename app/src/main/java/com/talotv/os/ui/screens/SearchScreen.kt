package com.talotv.os.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.runtime.collectAsState
import com.talotv.os.core.media.toDockAppItem
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.talotv.os.TaloApplication
import com.talotv.os.core.media.ContentRepository
import com.talotv.os.core.media.DockAppItem
import com.talotv.os.ui.components.TaloAppIcon
import com.talotv.os.ui.components.SearchGlassIcon
import com.talotv.os.ui.theme.TaloGlassBackground
import com.talotv.os.ui.theme.TaloGlassBorder
import com.talotv.os.ui.theme.TaloTextPrimary
import com.talotv.os.ui.theme.TaloTextSecondary

@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit,
    onAppSelected: (DockAppItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Tous") }

    val categories = listOf("Tous", "Applications", "Cinéma", "Télévision en Direct", "Jeux")

    val installedAppsManager = remember { TaloApplication.instance.installedAppsManager }
    val installedApps by installedAppsManager.installedApps.collectAsState()
    val dockApps = remember(installedApps) { installedAppsManager.getDockApps() }
    val allApps = remember(installedApps, dockApps) {
        val installedDockItems = installedApps.map { it.toDockAppItem() }
        (dockApps + installedDockItems).distinctBy { it.id }
    }

    val filteredApps = remember(searchQuery, selectedCategory) {
        allApps.filter { app ->
            val matchQuery = searchQuery.isEmpty() || app.name.contains(searchQuery, ignoreCase = true)
            val matchCat = when (selectedCategory) {
                "Applications" -> app.category.contains("Système", true) || app.category.contains("Communication", true)
                "Cinéma" -> app.category.contains("Cinéma", true) || app.category.contains("Streaming", true)
                "Télévision en Direct" -> app.category.contains("Direct", true)
                "Jeux" -> app.category.contains("Jeux", true)
                else -> true
            }
            matchQuery && matchCat
        }.distinctBy { it.id }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0C0D12))
            .padding(horizontal = 56.dp, vertical = 32.dp)
    ) {
        // Search Header with Back Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onNavigateBack() }
                    .padding(8.dp)
            ) {
                Text(text = "‹", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Light)
                Text(text = "Recherche", color = TaloTextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }

            // Search Bar Input Container
            Box(
                modifier = Modifier
                    .width(420.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(TaloGlassBackground)
                    .border(BorderStroke(1.2.dp, TaloGlassBorder), RoundedCornerShape(24.dp))
                    .padding(horizontal = 18.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SearchGlassIcon(size = 20.dp, tint = TaloTextSecondary)
                    Text(
                        text = if (searchQuery.isEmpty()) "Rechercher une application ou un film…" else searchQuery,
                        color = if (searchQuery.isEmpty()) TaloTextSecondary else Color.White,
                        fontSize = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Category Pills Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(categories) { cat ->
                val isSelected = selectedCategory == cat
                val interactionSource = remember { MutableInteractionSource() }
                val isFocused by interactionSource.collectIsFocusedAsState()

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) Color.White else if (isFocused) Color(0x33FFFFFF) else TaloGlassBackground)
                        .border(
                            BorderStroke(
                                if (isFocused) 2.dp else 1.dp,
                                if (isFocused) Color.White else TaloGlassBorder
                            ),
                            RoundedCornerShape(20.dp)
                        )
                        .clickable(interactionSource = interactionSource, indication = null) {
                            TaloApplication.instance.soundEffectManager.playSelectSound()
                            selectedCategory = cat
                        }
                        .focusable(interactionSource = interactionSource)
                        .padding(horizontal = 18.dp, vertical = 9.dp)
                ) {
                    Text(
                        text = cat,
                        color = if (isSelected) Color.Black else Color.White,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Search Results Grid
        Text(
            text = "Résultats (${filteredApps.size})",
            color = TaloTextSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            items(filteredApps) { app ->
                SearchResultCard(
                    app = app,
                    onClick = {
                        TaloApplication.instance.soundEffectManager.playSelectSound()
                        onAppSelected(app)
                    }
                )
            }
        }
    }
}

@Composable
fun SearchResultCard(
    app: DockAppItem,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Box(
        modifier = Modifier
            .scale(if (isFocused) 1.08f else 1.0f)
            .height(90.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (isFocused) Color(0x55FFFFFF) else TaloGlassBackground)
            .border(
                BorderStroke(
                    width = if (isFocused) 3.dp else 1.dp,
                    color = if (isFocused) Color.White else TaloGlassBorder
                ),
                RoundedCornerShape(18.dp)
            )
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .focusable(interactionSource = interactionSource)
            .padding(14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(app.bgGradient.first())),
                contentAlignment = Alignment.Center
            ) {
                if (app.iconBitmap != null) {
                    Image(
                        bitmap = app.iconBitmap,
                        contentDescription = app.name,
                        contentScale = if (app.isBanner) ContentScale.Crop else ContentScale.Fit,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                    )
                } else {
                    TaloAppIcon(iconType = app.iconType, size = 28.dp)
                }
            }

            Column {
                Text(
                    text = app.name,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = app.category,
                    color = TaloTextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}
