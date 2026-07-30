package com.example.backdoor.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.TerminalGreen

@Composable
fun CrtOverlay(
    enabled: Boolean = true,
    scanlineColor: Color = TerminalGreen.copy(alpha = 0.03f),
    modifier: Modifier = Modifier
) {
    if (!enabled) return

    Canvas(modifier = modifier.fillMaxSize()) {
        val strokeWidth = 1f
        val gap = 4f
        var y = 0f
        while (y < size.height) {
            drawLine(
                color = scanlineColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = strokeWidth
            )
            y += gap
        }
    }
}
