package com.example.backdoor.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.backdoor.game.AbyssOSManager
import com.example.backdoor.game.OsState
import com.example.backdoor.ui.boot.BootScreen
import com.example.backdoor.ui.components.OsNotificationOverlay
import com.example.backdoor.ui.desktop.DesktopScreen
import com.example.backdoor.ui.login.LoginScreen
import com.example.ui.theme.BackdoorTheme

@Composable
fun BackdoorAppRoot() {
    val scope = rememberCoroutineScope()
    val osManager = remember { AbyssOSManager(scope = scope) }

    val osState by osManager.osState.collectAsState()
    val bootLogs by osManager.bootLogs.collectAsState()
    val userProfile by osManager.userProfile.collectAsState()
    val activeNotification by osManager.activeNotification.collectAsState()
    val brightnessDimAlpha by osManager.brightnessAlpha.collectAsState()
    val settings by osManager.settingsRepository.settings.collectAsState()
    val accentColor = settings.theme.primaryColor

    LaunchedEffect(Unit) {
        osManager.startBootSequence()
    }

    BackdoorTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = osState,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "osStateTransition",
                modifier = Modifier.fillMaxSize()
            ) { state ->
                when (state) {
                    OsState.BOOTING -> {
                        BootScreen(
                            bootLogs = bootLogs,
                            onSkip = { osManager.skipBootSequence() },
                            accentColor = accentColor,
                            crtEnabled = settings.crtEffectEnabled
                        )
                    }
                    OsState.LOGIN -> {
                        LoginScreen(
                            existingProfile = userProfile,
                            onRegister = { u, p -> osManager.registerUser(u, p) },
                            onLogin = { u, p -> osManager.loginUser(u, p) },
                            accentColor = accentColor,
                            crtEnabled = settings.crtEffectEnabled
                        )
                    }
                    OsState.DESKTOP -> {
                        DesktopScreen(osManager = osManager)
                    }
                }
            }

            // Virtual Brightness Dimming Overlay
            if (brightnessDimAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = brightnessDimAlpha))
                )
            }

            // Global System Notification Overlay
            OsNotificationOverlay(notification = activeNotification)
        }
    }
}

