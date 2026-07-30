package com.example.backdoor.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@Composable
fun BottomDock(
    activeApp: OsApp?,
    onAppClick: (OsApp) -> Unit,
    accentColor: Color = TechPurple,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Floating Pill Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(AbyssSurface.copy(alpha = 0.9f))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(horizontal = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quick App Launch Icons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    OsApp.entries.forEach { app ->
                        DockAppIcon(
                            app = app,
                            isActive = activeApp == app,
                            accentColor = accentColor,
                            onClick = { onAppClick(app) }
                        )
                    }
                }

                // Active App Title / Standby Indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    AnimatedVisibility(visible = activeApp != null) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(accentColor.copy(alpha = 0.15f))
                                .border(0.5.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(accentColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = activeApp?.appName ?: "",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    if (activeApp == null) {
                        Text(
                            text = "Standby",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DockAppIcon(
    app: OsApp,
    isActive: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    val icon = getAppIconVector(app)

    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(if (isActive) accentColor.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = app.appName,
            tint = if (isActive) accentColor else TextPrimary.copy(alpha = 0.7f),
            modifier = Modifier.size(18.dp)
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
    }
}

