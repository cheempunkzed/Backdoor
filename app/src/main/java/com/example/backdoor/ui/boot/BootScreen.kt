package com.example.backdoor.ui.boot

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.backdoor.game.BootLogEntry
import com.example.backdoor.ui.components.CrtOverlay
import com.example.ui.theme.AbyssBackground
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TextMuted

@Composable
fun BootScreen(
    bootLogs: List<BootLogEntry>,
    onSkip: () -> Unit,
    accentColor: Color = TerminalGreen,
    crtEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(bootLogs.size) {
        if (bootLogs.isNotEmpty()) {
            listState.animateScrollToItem(bootLogs.lastIndex)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AbyssBackground)
            .clickable { onSkip() }
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Spacer(modifier = Modifier.height(32.dp))

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(bootLogs) { log ->
                        val isHeader = log.text.startsWith("AbyssOS")
                        val isDone = log.isDone

                        Text(
                            text = log.text,
                            color = when {
                                isHeader -> accentColor
                                isDone -> accentColor
                                else -> accentColor.copy(alpha = 0.85f)
                            },
                            fontSize = if (isHeader) 18.sp else 13.sp,
                            fontWeight = if (isHeader || isDone) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            modifier = Modifier.padding(vertical = 3.dp)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "[ TAP ANYWHERE TO SKIP BOOT SEQUENCE ]",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        CrtOverlay(enabled = crtEnabled)
    }
}
