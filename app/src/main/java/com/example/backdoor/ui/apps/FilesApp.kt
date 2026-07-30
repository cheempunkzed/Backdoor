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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.backdoor.filesystem.VFSFileType
import com.example.backdoor.filesystem.VFSNode
import com.example.backdoor.filesystem.VFSSortMode
import com.example.backdoor.filesystem.VirtualFileSystem
import com.example.backdoor.game.AbyssOSManager
import com.example.ui.theme.AbyssBackground
import com.example.ui.theme.AbyssCard
import com.example.ui.theme.AbyssSurface
import com.example.ui.theme.AbyssSurfaceVariant
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FilesApp(
    osManager: AbyssOSManager,
    accentColor: Color = TerminalGreen,
    modifier: Modifier = Modifier
) {
    val vfs = osManager.vfs
    val profile by osManager.userProfile.collectAsState()
    val activeUser = profile?.username ?: "operator"

    // Subscribe to VFS state updates
    val vfsVersionEvent by vfs.updateEvent.collectAsState()

    var currentPath by remember { mutableStateOf("/home/$activeUser") }
    var searchQuery by remember { mutableStateOf("") }
    var sortMode by remember { mutableStateOf(VFSSortMode.NAME_ASC) }
    var selectedNode by remember { mutableStateOf<VFSNode?>(null) }
    var showTrashView by remember { mutableStateOf(false) }

    // Dialog states
    var showCreateFileDialog by remember { mutableStateOf(false) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showEditFileDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showMoveDialog by remember { mutableStateOf(false) }
    var showCopyDialog by remember { mutableStateOf(false) }

    // Input fields for dialogs
    var inputName by remember { mutableStateOf("") }
    var inputContent by remember { mutableStateOf("") }
    var inputPath by remember { mutableStateOf("") }

    // Retrieve directory contents reactively based on vfsVersionEvent trigger
    val rawNodes = remember(currentPath, showTrashView, vfsVersionEvent) {
        if (showTrashView) {
            val trashPath = "/home/$activeUser/Trash"
            vfs.listDirectory(trashPath, includeTrash = true) ?: emptyList()
        } else {
            vfs.listDirectory(currentPath, includeHidden = false) ?: emptyList()
        }
    }

    // Filter and Sort nodes
    val filteredNodes = remember(rawNodes, searchQuery, sortMode) {
        var list = if (searchQuery.isNotBlank()) {
            vfs.findFiles(searchQuery, if (showTrashView) "/home/$activeUser/Trash" else currentPath)
        } else {
            rawNodes
        }

        when (sortMode) {
            VFSSortMode.NAME_ASC -> list.sortedWith(compareBy({ it !is VFSNode.Directory }, { it.name.lowercase() }))
            VFSSortMode.NAME_DESC -> list.sortedWith(compareBy({ it !is VFSNode.Directory }, { it.name.lowercase() })).reversed()
            VFSSortMode.SIZE_ASC -> list.sortedBy { it.metadata.sizeBytes }
            VFSSortMode.SIZE_DESC -> list.sortedByDescending { it.metadata.sizeBytes }
            VFSSortMode.DATE_ASC -> list.sortedBy { it.metadata.modifiedTimestamp }
            VFSSortMode.DATE_DESC -> list.sortedByDescending { it.metadata.modifiedTimestamp }
            VFSSortMode.TYPE -> list.sortedBy { it.metadata.fileType.name }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AbyssBackground)
            .padding(8.dp)
    ) {
        // Navigation & Breadcrumb Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(AbyssSurface)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentPath != "/" && !showTrashView) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(AbyssSurfaceVariant)
                        .clickable {
                            val parent = vfs.getNode(currentPath)?.parentPath ?: "/"
                            currentPath = parent
                            selectedNode = null
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Up Directory",
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Text(
                text = if (showTrashView) "TRASH REPOSITORY: " else "PATH: ",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = if (showTrashView) "/home/$activeUser/Trash" else currentPath,
                color = accentColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.weight(1f)
            )

            // Trash View Toggle
            Button(
                onClick = {
                    showTrashView = !showTrashView
                    selectedNode = null
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (showTrashView) Color(0xFFD32F2F) else AbyssSurfaceVariant,
                    contentColor = TextPrimary
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Trash",
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (showTrashView) "EXIT TRASH" else "TRASH",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Toolbar: Search, Sort & Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(AbyssSurface)
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search files...", fontSize = 11.sp, color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = accentColor, modifier = Modifier.size(16.dp)) },
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = AbyssSurfaceVariant,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Sort Dropdown
            var sortMenuExpanded by remember { mutableStateOf(false) }
            Box {
                IconButton(
                    onClick = { sortMenuExpanded = true },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(AbyssSurfaceVariant)
                ) {
                    Icon(Icons.Default.Sort, contentDescription = "Sort", tint = accentColor, modifier = Modifier.size(18.dp))
                }
                DropdownMenu(
                    expanded = sortMenuExpanded,
                    onDismissRequest = { sortMenuExpanded = false },
                    modifier = Modifier.background(AbyssCard)
                ) {
                    DropdownMenuItem(
                        text = { Text("Sort Name A-Z", fontSize = 11.sp, color = TextPrimary, fontFamily = FontFamily.Monospace) },
                        onClick = { sortMode = VFSSortMode.NAME_ASC; sortMenuExpanded = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Sort Size", fontSize = 11.sp, color = TextPrimary, fontFamily = FontFamily.Monospace) },
                        onClick = { sortMode = VFSSortMode.SIZE_DESC; sortMenuExpanded = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Sort Date", fontSize = 11.sp, color = TextPrimary, fontFamily = FontFamily.Monospace) },
                        onClick = { sortMode = VFSSortMode.DATE_DESC; sortMenuExpanded = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Sort Type", fontSize = 11.sp, color = TextPrimary, fontFamily = FontFamily.Monospace) },
                        onClick = { sortMode = VFSSortMode.TYPE; sortMenuExpanded = false }
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            if (showTrashView) {
                // Empty Trash Button
                Button(
                    onClick = { vfs.emptyTrash(activeUser) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C), contentColor = Color.White),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.height(38.dp)
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = "Empty", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("EMPTY TRASH", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            } else {
                // New File & New Folder Buttons
                IconButton(
                    onClick = { inputName = ""; inputContent = ""; showCreateFileDialog = true },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(AbyssSurfaceVariant)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New File", tint = accentColor, modifier = Modifier.size(18.dp))
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = { inputName = ""; showCreateFolderDialog = true },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(AbyssSurfaceVariant)
                ) {
                    Icon(Icons.Default.CreateNewFolder, contentDescription = "New Folder", tint = CyberCyan, modifier = Modifier.size(18.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Main Dual-Pane Explorer: Files List on Left, Action/Inspector Pane on Right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Left Pane: Directory / Files List
            LazyColumn(
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(AbyssSurface)
                    .padding(6.dp)
            ) {
                if (filteredNodes.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (showTrashView) "(Trash repository empty)" else "(Empty directory)",
                                color = TextMuted,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                } else {
                    items(filteredNodes) { node ->
                        val isDirectory = node is VFSNode.Directory
                        val isSelected = selectedNode?.path == node.path
                        val fileType = node.metadata.fileType

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    when {
                                        isSelected -> accentColor.copy(alpha = 0.25f)
                                        isDirectory -> AbyssSurfaceVariant.copy(alpha = 0.6f)
                                        else -> Color.Transparent
                                    }
                                )
                                .border(
                                    width = if (isSelected) 1.dp else 0.dp,
                                    color = if (isSelected) accentColor else Color.Transparent,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .clickable {
                                    selectedNode = node
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // File Icon
                            Icon(
                                imageVector = getFileIcon(fileType, (node as? VFSNode.File)?.isExecutable == true),
                                contentDescription = node.name,
                                tint = getFileIconColor(fileType, isDirectory, accentColor),
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = node.name,
                                    color = if (isDirectory) CyberCyan else TextPrimary,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = if (isDirectory) FontWeight.Bold else FontWeight.Medium
                                )
                                Text(
                                    text = "${node.metadata.permissions} | ${formatSize(node.metadata.sizeBytes)} | ${node.metadata.owner}",
                                    color = TextMuted,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            if (node.metadata.isSystemProtected) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = "Protected",
                                    tint = Color(0xFFFFB74D),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }

                            if (isDirectory && !showTrashView) {
                                Button(
                                    onClick = {
                                        currentPath = node.path
                                        selectedNode = null
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AbyssSurfaceVariant, contentColor = CyberCyan),
                                    shape = RoundedCornerShape(3.dp),
                                    modifier = Modifier.height(24.dp)
                                ) {
                                    Text("OPEN", fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right Pane: Item Inspector & Context Action Panel
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(AbyssCard)
                    .border(0.5.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                    .padding(8.dp)
            ) {
                val node = selectedNode
                if (node != null) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "METADATA INSPECTOR",
                            color = accentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "NAME: ${node.name}",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "TYPE: ${node.metadata.fileType.description}",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "PATH: ${node.path}",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "PERMS: ${node.metadata.permissions} (${node.metadata.owner})",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Context Actions Palette
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(AbyssSurface)
                                .padding(6.dp)
                        ) {
                            if (showTrashView) {
                                // Trash Operations
                                Button(
                                    onClick = {
                                        vfs.restoreFromTrash(node.path, activeUser)
                                        selectedNode = null
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = TerminalGreen, contentColor = Color.Black),
                                    modifier = Modifier.fillMaxWidth().height(28.dp),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Icon(Icons.Default.RestoreFromTrash, contentDescription = "Restore", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("RESTORE FILE", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Button(
                                    onClick = {
                                        vfs.deleteNode(node.path, permanent = true, userHandle = activeUser)
                                        selectedNode = null
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F), contentColor = Color.White),
                                    modifier = Modifier.fillMaxWidth().height(28.dp),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Icon(Icons.Default.DeleteForever, contentDescription = "Delete", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("DELETE PERMANENTLY", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                }
                            } else {
                                // Standard Operations
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    if (node is VFSNode.File) {
                                        Button(
                                            onClick = {
                                                inputContent = node.content
                                                showEditFileDialog = true
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = AbyssSurfaceVariant, contentColor = accentColor),
                                            modifier = Modifier.weight(1f).height(28.dp),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text("EDIT", fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }

                                    Button(
                                        onClick = {
                                            inputName = node.name
                                            showRenameDialog = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = AbyssSurfaceVariant, contentColor = TextPrimary),
                                        modifier = Modifier.weight(1f).height(28.dp),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Icon(Icons.Default.DriveFileRenameOutline, contentDescription = "Rename", modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text("RENAME", fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Button(
                                        onClick = {
                                            inputPath = "/home/$activeUser/Documents"
                                            showCopyDialog = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = AbyssSurfaceVariant, contentColor = TextPrimary),
                                        modifier = Modifier.weight(1f).height(28.dp),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text("COPY", fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Button(
                                        onClick = {
                                            inputPath = "/home/$activeUser/Documents"
                                            showMoveDialog = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = AbyssSurfaceVariant, contentColor = TextPrimary),
                                        modifier = Modifier.weight(1f).height(28.dp),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Icon(Icons.Default.DriveFileMove, contentDescription = "Move", modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text("MOVE", fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Button(
                                        onClick = { showInfoDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = AbyssSurfaceVariant, contentColor = CyberCyan),
                                        modifier = Modifier.weight(1f).height(28.dp),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Icon(Icons.Default.Info, contentDescription = "Info", modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text("INFO", fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Button(
                                        onClick = {
                                            vfs.deleteNode(node.path, permanent = false, userHandle = activeUser)
                                            selectedNode = null
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828), contentColor = Color.White),
                                        modifier = Modifier.weight(1f).height(28.dp),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text("TRASH", fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Preview Content Box
                        Text(
                            text = "CONTENT PREVIEW:",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(4.dp))
                                .background(AbyssBackground)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = when (node) {
                                    is VFSNode.File -> node.content.ifEmpty { "(empty file)" }
                                    is VFSNode.Directory -> "Directory containing ${node.children.size} items."
                                },
                                color = TextPrimary,
                                fontSize = 10.sp,
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
                            text = "Select an item to view metadata & actions.",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }

    // Modal Dialog: Create File
    if (showCreateFileDialog) {
        CustomFileDialog(
            title = "CREATE NEW FILE",
            nameLabel = "Filename (e.g. data.txt, payload.sh)",
            initialName = inputName,
            initialContent = inputContent,
            showContentField = true,
            confirmButtonText = "CREATE FILE",
            accentColor = accentColor,
            onDismiss = { showCreateFileDialog = false },
            onConfirm = { name, content ->
                if (name.isNotBlank()) {
                    vfs.createFile(currentPath, name, content, owner = activeUser)
                }
                showCreateFileDialog = false
            }
        )
    }

    // Modal Dialog: Create Folder
    if (showCreateFolderDialog) {
        CustomFileDialog(
            title = "CREATE NEW FOLDER",
            nameLabel = "Folder Name",
            initialName = inputName,
            showContentField = false,
            confirmButtonText = "CREATE FOLDER",
            accentColor = CyberCyan,
            onDismiss = { showCreateFolderDialog = false },
            onConfirm = { name, _ ->
                if (name.isNotBlank()) {
                    vfs.createDirectory(currentPath, name, owner = activeUser)
                }
                showCreateFolderDialog = false
            }
        )
    }

    // Modal Dialog: Rename Item
    if (showRenameDialog && selectedNode != null) {
        CustomFileDialog(
            title = "RENAME ITEM",
            nameLabel = "New Name",
            initialName = inputName,
            showContentField = false,
            confirmButtonText = "RENAME",
            accentColor = accentColor,
            onDismiss = { showRenameDialog = false },
            onConfirm = { newName, _ ->
                if (newName.isNotBlank()) {
                    vfs.renameNode(selectedNode!!.path, newName, activeUser)
                    selectedNode = null
                }
                showRenameDialog = false
            }
        )
    }

    // Modal Dialog: Edit File
    if (showEditFileDialog && selectedNode is VFSNode.File) {
        CustomFileDialog(
            title = "EDIT FILE: ${selectedNode!!.name}",
            nameLabel = "Filename",
            initialName = selectedNode!!.name,
            initialContent = inputContent,
            showNameField = false,
            showContentField = true,
            confirmButtonText = "SAVE CHANGES",
            accentColor = accentColor,
            onDismiss = { showEditFileDialog = false },
            onConfirm = { _, content ->
                vfs.writeFile(selectedNode!!.path, content, activeUser)
                showEditFileDialog = false
            }
        )
    }

    // Modal Dialog: Move Item
    if (showMoveDialog && selectedNode != null) {
        CustomFileDialog(
            title = "MOVE ITEM",
            nameLabel = "Destination Directory Path",
            initialName = inputPath,
            showContentField = false,
            confirmButtonText = "MOVE HERE",
            accentColor = accentColor,
            onDismiss = { showMoveDialog = false },
            onConfirm = { destPath, _ ->
                if (destPath.isNotBlank()) {
                    vfs.moveNode(selectedNode!!.path, destPath, activeUser)
                    selectedNode = null
                }
                showMoveDialog = false
            }
        )
    }

    // Modal Dialog: Copy Item
    if (showCopyDialog && selectedNode != null) {
        CustomFileDialog(
            title = "COPY ITEM",
            nameLabel = "Destination Directory Path",
            initialName = inputPath,
            showContentField = false,
            confirmButtonText = "COPY HERE",
            accentColor = accentColor,
            onDismiss = { showCopyDialog = false },
            onConfirm = { destPath, _ ->
                if (destPath.isNotBlank()) {
                    vfs.copyNode(selectedNode!!.path, destPath, activeUser)
                    selectedNode = null
                }
                showCopyDialog = false
            }
        )
    }

    // Modal Dialog: Full File Info
    if (showInfoDialog && selectedNode != null) {
        val node = selectedNode!!
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            containerColor = AbyssCard,
            title = {
                Text(
                    text = "FILE PROPERTIES",
                    color = accentColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            },
            text = {
                Column {
                    Text("Name: ${node.name}", color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Text("Type: ${node.metadata.fileType.description}", color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Text("Path: ${node.path}", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text("Owner: ${node.metadata.owner}", color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Text("Permissions: ${node.metadata.permissions}", color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Text("Size: ${formatSize(node.metadata.sizeBytes)}", color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Text("Created: ${dateFormat.format(Date(node.metadata.createdTimestamp))}", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text("Modified: ${dateFormat.format(Date(node.metadata.modifiedTimestamp))}", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text("System Protected: ${node.metadata.isSystemProtected}", color = if (node.metadata.isSystemProtected) Color(0xFFFFB74D) else TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showInfoDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AbyssSurfaceVariant, contentColor = TextPrimary)
                ) {
                    Text("CLOSE", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }
        )
    }
}

@Composable
private fun CustomFileDialog(
    title: String,
    nameLabel: String,
    initialName: String = "",
    initialContent: String = "",
    showNameField: Boolean = true,
    showContentField: Boolean = false,
    confirmButtonText: String,
    accentColor: Color,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var nameState by remember { mutableStateOf(initialName) }
    var contentState by remember { mutableStateOf(initialContent) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AbyssCard,
        title = {
            Text(
                text = title,
                color = accentColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        },
        text = {
            Column {
                if (showNameField) {
                    OutlinedTextField(
                        value = nameState,
                        onValueChange = { nameState = it },
                        label = { Text(nameLabel, fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (showContentField) {
                    OutlinedTextField(
                        value = contentState,
                        onValueChange = { contentState = it },
                        label = { Text("File Content", fontSize = 10.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(nameState, contentState) },
                colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.Black)
            ) {
                Text(confirmButtonText, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = AbyssSurfaceVariant, contentColor = TextMuted)
            ) {
                Text("CANCEL", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
        }
    )
}

private fun getFileIcon(type: VFSFileType, isExecutable: Boolean): ImageVector {
    return when {
        isExecutable || type == VFSFileType.EXE -> Icons.Default.Terminal
        type == VFSFileType.SH -> Icons.Default.Code
        type == VFSFileType.DIRECTORY -> Icons.Default.Folder
        type == VFSFileType.KEY -> Icons.Default.Key
        type == VFSFileType.ENC || type == VFSFileType.SYS -> Icons.Default.Lock
        else -> Icons.Default.Description
    }
}

private fun getFileIconColor(type: VFSFileType, isDir: Boolean, accentColor: Color): Color {
    return when {
        isDir -> CyberCyan
        type == VFSFileType.EXE || type == VFSFileType.SH -> TerminalGreen
        type == VFSFileType.KEY || type == VFSFileType.ENC -> Color(0xFFFFB74D)
        type == VFSFileType.LOG -> Color(0xFF81D4FA)
        else -> accentColor
    }
}

private fun formatSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> "${bytes / (1024 * 1024)} MB"
    }
}
