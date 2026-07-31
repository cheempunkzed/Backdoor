package com.example.backdoor.ui.apps

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.backdoor.filesystem.VFSFileType
import com.example.backdoor.filesystem.VFSNode
import com.example.backdoor.filesystem.VFSSortMode
import com.example.backdoor.game.AbyssOSManager
import com.example.backdoor.ui.components.ContextMenuPopup
import com.example.backdoor.ui.components.ContextMenuItem
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FilesApp(
    osManager: AbyssOSManager,
    accentColor: Color = TerminalGreen,
    modifier: Modifier = Modifier
) {
    val vfs = osManager.vfs
    val profile by osManager.userProfile.collectAsState()
    val activeUser = profile?.username ?: "operator"

    val vfsVersionEvent by vfs.updateEvent.collectAsState()

    val process = osManager.processManager.getProcessForApp(com.example.backdoor.game.OsApp.FILES)
    val appState = process?.appState as? com.example.backdoor.core.FilesAppState

    var currentPath by remember { appState?.currentPath ?: mutableStateOf("/home/$activeUser") }
    var searchQuery by remember { mutableStateOf("") }
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

    // Dialog Input states
    var inputName by remember { mutableStateOf("") }
    var inputContent by remember { mutableStateOf("") }
    var inputPath by remember { mutableStateOf("") }

    // Right-click / Long-press context menu
    var contextMenuNode by remember { mutableStateOf<VFSNode?>(null) }

    // Directory list nodes
    val directoryNodes = remember(currentPath, showTrashView, vfsVersionEvent) {
        if (showTrashView) {
            val trashPath = "/home/$activeUser/Trash"
            vfs.listDirectory(trashPath, includeTrash = true) ?: emptyList()
        } else {
            vfs.listDirectory(currentPath, includeHidden = false) ?: emptyList()
        }
    }

    val filteredNodes = remember(directoryNodes, searchQuery) {
        if (searchQuery.isNotBlank()) {
            vfs.findFiles(searchQuery, if (showTrashView) "/home/$activeUser/Trash" else currentPath)
        } else {
            directoryNodes.sortedWith(compareBy({ it !is VFSNode.Directory }, { it.name.lowercase() }))
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AbyssBackground)
            .padding(6.dp)
    ) {
        // =========================================================================
        // TOP AREA: SELECTED ITEM INSPECTOR & ACTION BUTTONS
        // =========================================================================
        TopInspectorPane(
            selectedNode = selectedNode,
            activeUser = activeUser,
            accentColor = accentColor,
            onOpen = { node ->
                if (node is VFSNode.Directory) {
                    currentPath = node.path
                    selectedNode = null
                } else if (node is VFSNode.File) {
                    selectedNode = node
                    inputContent = node.content
                    showEditFileDialog = true
                }
            },
            onRead = { node ->
                if (node is VFSNode.File) {
                    selectedNode = node
                    showInfoDialog = true
                }
            },
            onEdit = { node ->
                if (node is VFSNode.File) {
                    selectedNode = node
                    inputContent = node.content
                    showEditFileDialog = true
                }
            },
            onCopy = { node ->
                selectedNode = node
                inputPath = currentPath
                showCopyDialog = true
            },
            onMove = { node ->
                selectedNode = node
                inputPath = currentPath
                showMoveDialog = true
            },
            onRename = { node ->
                selectedNode = node
                inputName = node.name
                showRenameDialog = true
            },
            onDelete = { node ->
                if (node != null) {
                    vfs.deleteNode(node.path, permanent = showTrashView, userHandle = activeUser)
                    selectedNode = null
                }
            },
            onCreateFile = {
                inputName = ""
                inputContent = ""
                showCreateFileDialog = true
            },
            onCreateFolder = {
                inputName = ""
                showCreateFolderDialog = true
            }
        )

        Spacer(modifier = Modifier.height(6.dp))

        // =========================================================================
        // BOTTOM AREA: DUAL-PANE (DIR TREE SIDEBAR + CONTENT VIEWER)
        // =========================================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Left Pane: Directory Tree Navigation
            DirectoryTreeSidebar(
                vfs = vfs,
                activeUser = activeUser,
                currentPath = currentPath,
                accentColor = accentColor,
                onSelectDirectory = { path ->
                    currentPath = path
                    selectedNode = null
                },
                modifier = Modifier
                    .width(180.dp)
                    .fillMaxHeight()
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Right Pane: Folder Content Viewer & Search
            FolderContentViewer(
                currentPath = currentPath,
                nodes = filteredNodes,
                selectedNode = selectedNode,
                searchQuery = searchQuery,
                showTrashView = showTrashView,
                activeUser = activeUser,
                accentColor = accentColor,
                onSearchChange = { searchQuery = it },
                onSelectNode = { node -> selectedNode = node },
                onNodeDoubleTap = { node ->
                    if (node is VFSNode.Directory) {
                        currentPath = node.path
                        selectedNode = null
                    } else if (node is VFSNode.File) {
                        selectedNode = node
                        inputContent = node.content
                        showEditFileDialog = true
                    }
                },
                onNodeLongPress = { node -> contextMenuNode = node },
                onToggleTrash = {
                    showTrashView = !showTrashView
                    selectedNode = null
                },
                onNavigateUp = {
                    val parent = vfs.getNode(currentPath)?.parentPath ?: "/"
                    currentPath = parent
                    selectedNode = null
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
    }

    // Context Menu for Items
    val targetCtxNode = contextMenuNode
    if (targetCtxNode != null) {
        ContextMenuPopup(
            visible = true,
            onDismissRequest = { contextMenuNode = null },
            title = targetCtxNode.name,
            accentColor = accentColor,
            items = buildList {
                if (targetCtxNode is VFSNode.Directory) {
                    add(
                        ContextMenuItem(
                            label = "Open Directory",
                            icon = Icons.Default.Folder,
                            onClick = {
                                currentPath = targetCtxNode.path
                                selectedNode = null
                            }
                        )
                    )
                } else {
                    add(
                        ContextMenuItem(
                            label = "Read Content",
                            icon = Icons.Default.Description,
                            onClick = {
                                selectedNode = targetCtxNode
                                showInfoDialog = true
                            }
                        )
                    )
                    add(
                        ContextMenuItem(
                            label = "Edit File",
                            icon = Icons.Default.Edit,
                            onClick = {
                                selectedNode = targetCtxNode
                                inputContent = (targetCtxNode as VFSNode.File).content
                                showEditFileDialog = true
                            }
                        )
                    )
                }
                add(
                    ContextMenuItem(
                        label = "Copy Node",
                        icon = Icons.Default.ContentCopy,
                        onClick = {
                            selectedNode = targetCtxNode
                            inputPath = currentPath
                            showCopyDialog = true
                        }
                    )
                )
                add(
                    ContextMenuItem(
                        label = "Move Node",
                        icon = Icons.Default.DriveFileMove,
                        onClick = {
                            selectedNode = targetCtxNode
                            inputPath = currentPath
                            showMoveDialog = true
                        }
                    )
                )
                add(
                    ContextMenuItem(
                        label = "Rename Node",
                        icon = Icons.Default.DriveFileRenameOutline,
                        onClick = {
                            selectedNode = targetCtxNode
                            inputName = targetCtxNode.name
                            showRenameDialog = true
                        }
                    )
                )
                add(
                    ContextMenuItem(
                        label = "Delete Node",
                        icon = Icons.Default.Delete,
                        isDanger = true,
                        onClick = {
                            vfs.deleteNode(targetCtxNode.path, permanent = showTrashView, userHandle = activeUser)
                            selectedNode = null
                        }
                    )
                )
            }
        )
    }

    // Modals & Dialogs
    if (showCreateFileDialog) {
        CreateFileDialog(
            onDismiss = { showCreateFileDialog = false },
            onCreate = { name, content ->
                vfs.createFile(dirPath = currentPath, fileName = name, content = content, owner = activeUser)
                showCreateFileDialog = false
            },
            accentColor = accentColor
        )
    }

    if (showCreateFolderDialog) {
        CreateFolderDialog(
            onDismiss = { showCreateFolderDialog = false },
            onCreate = { folderName ->
                vfs.createDirectory(dirPath = currentPath, dirName = folderName, owner = activeUser)
                showCreateFolderDialog = false
            },
            accentColor = accentColor
        )
    }

    if (showRenameDialog && selectedNode != null) {
        RenameDialog(
            currentName = selectedNode?.name ?: "",
            onDismiss = { showRenameDialog = false },
            onRename = { newName ->
                selectedNode?.let { vfs.renameNode(it.path, newName, activeUser) }
                showRenameDialog = false
                selectedNode = null
            },
            accentColor = accentColor
        )
    }

    if (showEditFileDialog && selectedNode is VFSNode.File) {
        EditFileDialog(
            node = selectedNode as VFSNode.File,
            onDismiss = { showEditFileDialog = false },
            onSave = { newContent ->
                (selectedNode as? VFSNode.File)?.let { vfs.writeFile(it.path, newContent, activeUser) }
                showEditFileDialog = false
            },
            accentColor = accentColor
        )
    }

    if (showInfoDialog && selectedNode != null) {
        FileInfoDialog(
            node = selectedNode!!,
            onDismiss = { showInfoDialog = false },
            accentColor = accentColor
        )
    }

    if (showMoveDialog && selectedNode != null) {
        PathPromptDialog(
            title = "MOVE NODE",
            promptLabel = "Destination Directory Path:",
            defaultPath = currentPath,
            onDismiss = { showMoveDialog = false },
            onConfirm = { destDir ->
                selectedNode?.let { vfs.moveNode(it.path, "$destDir/${it.name}", activeUser) }
                showMoveDialog = false
                selectedNode = null
            },
            accentColor = accentColor
        )
    }

    if (showCopyDialog && selectedNode != null) {
        PathPromptDialog(
            title = "COPY NODE",
            promptLabel = "Target Directory Path:",
            defaultPath = currentPath,
            onDismiss = { showCopyDialog = false },
            onConfirm = { destDir ->
                selectedNode?.let { vfs.copyNode(it.path, "$destDir/${it.name}_copy", activeUser) }
                showCopyDialog = false
                selectedNode = null
            },
            accentColor = accentColor
        )
    }
}

// =============================================================================
// TOP INSPECTOR PANE
// =============================================================================
@Composable
private fun TopInspectorPane(
    selectedNode: VFSNode?,
    activeUser: String,
    accentColor: Color,
    onOpen: (VFSNode) -> Unit,
    onRead: (VFSNode) -> Unit,
    onEdit: (VFSNode) -> Unit,
    onCopy: (VFSNode) -> Unit,
    onMove: (VFSNode) -> Unit,
    onRename: (VFSNode) -> Unit,
    onDelete: (VFSNode?) -> Unit,
    onCreateFile: () -> Unit,
    onCreateFolder: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AbyssSurface)
            .border(0.5.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(10.dp)
            .animateContentSize()
    ) {
        Column {
            // Selected Properties Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "INSPECTOR: " + (selectedNode?.name ?: "[ No File Selected ]"),
                    color = accentColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = onCreateFile,
                        colors = ButtonDefaults.buttonColors(containerColor = AbyssSurfaceVariant, contentColor = TextPrimary),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("+File", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }

                    Button(
                        onClick = onCreateFolder,
                        colors = ButtonDefaults.buttonColors(containerColor = AbyssSurfaceVariant, contentColor = TextPrimary),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Icon(Icons.Default.CreateNewFolder, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("+Folder", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Meta detail grid
            val meta = selectedNode?.metadata
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PropertyMetaTag(label = "PATH", value = selectedNode?.path ?: "---")
                PropertyMetaTag(label = "SIZE", value = if (meta != null) formatBytes(meta.sizeBytes) else "---")
                PropertyMetaTag(label = "OWNER", value = meta?.owner ?: "---")
                PropertyMetaTag(label = "PERM", value = meta?.permissions ?: "---")
                PropertyMetaTag(label = "TYPE", value = meta?.fileType?.name ?: "---")
                PropertyMetaTag(label = "MODIFIED", value = if (meta != null) formatDate(meta.modifiedTimestamp) else "---")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val hasSelection = selectedNode != null

                ActionButton(label = "Open", icon = Icons.Default.OpenInNew, enabled = hasSelection, accentColor = accentColor) {
                    selectedNode?.let(onOpen)
                }
                ActionButton(label = "Read", icon = Icons.Default.Description, enabled = hasSelection && selectedNode is VFSNode.File, accentColor = accentColor) {
                    selectedNode?.let(onRead)
                }
                ActionButton(label = "Edit", icon = Icons.Default.Edit, enabled = hasSelection && selectedNode is VFSNode.File, accentColor = accentColor) {
                    selectedNode?.let(onEdit)
                }
                ActionButton(label = "Copy", icon = Icons.Default.ContentCopy, enabled = hasSelection, accentColor = accentColor) {
                    selectedNode?.let(onCopy)
                }
                ActionButton(label = "Move", icon = Icons.Default.DriveFileMove, enabled = hasSelection, accentColor = accentColor) {
                    selectedNode?.let(onMove)
                }
                ActionButton(label = "Rename", icon = Icons.Default.DriveFileRenameOutline, enabled = hasSelection, accentColor = accentColor) {
                    selectedNode?.let(onRename)
                }
                ActionButton(label = "Delete", icon = Icons.Default.Delete, enabled = hasSelection, isDanger = true, accentColor = accentColor) {
                    onDelete(selectedNode)
                }
            }
        }
    }
}

@Composable
private fun PropertyMetaTag(label: String, value: String) {
    Column {
        Text(text = label, color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
        Text(
            text = value,
            color = TextPrimary,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ActionButton(
    label: String,
    icon: ImageVector,
    enabled: Boolean,
    isDanger: Boolean = false,
    accentColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isDanger) Color(0xFFD32F2F).copy(alpha = 0.8f) else accentColor.copy(alpha = 0.2f),
            disabledContainerColor = AbyssSurfaceVariant.copy(alpha = 0.4f),
            contentColor = if (isDanger) Color.White else accentColor,
            disabledContentColor = TextMuted.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.height(28.dp)
    ) {
        Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(12.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
}

// =============================================================================
// LEFT SIDEBAR: DIRECTORY TREE
// =============================================================================
@Composable
private fun DirectoryTreeSidebar(
    vfs: com.example.backdoor.filesystem.VirtualFileSystem,
    activeUser: String,
    currentPath: String,
    accentColor: Color,
    onSelectDirectory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val roots = remember {
        listOf(
            "/",
            "/home/$activeUser",
            "/home/$activeUser/Desktop",
            "/home/$activeUser/Downloads",
            "/home/$activeUser/Documents",
            "/bin",
            "/etc",
            "/system",
            "/logs"
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(AbyssSurface)
            .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
            .padding(6.dp)
    ) {
        Column {
            Text(
                text = "DIRECTORY TREE",
                color = accentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(roots) { path ->
                    val isSelected = currentPath == path
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) accentColor.copy(alpha = 0.25f) else Color.Transparent)
                            .clickable { onSelectDirectory(path) }
                            .padding(horizontal = 6.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = path,
                            tint = if (isSelected) accentColor else CyberCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = path.split("/").lastOrNull().takeIf { !it.isNullOrEmpty() } ?: "/",
                            color = if (isSelected) TextPrimary else TextMuted,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// RIGHT PANE: FOLDER CONTENT VIEWER
// =============================================================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FolderContentViewer(
    currentPath: String,
    nodes: List<VFSNode>,
    selectedNode: VFSNode?,
    searchQuery: String,
    showTrashView: Boolean,
    activeUser: String,
    accentColor: Color,
    onSearchChange: (String) -> Unit,
    onSelectNode: (VFSNode) -> Unit,
    onNodeDoubleTap: (VFSNode) -> Unit,
    onNodeLongPress: (VFSNode) -> Unit,
    onToggleTrash: () -> Unit,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(AbyssSurface)
            .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Column {
            // Path Navigation & Search Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (currentPath != "/" && !showTrashView) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(AbyssSurfaceVariant)
                                .clickable(onClick = onNavigateUp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Up",
                                tint = accentColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Text(
                        text = if (showTrashView) "/home/$activeUser/Trash" else currentPath,
                        color = accentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Search bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = { Text("Filter...", fontSize = 10.sp, color = TextMuted) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Filter", tint = accentColor, modifier = Modifier.size(12.dp)) },
                        modifier = Modifier
                            .width(130.dp)
                            .height(32.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = AbyssSurfaceVariant,
                            focusedContainerColor = AbyssBackground,
                            unfocusedContainerColor = AbyssBackground,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Button(
                        onClick = onToggleTrash,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (showTrashView) Color(0xFFD32F2F) else AbyssSurfaceVariant,
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Trash", modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(if (showTrashView) "Exit Trash" else "Trash", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // File items grid
            if (nodes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "[ Directory Empty ]",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 130.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(nodes, key = { it.path }) { node ->
                        val isSelected = selectedNode?.path == node.path
                        val isDir = node is VFSNode.Directory

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) accentColor.copy(alpha = 0.22f) else AbyssSurfaceVariant)
                                .border(
                                    width = if (isSelected) 1.dp else 0.5.dp,
                                    color = if (isSelected) accentColor else Color.White.copy(alpha = 0.05f),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .combinedClickable(
                                    onClick = { onSelectNode(node) },
                                    onDoubleClick = { onNodeDoubleTap(node) },
                                    onLongClick = { onNodeLongPress(node) }
                                )
                                .padding(8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = if (isDir) Icons.Default.Folder else getFileIconVector(node.metadata.fileType),
                                    contentDescription = node.name,
                                    tint = if (isDir) CyberCyan else accentColor,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = node.name,
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (isDir) "DIR" else formatBytes(node.metadata.sizeBytes),
                                    color = TextMuted,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper icons & formatting
fun getFileIconVector(type: VFSFileType): ImageVector {
    return when (type) {
        VFSFileType.TXT, VFSFileType.LOG -> Icons.Default.Description
        VFSFileType.CFG, VFSFileType.SYS -> Icons.Default.Code
        VFSFileType.KEY, VFSFileType.ENC -> Icons.Default.Key
        VFSFileType.EXE, VFSFileType.SH -> Icons.Default.Terminal
        else -> Icons.Default.Description
    }
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> "${bytes / (1024 * 1024)} MB"
    }
}

private fun formatDate(timestamp: Long): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))
}

// =============================================================================
// MODAL DIALOGS
// =============================================================================
@Composable
private fun CreateFileDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String) -> Unit,
    accentColor: Color
) {
    var fileName by remember { mutableStateOf("") }
    var fileContent by remember { mutableStateOf("") }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(320.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(AbyssCard)
                .border(1.dp, accentColor, RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Column {
                Text("CREATE FILE", color = accentColor, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text("File Name (e.g. note.txt)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accentColor, unfocusedBorderColor = TextMuted),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = fileContent,
                    onValueChange = { fileContent = it },
                    label = { Text("Content") },
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accentColor, unfocusedBorderColor = TextMuted),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = AbyssSurfaceVariant)) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (fileName.isNotBlank()) onCreate(fileName.trim(), fileContent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.Black)
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateFolderDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit,
    accentColor: Color
) {
    var folderName by remember { mutableStateOf("") }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(300.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(AbyssCard)
                .border(1.dp, accentColor, RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Column {
                Text("CREATE FOLDER", color = accentColor, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = folderName,
                    onValueChange = { folderName = it },
                    label = { Text("Folder Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accentColor, unfocusedBorderColor = TextMuted),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = AbyssSurfaceVariant)) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (folderName.isNotBlank()) onCreate(folderName.trim())
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.Black)
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}

@Composable
private fun RenameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onRename: (String) -> Unit,
    accentColor: Color
) {
    var newName by remember { mutableStateOf(currentName) }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(300.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(AbyssCard)
                .border(1.dp, accentColor, RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Column {
                Text("RENAME NODE", color = accentColor, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("New Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accentColor, unfocusedBorderColor = TextMuted),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = AbyssSurfaceVariant)) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (newName.isNotBlank()) onRename(newName.trim())
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.Black)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
private fun EditFileDialog(
    node: VFSNode.File,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    accentColor: Color
) {
    var content by remember { mutableStateOf(node.content) }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(8.dp))
                .background(AbyssCard)
                .border(1.dp, accentColor, RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Column {
                Text("EDIT FILE: ${node.name}", color = accentColor, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    minLines = 8,
                    maxLines = 14,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accentColor, unfocusedBorderColor = TextMuted),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = AbyssSurfaceVariant)) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(content) },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.Black)
                    ) {
                        Text("Save File")
                    }
                }
            }
        }
    }
}

@Composable
private fun FileInfoDialog(
    node: VFSNode,
    onDismiss: () -> Unit,
    accentColor: Color
) {
    val meta = node.metadata

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(320.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(AbyssCard)
                .border(1.dp, accentColor, RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Column {
                Text("FILE PROPERTIES", color = accentColor, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(10.dp))

                Text("Name: ${node.name}", color = TextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                Text("Path: ${node.path}", color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Text("Type: ${meta.fileType}", color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Text("Size: ${formatBytes(meta.sizeBytes)}", color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Text("Owner: ${meta.owner}", color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Text("Permissions: ${meta.permissions}", color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Text("Modified: ${formatDate(meta.modifiedTimestamp)}", color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)

                if (node is VFSNode.File) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("CONTENT PREVIEW:", color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(AbyssBackground)
                            .padding(6.dp)
                    ) {
                        Text(
                            text = node.content.take(400),
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.Black)) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
private fun PathPromptDialog(
    title: String,
    promptLabel: String,
    defaultPath: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    accentColor: Color
) {
    var pathText by remember { mutableStateOf(defaultPath) }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(320.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(AbyssCard)
                .border(1.dp, accentColor, RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(title, color = accentColor, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = pathText,
                    onValueChange = { pathText = it },
                    label = { Text(promptLabel) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accentColor, unfocusedBorderColor = TextMuted),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = AbyssSurfaceVariant)) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (pathText.isNotBlank()) onConfirm(pathText.trim())
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.Black)
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}
