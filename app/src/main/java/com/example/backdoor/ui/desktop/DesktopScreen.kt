package com.example.backdoor.ui.desktop

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.backdoor.game.AbyssOSManager
import com.example.backdoor.game.OsApp
import com.example.backdoor.ui.apps.BrowserApp
import com.example.backdoor.ui.apps.DarkNetApp
import com.example.backdoor.ui.apps.FilesApp
import com.example.backdoor.ui.apps.LogsApp
import com.example.backdoor.ui.apps.NetworkApp
import com.example.backdoor.ui.apps.SettingsApp
import com.example.backdoor.ui.apps.SystemMonitorApp
import com.example.backdoor.ui.apps.TerminalApp
import com.example.backdoor.ui.components.BottomDock
import com.example.backdoor.ui.components.CrtOverlay
import com.example.backdoor.ui.components.TopStatusBar
import com.example.backdoor.ui.components.WindowFrame
import com.example.backdoor.ui.components.getAppIconVector
import com.example.ui.theme.AbyssBackground
import com.example.ui.theme.AbyssCard
import com.example.ui.theme.AbyssSurface
import com.example.ui.theme.AbyssSurfaceVariant
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun DesktopScreen(
    osManager: AbyssOSManager,
    modifier: Modifier = Modifier
) {
    val systemStatus by osManager.systemStatus.collectAsState()
    val activeApp by osManager.activeApp.collectAsState()
    val settings by osManager.settingsRepository.settings.collectAsState()
    val accentColor = settings.theme.primaryColor

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AbyssBackground)
    ) {
        // Cyber Grid Background Wallpaper
        CyberGridWallpaper(accentColor = accentColor)

        Column(modifier = Modifier.fillMaxSize()) {
            // Top Status Bar
            TopStatusBar(
                systemStatus = systemStatus,
                timeString = osManager.getCurrentTimeString(),
                dateString = osManager.getCurrentDateString(),
                accentColor = accentColor
            )

            // Desktop Body Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (isLandscape) {
                    // Landscape Mode: Side System Widget + Apps Grid
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        // Left System Info Panel
                        LandscapeSideWidget(
                            osManager = osManager,
                            accentColor = accentColor,
                            modifier = Modifier
                                .width(220.dp)
                                .fillMaxHeight()
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        // Apps Grid
                        DesktopIconGrid(
                            onAppClick = { osManager.openApp(it) },
                            accentColor = accentColor,
                            columns = 4,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    // Portrait Mode: Clean Centered Desktop Grid
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        DesktopIconGrid(
                            onAppClick = { osManager.openApp(it) },
                            accentColor = accentColor,
                            columns = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Active App Window Overlay
                androidx.compose.animation.AnimatedVisibility(
                    visible = activeApp != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    activeApp?.let { app ->
                        WindowFrame(
                            title = app.appName,
                            icon = getAppIconVector(app),
                            onClose = { osManager.closeActiveApp() },
                            accentColor = accentColor
                        ) {
                            when (app) {
                                OsApp.TERMINAL -> TerminalApp(osManager = osManager, accentColor = accentColor)
                                OsApp.FILES -> FilesApp(osManager = osManager, accentColor = accentColor)
                                OsApp.SETTINGS -> SettingsApp(osManager = osManager, accentColor = accentColor)
                                OsApp.SYSTEM_MONITOR -> SystemMonitorApp(osManager = osManager, accentColor = accentColor)
                                OsApp.LOGS -> LogsApp(osManager = osManager, accentColor = accentColor)
                                OsApp.DARKNET -> DarkNetApp(osManager = osManager, accentColor = accentColor)
                                OsApp.BROWSER -> BrowserApp(osManager = osManager, accentColor = accentColor)
                                OsApp.NETWORK -> NetworkApp(osManager = osManager, accentColor = accentColor)
                            }
                        }
                    }
                }
            }

            // Bottom Dock Taskbar
            BottomDock(
                activeApp = activeApp,
                onAppClick = { osManager.openApp(it) },
                accentColor = accentColor
            )
        }

        // Optional CRT Screen Effect
        CrtOverlay(enabled = settings.crtEffectEnabled)
    }
}

@Composable
private fun DesktopIconGrid(
    onAppClick: (OsApp) -> Unit,
    accentColor: Color,
    columns: Int,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        items(OsApp.entries) { app ->
            DesktopAppShortcut(
                app = app,
                accentColor = accentColor,
                onClick = { onAppClick(app) }
            )
        }
    }
}

@Composable
private fun DesktopAppShortcut(
    app: OsApp,
    accentColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(AbyssCard)
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = getAppIconVector(app),
                contentDescription = app.appName,
                tint = accentColor,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = app.appName,
            color = TextPrimary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun LandscapeSideWidget(
    osManager: AbyssOSManager,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val status by osManager.systemStatus.collectAsState()

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(AbyssSurface)
            .border(0.5.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = "SYSTEM NODE OVERVIEW",
            color = accentColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(10.dp))

        WidgetDetailRow("NODE ID", status.hostname)
        WidgetDetailRow("OPERATOR", status.userHandle)
        WidgetDetailRow("KERNEL", "0.2.0-AbyssFS")
        WidgetDetailRow("UPTIME", "${status.uptimeSeconds}s")
        WidgetDetailRow("MEMORY", "${status.usedRamMb}MB")

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(AbyssSurfaceVariant)
                .padding(8.dp)
        ) {
            Text(
                text = "Backdoor Phase 1 Kernel online. Cyber OS abstraction layer active.",
                color = TextMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun WidgetDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        Text(value, color = TextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun CyberGridWallpaper(accentColor: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val gridGap = 40f
        val gridColor = accentColor.copy(alpha = 0.04f)

        var x = 0f
        while (x < size.width) {
            drawLine(color = gridColor, start = Offset(x, 0f), end = Offset(x, size.height), strokeWidth = 1f)
            x += gridGap
        }

        var y = 0f
        while (y < size.height) {
            drawLine(color = gridColor, start = Offset(0f, y), end = Offset(size.width, y), strokeWidth = 1f)
            y += gridGap
        }
    }
}
