package com.example.backdoor.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.backdoor.game.OsApp
import com.example.ui.theme.AbyssSurface
import com.example.ui.theme.TechPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BottomDock(
    pinnedApps: List<OsApp>,
    openApps: List<OsApp>,
    focusedApp: OsApp?,
    onAppClick: (OsApp) -> Unit,
    onPinApp: (OsApp) -> Unit,
    onUnpinApp: (OsApp) -> Unit,
    onCloseApp: (OsApp) -> Unit,
    accentColor: Color = TechPurple,
    modifier: Modifier = Modifier
) {
    var contextMenuApp by remember { mutableStateOf<OsApp?>(null) }

    // Combine pinned apps and running non-pinned apps
    val dockApps = (pinnedApps + openApps).distinct()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        // Larger, more readable Dock Container
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(AbyssSurface.copy(alpha = 0.94f))
                .border(
                    width = 1.dp,
                    color = accentColor.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (dockApps.isEmpty()) {
                    Text(
                        text = "[ Dock Empty - Long Press App on Desktop to Pin ]",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                } else {
                    dockApps.forEach { app ->
                        val isPinned = pinnedApps.contains(app)
                        val isOpen = openApps.contains(app)
                        val isFocused = focusedApp == app

                        DockItemIcon(
                            app = app,
                            isOpen = isOpen,
                            isFocused = isFocused,
                            isPinned = isPinned,
                            accentColor = accentColor,
                            onClick = { onAppClick(app) },
                            onLongClick = { contextMenuApp = app }
                        )
                    }
                }
            }
        }

        // Context Menu for Dock Icon
        val targetApp = contextMenuApp
        if (targetApp != null) {
            val isPinned = pinnedApps.contains(targetApp)
            val isOpen = openApps.contains(targetApp)

            ContextMenuPopup(
                visible = true,
                onDismissRequest = { contextMenuApp = null },
                title = targetApp.appName,
                accentColor = accentColor,
                items = buildList {
                    if (isPinned) {
                        add(
                            ContextMenuItem(
                                label = "Unpin from Dock",
                                icon = Icons.Default.PushPin,
                                onClick = { onUnpinApp(targetApp) }
                            )
                        )
                    } else {
                        add(
                            ContextMenuItem(
                                label = "Pin to Dock",
                                icon = Icons.Default.PushPin,
                                onClick = { onPinApp(targetApp) }
                            )
                        )
                    }
                    if (isOpen) {
                        add(
                            ContextMenuItem(
                                label = "Close App",
                                icon = Icons.Default.Close,
                                isDanger = true,
                                onClick = { onCloseApp(targetApp) }
                            )
                        )
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DockItemIcon(
    app: OsApp,
    isOpen: Boolean,
    isFocused: Boolean,
    isPinned: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.1f else 1.0f,
        animationSpec = spring(stiffness = 300f),
        label = "dockIconScale"
    )

    val bgColor by animateColorAsState(
        targetValue = when {
            isFocused -> accentColor.copy(alpha = 0.28f)
            isOpen -> Color.White.copy(alpha = 0.12f)
            else -> Color.Transparent
        },
        label = "dockIconBg"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.scale(scale)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(bgColor)
                .border(
                    width = if (isFocused) 1.dp else 0.5.dp,
                    color = if (isFocused) accentColor else Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                )
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = getAppIconVector(app),
                contentDescription = app.appName,
                tint = if (isFocused) accentColor else if (isOpen) TextPrimary else TextMuted,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Running Indicator Dot
        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isFocused -> accentColor
                        isOpen -> Color.White.copy(alpha = 0.7f)
                        else -> Color.Transparent
                    }
                )
        )
    }
}

fun getAppIconVector(app: OsApp): ImageVector {
    return when (app) {
        OsApp.TERMINAL -> Icons.Default.Code
        OsApp.FILES -> Icons.Default.Folder
        OsApp.BROWSER -> Icons.Default.Language
        OsApp.NETWORK -> Icons.Default.WifiTethering
        OsApp.DARKNET -> Icons.Default.Security
        OsApp.SETTINGS -> Icons.Default.Settings
        OsApp.LOGS -> Icons.Default.ListAlt
        OsApp.SYSTEM_MONITOR -> Icons.Default.Monitor
        OsApp.WALLET -> Icons.Default.AccountBalanceWallet
        OsApp.CONTRACTS -> Icons.Default.Work
        OsApp.MARKETPLACE -> Icons.Default.ShoppingCart
        OsApp.MAIL -> Icons.Default.Mail
        OsApp.NEWS -> Icons.Default.Article
        OsApp.INVENTORY -> Icons.Default.Inventory
    }
}

