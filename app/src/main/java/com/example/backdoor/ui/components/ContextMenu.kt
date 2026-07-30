package com.example.backdoor.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.ui.theme.AbyssSurface
import com.example.ui.theme.TechPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

data class ContextMenuItem(
    val label: String,
    val icon: ImageVector? = null,
    val isDanger: Boolean = false,
    val onClick: () -> Unit
)

@Composable
fun ContextMenuPopup(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    items: List<ContextMenuItem>,
    title: String? = null,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    accentColor: Color = TechPurple
) {
    if (!visible) return

    Popup(
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(focusable = true)
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + scaleIn(initialScale = 0.9f),
            exit = fadeOut() + scaleOut(targetScale = 0.9f)
        ) {
            Box(
                modifier = Modifier
                    .width(190.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(AbyssSurface.copy(alpha = 0.96f))
                    .border(
                        width = 1.dp,
                        color = accentColor.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(vertical = 6.dp)
            ) {
                Column {
                    if (title != null) {
                        Text(
                            text = title.uppercase(),
                            color = accentColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.5.dp)
                                .background(Color.White.copy(alpha = 0.1f))
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    items.forEach { item ->
                        ContextMenuItemRow(
                            item = item,
                            accentColor = accentColor,
                            onDismiss = onDismissRequest
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContextMenuItemRow(
    item: ContextMenuItem,
    accentColor: Color,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onDismiss()
                item.onClick()
            }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (item.icon != null) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = if (item.isDanger) Color(0xFFFF5252) else accentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
        }

        Text(
            text = item.label,
            color = if (item.isDanger) Color(0xFFFF5252) else TextPrimary,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium
        )
    }
}
