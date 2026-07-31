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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.backdoor.core.NotificationLevel
import com.example.backdoor.game.AbyssOSManager
import com.example.backdoor.network.models.NetworkNode
import com.example.backdoor.network.models.NodeStatus
import com.example.backdoor.network.models.NodeType
import com.example.ui.theme.AbyssBackground
import com.example.ui.theme.AbyssCard
import com.example.ui.theme.AbyssSurface
import com.example.ui.theme.AbyssSurfaceVariant
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.NeonRed
import com.example.ui.theme.StatusConnected
import com.example.ui.theme.TechPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

/**
 * AbyssNet Graphical Control Panel & Topology Visualizer.
 * Displays discovered network nodes, real-time links, interface states, and detailed device metadata.
 */
@Composable
fun NetworkApp(
    osManager: AbyssOSManager,
    accentColor: Color = AmberAlert,
    modifier: Modifier = Modifier
) {
    val nodes by osManager.networkEngine.nodes.collectAsState()
    val connections by osManager.networkEngine.connections.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedNodeTypeFilter by remember { mutableStateOf<NodeType?>(null) }
    var selectedNode by remember { mutableStateOf<NetworkNode?>(nodes.firstOrNull()) }
    var activeTab by remember { mutableStateOf(0) } // 0: Topology Map, 1: Devices List, 2: Interfaces

    val filteredNodes = nodes.filter { node ->
        val matchesQuery = searchQuery.isEmpty() ||
            node.hostname.contains(searchQuery, ignoreCase = true) ||
            node.ip.contains(searchQuery, ignoreCase = true) ||
            node.ownerId.contains(searchQuery, ignoreCase = true)
        val matchesType = selectedNodeTypeFilter == null || node.nodeType == selectedNodeTypeFilter
        matchesQuery && matchesType
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AbyssBackground)
            .padding(10.dp)
    ) {
        // Top Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(AbyssSurface)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lan,
                    contentDescription = "AbyssNet",
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ABYSSNET TOPOLOGY MONITOR",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Tab Selection
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(AbyssBackground)
                        .padding(2.dp)
                ) {
                    listOf("TOPOLOGY", "DEVICES", "INTERFACES").forEachIndexed { index, label ->
                        val isSelected = activeTab == index
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) accentColor.copy(alpha = 0.25f) else Color.Transparent)
                                .clickable { activeTab = index }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) accentColor else TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Refresh / Scan Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(accentColor.copy(alpha = 0.2f))
                        .border(0.5.dp, accentColor, RoundedCornerShape(6.dp))
                        .clickable {
                            val scanned = osManager.networkEngine.scanNetwork()
                            osManager.showNotification(
                                title = "SUBNET DISCOVERY",
                                message = "Discovered ${scanned.size} active network hosts.",
                                level = NotificationLevel.SUCCESS
                            )
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Scan",
                            tint = accentColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "SCAN SUBNET",
                            color = accentColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Main Content Area based on selected Tab
        when (activeTab) {
            0 -> TopologyMapView(
                nodes = nodes,
                selectedNode = selectedNode,
                onNodeSelect = { selectedNode = it },
                accentColor = accentColor
            )
            1 -> DevicesListView(
                nodes = filteredNodes,
                selectedNode = selectedNode,
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                selectedFilter = selectedNodeTypeFilter,
                onFilterSelect = { selectedNodeTypeFilter = it },
                onNodeSelect = { selectedNode = it },
                accentColor = accentColor
            )
            2 -> NetworkInterfacesView(osManager = osManager, accentColor = accentColor)
        }
    }
}

@Composable
private fun TopologyMapView(
    nodes: List<NetworkNode>,
    selectedNode: NetworkNode?,
    onNodeSelect: (NetworkNode) -> Unit,
    accentColor: Color
) {
    Row(modifier = Modifier.fillMaxSize()) {
        // Topology Canvas Graph View
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(8.dp))
                .background(AbyssCard)
                .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "VIRTUAL NETWORK MAP (LOCAL SUBNET 192.168.1.0/24)",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Network nodes layout grid
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(nodes) { node ->
                        val isSelected = selectedNode?.id == node.id
                        val icon = getNodeIcon(node.nodeType)
                        val statusColor = when (node.status) {
                            NodeStatus.ONLINE -> StatusConnected
                            NodeStatus.OFFLINE -> NeonRed
                            NodeStatus.DEGRADED -> AmberAlert
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) accentColor.copy(alpha = 0.15f) else AbyssSurface)
                                .border(
                                    width = if (isSelected) 1.dp else 0.5.dp,
                                    color = if (isSelected) accentColor else Color.White.copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { onNodeSelect(node) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(statusColor.copy(alpha = 0.15f))
                                        .border(1.dp, statusColor.copy(alpha = 0.4f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = node.hostname,
                                        tint = statusColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = node.hostname,
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "${node.ip} • MAC: ${node.mac}",
                                        color = TextMuted,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(AbyssSurfaceVariant)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = node.nodeType.displayName.uppercase(),
                                        color = CyberCyan,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "${node.latencyMs} ms",
                                    color = StatusConnected,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Node Inspector Side Pane
        Box(
            modifier = Modifier
                .width(240.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(8.dp))
                .background(AbyssCard)
                .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            NodeDetailInspector(node = selectedNode, accentColor = accentColor)
        }
    }
}

@Composable
private fun DevicesListView(
    nodes: List<NetworkNode>,
    selectedNode: NetworkNode?,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedFilter: NodeType?,
    onFilterSelect: (NodeType?) -> Unit,
    onNodeSelect: (NetworkNode) -> Unit,
    accentColor: Color
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Search & Filter controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(AbyssSurface)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = TextMuted,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            BasicTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                singleLine = true,
                textStyle = TextStyle(color = TextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Devices Table View
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(nodes) { node ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(AbyssCard)
                        .border(0.5.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(6.dp))
                        .clickable { onNodeSelect(node) }
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = node.hostname,
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "IP: ${node.ip} | MAC: ${node.mac}",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        text = node.status.name,
                        color = if (node.status == NodeStatus.ONLINE) StatusConnected else NeonRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun NetworkInterfacesView(
    osManager: AbyssOSManager,
    accentColor: Color
) {
    val ifconfig = osManager.networkEngine.getIfconfig()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .background(AbyssCard)
            .padding(14.dp)
    ) {
        LazyColumn {
            items(ifconfig) { line ->
                Text(
                    text = line,
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun NodeDetailInspector(
    node: NetworkNode?,
    accentColor: Color
) {
    if (node == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Select a node to inspect.", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "NODE INSPECTOR",
            color = accentColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(10.dp))

        Text(text = node.hostname, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Text(text = node.ip, color = CyberCyan, fontSize = 12.sp, fontFamily = FontFamily.Monospace)

        Spacer(modifier = Modifier.height(12.dp))

        DetailRow("MAC Address", node.mac)
        DetailRow("Node Type", node.nodeType.displayName)
        DetailRow("Owner ID", node.ownerId)
        DetailRow("Security Level", "Level ${node.securityLevel}")
        DetailRow("Latency", "${node.latencyMs} ms")

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "ACTIVE SERVICES (${node.services.size})",
            color = TechPurple,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(6.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(node.services) { s ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(AbyssSurface)
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "${s.name} :${s.port}", color = TextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text(text = if (s.isOpen) "OPEN" else "CLOSED", color = if (s.isOpen) StatusConnected else NeonRed, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        Text(text = value, color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
    }
}

private fun getNodeIcon(nodeType: NodeType): ImageVector {
    return when (nodeType) {
        NodeType.ROUTER -> Icons.Default.Router
        NodeType.SWITCH -> Icons.Default.Lan
        NodeType.PERSONAL_DEVICE -> Icons.Default.Computer
        NodeType.SERVER -> Icons.Default.Storage
        NodeType.DATABASE -> Icons.Default.Dns
        NodeType.FIREWALL -> Icons.Default.Security
        NodeType.IOT_DEVICE -> Icons.Default.Tv
        NodeType.UNKNOWN_DEVICE -> Icons.Default.Lan
    }
}
