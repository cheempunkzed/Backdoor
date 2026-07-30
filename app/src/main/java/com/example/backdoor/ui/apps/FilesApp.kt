package com.example.backdoor.ui.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Terminal
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.backdoor.filesystem.VFSNode
import com.example.backdoor.filesystem.VirtualFileSystem
import com.example.ui.theme.AbyssBackground
import com.example.ui.theme.AbyssCard
import com.example.ui.theme.AbyssSurface
import com.example.ui.theme.AbyssSurfaceVariant
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun FilesApp(
    vfs: VirtualFileSystem,
    accentColor: Color = TerminalGreen,
    modifier: Modifier = Modifier
) {
    var currentPath by remember { mutableStateOf(vfs.getCwd()) }
    var selectedFileNode by remember { mutableStateOf<VFSNode.File?>(null) }

    val currentNodes = vfs.listDirectory(currentPath) ?: emptyList()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AbyssBackground)
            .padding(8.dp)
    ) {
        // Breadcrumb Navigation Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(AbyssSurface)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentPath != "/") {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(AbyssSurfaceVariant)
                        .clickable {
                            val parent = vfs.getNode(currentPath)?.parentPath ?: "/"
                            currentPath = parent
                            selectedFileNode = null
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Up",
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Text(
                text = "PATH: ",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = currentPath,
                color = accentColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Main Explorer Pane (List on left, Preview on right)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Left: Directory / File List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(AbyssSurface)
                    .padding(6.dp)
            ) {
                items(currentNodes) { node ->
                    val isDirectory = node is VFSNode.Directory
                    val isFile = node is VFSNode.File
                    val isSelected = selectedFileNode?.path == node.path

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when {
                                    isSelected -> accentColor.copy(alpha = 0.2f)
                                    isDirectory -> AbyssSurfaceVariant
                                    else -> Color.Transparent
                                }
                            )
                            .border(
                                width = if (isSelected) 1.dp else 0.dp,
                                color = if (isSelected) accentColor else Color.Transparent,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clickable {
                                if (isDirectory) {
                                    currentPath = node.path
                                    selectedFileNode = null
                                } else if (isFile) {
                                    selectedFileNode = node as VFSNode.File
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when {
                                isDirectory -> Icons.Default.Folder
                                (node as? VFSNode.File)?.isExecutable == true -> Icons.Default.Terminal
                                else -> Icons.Default.Description
                            },
                            contentDescription = node.name,
                            tint = if (isDirectory) CyberCyan else accentColor,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = node.name,
                            color = if (isDirectory) CyberCyan else TextPrimary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isDirectory) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = node.permissions,
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right: File Content Inspector
            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(AbyssCard)
                    .border(0.5.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                    .padding(8.dp)
            ) {
                if (selectedFileNode != null) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "FILE: ${selectedFileNode?.name}",
                            color = accentColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "PERMISSIONS: ${selectedFileNode?.permissions}",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(4.dp))
                                .background(AbyssBackground)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = selectedFileNode?.content.orEmpty(),
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Select a file to inspect content.",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
