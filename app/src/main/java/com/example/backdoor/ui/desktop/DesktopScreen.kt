package com.example.backdoor.ui.desktop

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.backdoor.core.WindowState
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
import com.example.backdoor.ui.components.ContextMenuItem
import com.example.backdoor.ui.components.ContextMenuPopup
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DesktopScreen(
    osManager: AbyssOSManager,
    modifier: Modifier = Modifier
) {
    val systemStatus by osManager.systemStatus.collectAsState()
    val activeApp by osManager.activeApp.collectAsState()
    val pinnedApps by osManager.pinnedDockApps.collectAsState()
    val openWindows by osManager.windowManager.windows.collectAsState()
    val settings by osManager.settingsRepository.settings.collectAsState()
    val accentColor = settings.theme.primaryColor

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Desktop Context Menu state
    var desktopContextMenuVisible by remember { mutableStateOf(false) }
    var contextMenuApp by remember { mutableStateOf<OsApp?>(null) }

    // Running apps list derived from open windows
    val runningApps = remember(openWindows) { openWindows.map { it.app }.distinct() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AbyssBackground)
            .combinedClickable(
                onClick = {},
                onLongClick = { desktopContextMenuVisible = true }
            )
    ) {
        // Cyber Grid Wallpaper
        CyberGridWallpaper(accentColor = accentColor)

        Column(modifier = Modifier.fillMaxSize()) {
            // Top Status Bar (AbyssOS Custom Top Bar)
            TopStatusBar(
                systemStatus = systemStatus,
                timeString = osManager.getCurrentTimeString(),
                dateString = osManager.getCurrentDateString(),
                accentColor = accentColor
            )

            // Desktop Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (isLandscape) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        LandscapeSideWidget(
                            osManager = osManager,
                            accentColor = accentColor,
                            modifier = Modifier
                                .width(220.dp)
                                .fillMaxHeight()
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        DesktopIconGrid(
                            pinnedApps = pinnedApps,
                            onAppClick = { osManager.openApp(it) },
                            onAppLongClick = { app -> contextMenuApp = app },
                            accentColor = accentColor,
                            columns = 4,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        DesktopIconGrid(
                            pinnedApps = pinnedApps,
                            onAppClick = { osManager.openApp(it) },
                            onAppLongClick = { app -> contextMenuApp = app },
                            accentColor = accentColor,
                            columns = 4,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Render Multi-Window Stack
                openWindows.sortedBy { it.zIndex }.forEach { winState ->
                    androidx.compose.runtime.key(winState.windowId) {
                        DraggableWindowWrapper(
                            winState = winState,
                            osManager = osManager,
                            accentColor = accentColor
                        )
                    }
                }
            }

            // Bottom Dock Taskbar
            BottomDock(
                pinnedApps = pinnedApps,
                openApps = runningApps,
                focusedApp = activeApp,
                onAppClick = { app ->
                    if (osManager.windowManager.isAppOpen(app)) {
                        if (activeApp == app) {
                            osManager.windowManager.minimizeWindow(app)
                        } else {
                            osManager.openApp(app)
                        }
                    } else {
                        osManager.openApp(app)
                    }
                },
                onPinApp = { osManager.pinAppToDock(it) },
                onUnpinApp = { osManager.unpinAppFromDock(it) },
                onCloseApp = { osManager.closeApp(it) },
                accentColor = accentColor
            )
        }

        // Desktop Context Menu (Long press on wallpaper)
        ContextMenuPopup(
            visible = desktopContextMenuVisible,
            onDismissRequest = { desktopContextMenuVisible = false },
            title = "DESKTOP",
            accentColor = accentColor,
            items = listOf(
                ContextMenuItem(
                    label = "Create Folder",
                    icon = Icons.Default.CreateNewFolder,
                    onClick = {
                        osManager.vfs.createDirectory(dirPath = "/home/operator", dirName = "New_Folder", owner = "operator")
                        osManager.showNotification("DESKTOP", "Created new folder on desktop.", com.example.backdoor.core.NotificationLevel.INFO)
                    }
                ),
                ContextMenuItem(
                    label = "Refresh Desktop",
                    icon = Icons.Default.Refresh,
                    onClick = {
                        osManager.showNotification("SYSTEM", "Desktop environment refreshed.", com.example.backdoor.core.NotificationLevel.INFO)
                    }
                ),
                ContextMenuItem(
                    label = "System Settings",
                    icon = Icons.Default.Settings,
                    onClick = { osManager.openApp(OsApp.SETTINGS) }
                )
            )
        )

        // App Icon Context Menu
        val targetApp = contextMenuApp
        if (targetApp != null) {
            val isPinned = pinnedApps.contains(targetApp)
            ContextMenuPopup(
                visible = true,
                onDismissRequest = { contextMenuApp = null },
                title = targetApp.appName,
                accentColor = accentColor,
                items = listOf(
                    ContextMenuItem(
                        label = "Open Application",
                        icon = Icons.Default.OpenInNew,
                        onClick = { osManager.openApp(targetApp) }
                    ),
                    if (isPinned) {
                        ContextMenuItem(
                            label = "Unpin from Dock",
                            icon = Icons.Default.PushPin,
                            onClick = { osManager.unpinAppFromDock(targetApp) }
                        )
                    } else {
                        ContextMenuItem(
                            label = "Pin to Dock",
                            icon = Icons.Default.PushPin,
                            onClick = { osManager.pinAppToDock(targetApp) }
                        )
                    }
                )
            )
        }

        // CRT Screen Shader Overlay
        CrtOverlay(enabled = settings.crtEffectEnabled)
    }
}

@Composable
private fun DraggableWindowWrapper(
    winState: WindowState,
    osManager: AbyssOSManager,
    accentColor: Color
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    if (winState.isMinimized) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .offset { IntOffset(offsetX.toInt(), offsetY.toInt()) }
            .pointerInput(winState.app) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
            .clickable {
                osManager.windowManager.bringToFront(winState.app)
            }
    ) {
        WindowFrame(
            title = winState.title,
            icon = getAppIconVector(winState.app),
            onClose = { osManager.closeApp(winState.app) },
            accentColor = if (winState.isFocused) accentColor else TextMuted
        ) {
            when (winState.app) {
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

@Composable
private fun DesktopIconGrid(
    pinnedApps: List<OsApp>,
    onAppClick: (OsApp) -> Unit,
    onAppLongClick: (OsApp) -> Unit,
    accentColor: Color,
    columns: Int,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        items(OsApp.entries) { app ->
            DesktopAppShortcut(
                app = app,
                accentColor = accentColor,
                onClick = { onAppClick(app) },
                onLongClick = { onAppLongClick(app) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DesktopAppShortcut(
    app: OsApp,
    accentColor: Color,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(AbyssCard)
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = getAppIconVector(app),
                contentDescription = app.appName,
                tint = accentColor,
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

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
        WidgetDetailRow("KERNEL", "0.4.0-TerminalCore")
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
                text = "Backdoor Milestone 5 Terminal Core active. UNIX Shell Engine running.",
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
