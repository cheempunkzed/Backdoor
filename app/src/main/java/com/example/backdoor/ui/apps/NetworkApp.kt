package com.example.backdoor.ui.apps

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.backdoor.corporate.CorporateServer
import com.example.backdoor.corporate.DataCenter
import com.example.backdoor.corporate.IndustryType
import com.example.backdoor.corporate.Organization
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

@Composable
fun NetworkApp(
    osManager: AbyssOSManager,
    accentColor: Color = AmberAlert,
    modifier: Modifier = Modifier
) {
    val nodes by osManager.networkEngine.nodes.collectAsState()
    val orgs by osManager.corporateRepository.organizations.collectAsState()

    val process = osManager.processManager.getProcessForApp(com.example.backdoor.game.OsApp.NETWORK)
    val appState = process?.appState as? com.example.backdoor.core.NetworkAppState ?: remember { com.example.backdoor.core.NetworkAppState() }

    var activeTabStr by remember { appState.selectedTab }
    var activeTab by remember { mutableStateOf(activeTabStr.toIntOrNull() ?: 0) }

    var selectedOrgId by remember { appState.selectedOrgId }
    var searchQuery by remember { appState.searchQuery }
    var selectedIndustry by remember { appState.selectedIndustry }
    var selectedNodeId by remember { appState.selectedNodeId }
    var selectedServerId by remember { appState.selectedServerId }
    var currentSortMode by remember { appState.currentSortMode }
    var zoomLevel by remember { appState.zoomLevel }

    // Sync state
    LaunchedEffect(activeTab) {
        appState.selectedTab.value = activeTab.toString()
    }

    // Default select first org if none selected
    if (selectedOrgId == null && orgs.isNotEmpty()) {
        selectedOrgId = orgs.firstOrNull()?.id
    }

    val selectedOrg = orgs.find { it.id == selectedOrgId } ?: orgs.firstOrNull()

    // Filter organizations (Multi-field searching)
    val filteredOrgs = remember(searchQuery, selectedIndustry, orgs) {
        val clean = searchQuery.trim().lowercase()
        orgs.filter { org ->
            val matchesIndustry = selectedIndustry == null || org.industry == selectedIndustry
            val matchesQuery = if (clean.isEmpty()) {
                true
            } else {
                org.name.lowercase().contains(clean) ||
                org.code.lowercase().contains(clean) ||
                org.domain.lowercase().contains(clean) ||
                org.industry.displayName.lowercase().contains(clean) ||
                org.securityLevel.toString() == clean ||
                "tier ${org.securityLevel}".contains(clean) ||
                org.reputation.toString() == clean ||
                org.servers.size.toString() == clean ||
                "${org.servers.size} servers".contains(clean)
            }
            matchesIndustry && matchesQuery
        }
    }

    // Sorted list
    val sortedOrgs = remember(filteredOrgs, currentSortMode) {
        when (currentSortMode) {
            "NAME" -> filteredOrgs.sortedBy { it.name }
            "SECURITY" -> filteredOrgs.sortedByDescending { it.securityLevel }
            "REPUTATION" -> filteredOrgs.sortedByDescending { it.reputation }
            "SERVERS" -> filteredOrgs.sortedByDescending { it.servers.size }
            else -> filteredOrgs
        }
    }

    var menuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AbyssBackground)
            .padding(8.dp)
    ) {
        // Redesigned HEADER: ONLY Mode Selector & Settings configurations
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(AbyssSurface)
                .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // [Current Mode ▼] selector
            Box {
                var dropdownExpanded by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { dropdownExpanded = true }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val currentModeLabel = when (activeTab) {
                        0 -> "1. CORPORATE GRID"
                        1 -> "2. NETWORK MAP"
                        2 -> "3. ORGANIZATION INTELLIGENCE"
                        3 -> "4. INFRASTRUCTURE"
                        4 -> "5. SECURITY ANALYSIS"
                        5 -> "6. ACTIVITY CENTER"
                        else -> "SELECT MODE"
                    }
                    Text(
                        text = "$currentModeLabel ▼",
                        color = accentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false },
                    modifier = Modifier
                        .background(AbyssCard)
                        .border(0.5.dp, accentColor, RoundedCornerShape(6.dp))
                ) {
                    val modesList = listOf(
                        "1. CORPORATE GRID" to Icons.Default.Business,
                        "2. NETWORK MAP" to Icons.Default.Lan,
                        "3. ORGANIZATION INTELLIGENCE" to Icons.Default.Info,
                        "4. INFRASTRUCTURE" to Icons.Default.Dns,
                        "5. SECURITY ANALYSIS" to Icons.Default.Shield,
                        "6. ACTIVITY CENTER" to Icons.Default.Notifications
                    )
                    modesList.forEachIndexed { idx, (label, icon) ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        tint = if (activeTab == idx) accentColor else TextMuted,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = label,
                                        color = if (activeTab == idx) accentColor else TextPrimary,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = if (activeTab == idx) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            },
                            onClick = {
                                activeTab = idx
                                dropdownExpanded = false
                            },
                            modifier = Modifier.background(
                                if (activeTab == idx) accentColor.copy(alpha = 0.08f) else Color.Transparent
                            )
                        )
                    }
                }
            }

            // [Settings ⚙]
            Box {
                IconButton(
                    onClick = { menuExpanded = !menuExpanded },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Grid Configurations",
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier
                        .background(AbyssCard)
                        .border(0.5.dp, accentColor, RoundedCornerShape(4.dp))
                ) {
                    Text(
                        text = " CONFIGURATIONS",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                    
                    DropdownMenuItem(
                        text = {
                            Text("SORT BY: NAME", color = TextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        },
                        onClick = {
                            currentSortMode = "NAME"
                            menuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text("SORT BY: SECURITY", color = TextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        },
                        onClick = {
                            currentSortMode = "SECURITY"
                            menuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text("SORT BY: REPUTATION", color = TextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        },
                        onClick = {
                            currentSortMode = "REPUTATION"
                            menuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text("SORT BY: SERVERS", color = TextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        },
                        onClick = {
                            currentSortMode = "SERVERS"
                            menuExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Main workspace completely changes depending on mode
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (activeTab) {
                0 -> {
                    CorporateGridModeView(
                        orgs = orgs,
                        sortedOrgs = sortedOrgs,
                        selectedOrg = selectedOrg,
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        selectedIndustry = selectedIndustry,
                        onIndustrySelect = { selectedIndustry = it },
                        onSelectOrg = { selectedOrgId = it },
                        currentSortMode = currentSortMode,
                        onSortChange = { currentSortMode = it },
                        selectedServerId = selectedServerId,
                        onSelectServer = { selectedServerId = it },
                        zoomLevel = zoomLevel,
                        onZoomChange = { zoomLevel = it },
                        onNavigateToIntelligence = { activeTab = 2 },
                        onNavigateToSecurity = { activeTab = 4 },
                        accentColor = accentColor
                    )
                }
                1 -> {
                    NetworkMapModeView(
                        osManager = osManager,
                        nodes = nodes,
                        selectedNodeId = selectedNodeId,
                        onSelectNode = { selectedNodeId = it },
                        selectedOrg = selectedOrg,
                        accentColor = accentColor
                    )
                }
                2 -> {
                    OrganizationIntelligenceModeView(
                        orgs = orgs,
                        selectedOrg = selectedOrg,
                        onSelectOrg = { selectedOrgId = it },
                        osManager = osManager,
                        accentColor = accentColor
                    )
                }
                3 -> {
                    InfrastructureOverviewView(
                        osManager = osManager,
                        accentColor = accentColor
                    )
                }
                4 -> {
                    SecurityAnalysisModeView(
                        osManager = osManager,
                        selectedOrg = selectedOrg,
                        accentColor = accentColor
                    )
                }
                5 -> {
                    ActivityCenterView(
                        osManager = osManager,
                        accentColor = accentColor
                    )
                }
            }
        }
    }
}

// 1. LOCAL TOPOLOGY MAP VIEW (Mode 0) - SCREEN 2 NETWORK MAP
@Composable
private fun LocalTopologyMapView(
    osManager: AbyssOSManager,
    nodes: List<NetworkNode>,
    selectedNodeId: String?,
    onSelectNode: (String) -> Unit,
    accentColor: Color
) {
    var subnetScope by remember { mutableStateOf("LOCAL") } // "LOCAL" vs "WAN"
    var statusFilter by remember { mutableStateOf("ALL") } // "ALL", "ONLINE", "OFFLINE"

    val filteredNodes = nodes.filter { node ->
        when (statusFilter) {
            "ONLINE" -> node.status == NodeStatus.ONLINE
            "OFFLINE" -> node.status == NodeStatus.OFFLINE
            else -> true
        }
    }

    val selectedNode = filteredNodes.find { it.id == selectedNodeId }
        ?: nodes.find { it.id == selectedNodeId }
        ?: filteredNodes.firstOrNull()
        ?: nodes.firstOrNull()

    Column(modifier = Modifier.fillMaxSize()) {
        // FILTER CHIPS ROW: [LOCAL SUBNET] [TARGET CORPORATE WAN] | [ALL] [ONLINE] [OFFLINE]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left filters: Subnet Scope
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("LOCAL SUBNET" to "LOCAL", "TARGET CORPORATE WAN" to "WAN").forEach { (label, scope) ->
                    val isSelected = subnetScope == scope
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) accentColor.copy(alpha = 0.2f) else AbyssSurface)
                            .border(
                                width = if (isSelected) 1.dp else 0.5.dp,
                                color = if (isSelected) accentColor else Color.White.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clickable { subnetScope = scope }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) accentColor else TextMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Right filters: Status Filters
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("ALL", "ONLINE", "OFFLINE").forEach { filter ->
                    val isSelected = statusFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) accentColor.copy(alpha = 0.2f) else AbyssSurface)
                            .border(
                                width = if (isSelected) 1.dp else 0.5.dp,
                                color = if (isSelected) accentColor else Color.White.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clickable { statusFilter = filter }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = filter,
                            color = if (isSelected) accentColor else TextMuted,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // TOP PANEL: LOCAL SUBNET MAP (192.168.1.0/24)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(0.5.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
            colors = CardDefaults.cardColors(containerColor = AbyssCard)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (subnetScope == "LOCAL") "TOP PANEL: LOCAL SUBNET MAP (192.168.1.0/24)" else "TOP PANEL: TARGET CORPORATE WAN (10.0.0.0/16)",
                        color = accentColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(accentColor.copy(alpha = 0.2f))
                            .border(0.5.dp, accentColor, RoundedCornerShape(4.dp))
                            .clickable {
                                val scanned = osManager.networkEngine.scanNetwork()
                                osManager.showNotification(
                                    title = "SUBNET SCAN COMPLETE",
                                    message = "Discovered ${scanned.size} active local interface nodes.",
                                    level = NotificationLevel.SUCCESS
                                )
                            }
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Refresh, contentDescription = "Scan", tint = accentColor, modifier = Modifier.size(10.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("SCAN SUBNET", color = accentColor, fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredNodes) { node ->
                        val isSelected = selectedNode?.id == node.id

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) accentColor.copy(alpha = 0.12f) else AbyssSurface)
                                .border(
                                    width = if (isSelected) 1.dp else 0.5.dp,
                                    color = if (isSelected) accentColor else Color.White.copy(alpha = 0.04f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .clickable { onSelectNode(node.id) }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                // Square checkbox indicator []
                                Box(
                                    modifier = Modifier
                                        .size(13.dp)
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) accentColor else TextMuted.copy(alpha = 0.4f),
                                            shape = RoundedCornerShape(2.dp)
                                        )
                                        .background(if (isSelected) accentColor.copy(alpha = 0.25f) else Color.Transparent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(5.dp)
                                                .background(accentColor, RoundedCornerShape(1.dp))
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Hostname
                                Text(
                                    text = node.hostname,
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.weight(1f)
                                )

                                // IP Address
                                Text(
                                    text = node.ip,
                                    color = CyberCyan,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                            }

                            // Type Tag [ Personal ], [ Router ], etc.
                            Box(
                                modifier = Modifier
                                    .border(0.5.dp, TechPurple.copy(alpha = 0.6f), RoundedCornerShape(3.dp))
                                    .background(TechPurple.copy(alpha = 0.1f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "[ ${node.nodeType.displayName} ]",
                                    color = TechPurple,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // BOTTOM PANEL: LOCAL NODE DETAILS
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp)),
            colors = CardDefaults.cardColors(containerColor = AbyssCard)
        ) {
            if (selectedNode != null) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "BOTTOM PANEL: LOCAL NODE DETAILS",
                        color = accentColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 2-Column Property Grid matching ASCII spec
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            NodeDetailPair("Node Name:", selectedNode.hostname)
                            NodeDetailPair("IP Address:", selectedNode.ip)
                            NodeDetailPair("Node Type:", selectedNode.nodeType.displayName)
                            NodeDetailPair("Latency:", "${selectedNode.latencyMs} ms")
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            NodeDetailPair("MAC Address:", selectedNode.mac)
                            NodeDetailPair("Owner ID:", selectedNode.ownerId)
                            NodeDetailPair("Security Tier:", "Tier ${selectedNode.securityLevel}")
                            NodeDetailPair(
                                label = "Status:",
                                value = if (selectedNode.status == NodeStatus.ONLINE) "ACTIVE" else selectedNode.status.name,
                                statusColor = if (selectedNode.status == NodeStatus.ONLINE) StatusConnected else NeonRed
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(6.dp))

                    // ACTIVE SERVICES (Count)
                    Text(
                        text = "ACTIVE SERVICES (${selectedNode.services.size}):",
                        color = TechPurple,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    if (selectedNode.services.isEmpty()) {
                        Text(
                            text = "  • NO ACTIVE SERVICES DETECTED",
                            color = TextMuted,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    } else {
                        selectedNode.services.forEach { service ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 1.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "  • ${service.name.uppercase()} :${service.port}",
                                    color = TextPrimary,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Box(
                                    modifier = Modifier
                                        .border(0.5.dp, if (service.isOpen) StatusConnected else NeonRed, RoundedCornerShape(2.dp))
                                        .background(if (service.isOpen) StatusConnected.copy(alpha = 0.15f) else NeonRed.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (service.isOpen) "[ OPEN / ACTIVE ]" else "[ CLOSED ]",
                                        color = if (service.isOpen) StatusConnected else NeonRed,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "NO LOCAL NODE SELECTED",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

// Global helper: Node Detail Pair for 2-column layout
@Composable
private fun NodeDetailPair(
    label: String,
    value: String,
    statusColor: Color? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = TextMuted,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = value,
            color = statusColor ?: TextPrimary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

// 2. GRID TREE VIEW WITH ZOOM / SCROLL (Mode 1)
@Composable
private fun CorporateGridTreeView(
    orgs: List<Organization>,
    selectedServerId: String?,
    onSelectServer: (String) -> Unit,
    zoomLevel: Float,
    onZoomChange: (Float) -> Unit,
    accentColor: Color
) {
    var expandedOrgId by remember { mutableStateOf<String?>(orgs.firstOrNull()?.id) }
    var expandedDcId by remember { mutableStateOf<String?>(null) }
    val selectedServer = orgs.flatMap { it.servers }.find { it.id == selectedServerId }

    Row(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(8.dp))
                .background(AbyssCard)
                .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                .padding(10.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "GLOBAL CORPORATE INFRASTRUCTURE TREE",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    
                    // Zoom controller buttons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "ZOOM: ${String.format("%.0f%%", zoomLevel * 100)}",
                            color = TextMuted,
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(AbyssSurface)
                                .clickable { onZoomChange((zoomLevel - 0.1f).coerceIn(0.5f, 1.5f)) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("-", color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(AbyssSurface)
                                .clickable { onZoomChange((zoomLevel + 0.1f).coerceIn(0.5f, 1.5f)) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+", color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(AbyssSurface)
                                .clickable { onZoomChange(1.0f) }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("100%", color = accentColor, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(zoomLevel)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(orgs) { org ->
                            val isOrgExpanded = expandedOrgId == org.id

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AbyssSurface)
                                    .border(0.5.dp, if (isOrgExpanded) accentColor else Color.White.copy(alpha = 0.04f), RoundedCornerShape(6.dp))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { expandedOrgId = if (isOrgExpanded) null else org.id }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Business,
                                            contentDescription = "Org",
                                            tint = accentColor,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${org.name} [${org.code}]",
                                            color = TextPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    Icon(
                                        imageVector = if (isOrgExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = "Toggle",
                                        tint = TextMuted,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }

                                AnimatedVisibility(visible = isOrgExpanded) {
                                    Column(modifier = Modifier.padding(start = 12.dp, end = 6.dp, bottom = 6.dp)) {
                                        org.dataCenters.forEach { dc ->
                                            val isDcExpanded = expandedDcId == dc.id
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 1.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(AbyssBackground)
                                                    .padding(4.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable { expandedDcId = if (isDcExpanded) null else dc.id },
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            imageVector = Icons.Default.Domain,
                                                            contentDescription = "DC",
                                                            tint = CyberCyan,
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = "${dc.name} (${dc.location})",
                                                            color = CyberCyan,
                                                            fontSize = 10.sp,
                                                            fontFamily = FontFamily.Monospace
                                                        )
                                                    }
                                                    Text(
                                                        text = "${dc.serversCount} Server Nodes",
                                                        color = TextMuted,
                                                        fontSize = 8.sp,
                                                        fontFamily = FontFamily.Monospace
                                                    )
                                                }

                                                AnimatedVisibility(visible = isDcExpanded) {
                                                    Column(modifier = Modifier.padding(start = 10.dp, top = 2.dp)) {
                                                        val dcServers = org.servers.filter { it.dataCenterId == dc.id }
                                                        dcServers.forEach { server ->
                                                            Row(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .padding(vertical = 1.dp)
                                                                    .clip(RoundedCornerShape(4.dp))
                                                                    .background(if (selectedServer?.id == server.id) accentColor.copy(alpha = 0.2f) else AbyssSurfaceVariant)
                                                                    .clickable { onSelectServer(server.id) }
                                                                    .padding(horizontal = 6.dp, vertical = 3.dp),
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.SpaceBetween
                                                            ) {
                                                                Text(
                                                                    text = "${server.name} (${server.ip})",
                                                                    color = TextPrimary,
                                                                    fontSize = 9.sp,
                                                                    fontFamily = FontFamily.Monospace
                                                                )
                                                                Text(
                                                                    text = server.type.name,
                                                                    color = StatusConnected,
                                                                    fontSize = 8.sp,
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
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        Box(
            modifier = Modifier
                .width(220.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(8.dp))
                .background(AbyssCard)
                .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            CorporateServerInspector(server = selectedServer, accentColor = accentColor)
        }
    }
}

// 3. ORGANIZATION DIRECTORY VIEW (Mode 2)
@Composable
private fun OrganizationDirectoryView(
    orgs: List<Organization>,
    selectedOrgId: String?,
    onSelectOrg: (String) -> Unit,
    accentColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .background(AbyssCard)
            .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(
                text = "GLOBAL CORPORATE ASSETS DIRECTORY MAP",
                color = TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(6.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(orgs) { index, org ->
                    val isSelected = org.id == selectedOrgId
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isSelected) 1.5.dp else 0.5.dp,
                                color = if (isSelected) accentColor else Color.White.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable { onSelectOrg(org.id) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) accentColor.copy(alpha = 0.12f) else AbyssSurface
                        )
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            // Top line: Ordinal Index & Corp Code
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = String.format("#%02d", index + 1),
                                    color = TextMuted,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "[${org.code}]",
                                    color = accentColor,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            // Name of Corporation
                            Text(
                                text = org.name,
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1
                            )
                            // URL / Domain address
                            Text(
                                text = org.domain,
                                color = CyberCyan,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1
                            )
                            
                            Spacer(modifier = Modifier.height(6.dp))
                            // Bottom line: Sec level & Nodes count
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Sec. level ${org.securityLevel}",
                                    color = TechPurple,
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "${org.servers.size} nodes",
                                    color = StatusConnected,
                                    fontSize = 8.sp,
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

// 4. CUSTOM GLOWING NETWORK TOPOLOGY VIEW (Mode 3)
@Composable
private fun CustomNetworkTopologyView(
    org: Organization?,
    accentColor: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "traffic")
    val animOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .background(AbyssCard)
            .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(
                text = if (org != null) "TOPOLOGY CONNECTIONS: ${org.name.uppercase()} (${org.topologyType.displayName.uppercase()})" else "NETWORK TOPOLOGY ENGINE",
                color = TextMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(AbyssSurface)
                    .border(0.5.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(6.dp))
            ) {
                // Interactive glowing Canvas topology representation
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // 1. Define physical node locations
                    val gw = Pair(w * 0.5f, h * 0.5f)    // Central gateway
                    val web = Pair(w * 0.18f, h * 0.32f) // Public web server
                    val dns = Pair(w * 0.5f, h * 0.15f)  // Core DNS resolver
                    val db = Pair(w * 0.82f, h * 0.5f)   // High-security Database
                    val auth = Pair(w * 0.5f, h * 0.85f) // Authentication node
                    val backup = Pair(w * 0.82f, h * 0.85f) // Storage Vault

                    // Security Zones drawing (DMZ, SECURED INTERNAL, DATABASE EXCLUSIVITY)
                    // DMZ (Web & DNS)
                    drawRect(
                        color = TechPurple.copy(alpha = 0.04f),
                        topLeft = androidx.compose.ui.geometry.Offset(0f, 0f),
                        size = androidx.compose.ui.geometry.Size(w * 0.65f, h * 0.45f)
                    )
                    // Secured Internal (Auth, Database, Vault)
                    drawRect(
                        color = StatusConnected.copy(alpha = 0.04f),
                        topLeft = androidx.compose.ui.geometry.Offset(w * 0.35f, h * 0.35f),
                        size = androidx.compose.ui.geometry.Size(w * 0.65f, h * 0.65f)
                    )

                    // Helper connection lines
                    val connections = listOf(
                        Pair(gw, web),
                        Pair(gw, dns),
                        Pair(gw, db),
                        Pair(gw, auth),
                        Pair(db, backup),
                        Pair(auth, backup)
                    )

                    // Draw connections with dynamic traffic glow
                    connections.forEach { (start, end) ->
                        drawLine(
                            color = Color.White.copy(alpha = 0.12f),
                            start = androidx.compose.ui.geometry.Offset(start.first, start.second),
                            end = androidx.compose.ui.geometry.Offset(end.first, end.second),
                            strokeWidth = 2f
                        )

                        // Draw sliding traffic packet dot
                        val packetX = start.first + (end.first - start.first) * animOffset
                        val packetY = start.second + (end.second - start.second) * animOffset
                        drawCircle(
                            color = CyberCyan,
                            radius = 4f,
                            center = androidx.compose.ui.geometry.Offset(packetX, packetY)
                        )
                    }

                    // Drawing pulsing glow circles at nodes
                    drawCircle(color = accentColor.copy(alpha = 0.15f), radius = 24f, center = androidx.compose.ui.geometry.Offset(gw.first, gw.second))
                    drawCircle(color = TechPurple.copy(alpha = 0.15f), radius = 18f, center = androidx.compose.ui.geometry.Offset(web.first, web.second))
                    drawCircle(color = CyberCyan.copy(alpha = 0.15f), radius = 18f, center = androidx.compose.ui.geometry.Offset(dns.first, dns.second))
                    drawCircle(color = NeonRed.copy(alpha = 0.15f), radius = 18f, center = androidx.compose.ui.geometry.Offset(db.first, db.second))
                    drawCircle(color = StatusConnected.copy(alpha = 0.15f), radius = 18f, center = androidx.compose.ui.geometry.Offset(auth.first, auth.second))
                    drawCircle(color = StatusConnected.copy(alpha = 0.15f), radius = 18f, center = androidx.compose.ui.geometry.Offset(backup.first, backup.second))

                    // Draw Node points
                    drawCircle(color = accentColor, radius = 8f, center = androidx.compose.ui.geometry.Offset(gw.first, gw.second))
                    drawCircle(color = TechPurple, radius = 6f, center = androidx.compose.ui.geometry.Offset(web.first, web.second))
                    drawCircle(color = CyberCyan, radius = 6f, center = androidx.compose.ui.geometry.Offset(dns.first, dns.second))
                    drawCircle(color = NeonRed, radius = 6f, center = androidx.compose.ui.geometry.Offset(db.first, db.second))
                    drawCircle(color = StatusConnected, radius = 6f, center = androidx.compose.ui.geometry.Offset(auth.first, auth.second))
                    drawCircle(color = StatusConnected, radius = 6f, center = androidx.compose.ui.geometry.Offset(backup.first, backup.second))
                }

                // Node Tooltip Labels
                Box(modifier = Modifier.fillMaxSize()) {
                    Text("CORE-GW-01", color = TextPrimary, fontSize = 8.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.align(Alignment.Center).padding(top = 28.dp))
                    Text("PUBLIC-WEB", color = TechPurple, fontSize = 8.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.align(Alignment.TopStart).padding(start = 10.dp, top = 25.dp))
                    Text("DNS-RESOLVER", color = CyberCyan, fontSize = 8.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.align(Alignment.TopCenter).padding(top = 40.dp))
                    Text("DB-CLUSTER-NODE", color = NeonRed, fontSize = 8.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 10.dp, bottom = 40.dp))
                    Text("AUTH-DIRECTORY", color = StatusConnected, fontSize = 8.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 45.dp))
                    Text("STORAGE-VAULT", color = TextMuted, fontSize = 8.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.align(Alignment.BottomEnd).padding(end = 15.dp, bottom = 45.dp))
                    
                    // Zone Overlay Text
                    Text("DMZ PUBLIC ZONE (UNSECURED)", color = TechPurple, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, modifier = Modifier.align(Alignment.TopStart).padding(8.dp))
                    Text("INTERNAL INTRA-SEC ZONE", color = StatusConnected, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp))
                }
            }
        }
    }
}

// 5. INFRASTRUCTURE OVERVIEW VIEW (Mode 4) - SCREEN 4 INFRASTRUCTURE
@Composable
private fun InfrastructureOverviewView(
    osManager: AbyssOSManager,
    accentColor: Color
) {
    val ifconfig = osManager.networkEngine.getIfconfig()

    Column(modifier = Modifier.fillMaxSize()) {
        // TOP PANEL: NETWORK INTERFACES CONFIGURATION
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(0.5.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
            colors = CardDefaults.cardColors(containerColor = AbyssCard)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = "TOP PANEL: NETWORK INTERFACES CONFIGURATION",
                    color = accentColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Parse or display ifconfig interface blocks neatly
                    val formattedBlocks = parseIfconfigBlocks(ifconfig)
                    items(formattedBlocks) { blockText ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(AbyssSurface)
                                .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = blockText,
                                color = TextPrimary,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 13.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // BOTTOM PANEL: OFFENSIVE SECURITY ENGINE
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.1f)
                .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp)),
            colors = CardDefaults.cardColors(containerColor = AbyssCard)
        ) {
            Box(modifier = Modifier.padding(10.dp)) {
                SecurityTabView(osManager = osManager, accentColor = accentColor)
            }
        }
    }
}

// Helper to chunk lines of ifconfig output into interface blocks
private fun parseIfconfigBlocks(ifconfigLines: List<String>): List<String> {
    if (ifconfigLines.isEmpty()) {
        return listOf(
            "eth0: flags=4163<UP,BROADCAST,RUNNING,MULTICAST> mtu 1500\n      inet 192.168.1.100  netmask 255.255.255.0  broadcast 192.168.1.255\n      ether 00:1A:2B:3C:4D:5E  txqueuelen 1000 (Ethernet)\n      RX packets 14209 (9.8 MB)  |  TX packets 11042 (1.4 MB)",
            "wlan0: flags=4099<UP,BROADCAST,MULTICAST> mtu 1500\n       inet 10.0.0.42  netmask 255.255.255.0  broadcast 10.0.0.255\n       ether 00:1A:2B:3C:4D:5F  txqueuelen 1000 (Wi-Fi)\n       RX packets 0 (0.0 B)  |  TX packets 0 (0.0 B)",
            "lo: flags=73<UP,LOOPBACK,RUNNING> mtu 65536\n    inet 127.0.0.1  netmask 255.0.0.0"
        )
    }

    val blocks = mutableListOf<String>()
    var currentBlock = StringBuilder()

    for (line in ifconfigLines) {
        if (line.isBlank() || (line.firstOrNull()?.isLetterOrDigit() == true && !line.startsWith(" ") && currentBlock.isNotEmpty())) {
            if (currentBlock.isNotBlank()) {
                blocks.add(currentBlock.toString().trimEnd())
                currentBlock = StringBuilder()
            }
        }
        if (currentBlock.isNotEmpty()) {
            currentBlock.append("\n")
        }
        currentBlock.append(line)
    }
    if (currentBlock.isNotBlank()) {
        blocks.add(currentBlock.toString().trimEnd())
    }

    return if (blocks.isEmpty()) ifconfigLines else blocks
}

// 6. HELPER COMPONENT: Security Tab (Inside Infrastructure Overview)
@Composable
private fun SecurityTabView(
    osManager: AbyssOSManager,
    accentColor: Color
) {
    val framework = osManager.securityFramework
    val modules = framework.getRegisteredModules()
    val orgs by osManager.corporateRepository.organizations.collectAsState()
    val tasks by framework.activeTasks.collectAsState()

    var targetInput by remember { mutableStateOf("aegis-corp.com") }
    var selectedModuleId by remember { mutableStateOf(modules.firstOrNull()?.id ?: "mod-service-discovery") }

    val activeTask = tasks.firstOrNull { it.target == targetInput || it.status == com.example.backdoor.security.framework.TaskStatus.RUNNING }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "BOTTOM PANEL: OFFENSIVE SECURITY ENGINE",
            color = accentColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(6.dp))

        // TARGET & EXECUTE AUDIT ROW
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Target input box
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(AbyssSurface)
                    .border(0.5.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TARGET: ",
                    color = accentColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                BasicTextField(
                    value = targetInput,
                    onValueChange = { targetInput = it },
                    singleLine = true,
                    textStyle = TextStyle(color = TextPrimary, fontSize = 9.sp, fontFamily = FontFamily.Monospace),
                    modifier = Modifier.weight(1f)
                )
            }

            // EXECUTE AUDIT Button
            val isRunning = activeTask != null
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isRunning) NeonRed.copy(alpha = 0.2f) else accentColor.copy(alpha = 0.2f))
                    .border(1.dp, if (isRunning) NeonRed else accentColor, RoundedCornerShape(4.dp))
                    .clickable(enabled = !isRunning) {
                        framework.runTask(
                            moduleId = selectedModuleId,
                            target = targetInput,
                            osManager = osManager
                        )
                        osManager.showNotification(
                            title = "AUDIT STARTED",
                            message = "Launching module on target $targetInput...",
                            level = NotificationLevel.INFO
                        )
                    }
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isRunning) "[ SCANNING... ]" else "[ EXECUTE AUDIT ]",
                    color = if (isRunning) NeonRed else accentColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(6.dp))

        // SECURITY MODULES list
        Text(
            text = "SECURITY MODULES:",
            color = TechPurple,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(4.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(modules) { mod ->
                val isSelected = mod.id == selectedModuleId
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSelected) accentColor.copy(alpha = 0.12f) else AbyssSurface)
                        .border(
                            width = if (isSelected) 1.dp else 0.5.dp,
                            color = if (isSelected) accentColor else Color.White.copy(alpha = 0.06f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .clickable { selectedModuleId = mod.id }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        // Bullet indicator [•] or [ ]
                        Text(
                            text = if (isSelected) "[•] " else "[ ] ",
                            color = if (isSelected) accentColor else TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = mod.name,
                                color = if (isSelected) accentColor else TextPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = mod.description,
                                color = TextMuted,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        }
    }
}

// 7. HELPER COMPONENT: Organization Profile (with expand tabs)
@Composable
private fun OrganizationProfileView(
    org: Organization,
    osManager: AbyssOSManager,
    accentColor: Color
) {
    var activeProfileTab by remember { mutableStateOf("INFRA") } // INFRA, SECURITY, CORP, ACTIVITY

    Column(modifier = Modifier.fillMaxSize()) {
        // High-level header info
        Text(
            text = "ORGANIZATION DETAILED SPECIFICATION",
            color = accentColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(org.name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Text("Code: ${org.code} • ${org.domain}", color = CyberCyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        
        Spacer(modifier = Modifier.height(6.dp))
        Text(org.description, color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace, maxLines = 2)
        Spacer(modifier = Modifier.height(6.dp))

        // Custom Profile Tabs row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val tabs = listOf("INFRA" to "INFRASTRUCTURE", "SECURITY" to "SECURITY", "CORP" to "CORPORATE", "ACTIVITY" to "ACTIVITY")
            tabs.forEach { (key, label) ->
                val isSel = activeProfileTab == key
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSel) accentColor.copy(alpha = 0.2f) else AbyssSurface)
                        .border(0.5.dp, if (isSel) accentColor else Color.White.copy(alpha = 0.04f), RoundedCornerShape(4.dp))
                        .clickable { activeProfileTab = key }
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(label.take(6), color = if (isSel) accentColor else TextMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Tab Content view
        Box(modifier = Modifier.weight(1f)) {
            when (activeProfileTab) {
                "INFRA" -> {
                    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        Text("DATA CENTER FACILITIES (${org.dataCenters.size})", color = CyberCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(4.dp))
                        org.dataCenters.forEach { dc ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(AbyssSurface)
                                    .padding(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(dc.name, color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    Text(dc.location, color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Cooling: ${dc.coolingStatus}", color = StatusConnected, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                    Text("Power: ${dc.powerStatus}", color = TechPurple, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text("SERVERS CLUSTER (${org.servers.size})", color = StatusConnected, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(4.dp))
                        org.servers.forEach { server ->
                            var servicesExpanded by remember { mutableStateOf(false) }
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(AbyssSurface)
                                    .clickable { servicesExpanded = !servicesExpanded }
                                    .padding(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(server.name, color = TextPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    Text(server.ip, color = CyberCyan, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                }
                                Text("Class: ${server.type.displayName}", color = TextMuted, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                
                                AnimatedVisibility(visible = servicesExpanded) {
                                    Column(modifier = Modifier.padding(start = 8.dp, top = 4.dp)) {
                                        Text("EXPOSED SERVICES (${server.services.size}):", color = TechPurple, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                        server.services.forEach { svc ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("${svc.name} :${svc.port}", color = TextPrimary, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                                Text(if (svc.isOpen) "OPEN" else "CLOSED", color = if (svc.isOpen) StatusConnected else NeonRed, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                "SECURITY" -> {
                    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        val secPercent = 100 - (org.securityLevel * 14)
                        Text("SECURITY MATRIX ANALYSIS", color = accentColor, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(4.dp))
                        DetailRow("Clearance Required", "Tier ${org.securityLevel} / 5")
                        DetailRow("Reputation Rating", "${org.reputation} / 100")
                        DetailRow("Subnet Range", org.subnet)
                        DetailRow("Topology Blueprint", org.topologyType.displayName)
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("FIREWALL SECURITY STATUS", color = CyberCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Tier ${org.securityLevel} Intrusion Detection System Active", color = TextPrimary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        LinearProgressIndicator(
                            progress = { secPercent / 100f },
                            color = CyberCyan,
                            trackColor = AbyssSurface,
                            modifier = Modifier.fillMaxWidth().height(4.dp).padding(vertical = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("VULNERABILITY ASSESSMENT REPORT", color = NeonRed, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(4.dp))
                        val vulnerabilities = listOf(
                            "SSH service banner disclosing old version",
                            "Anonymous Rsync configuration allowing full structure read",
                            "Unencrypted LDAP authentication protocol in transit",
                            "Outdated HAProxy server with known buffer overflow CVE"
                        )
                        vulnerabilities.take(org.securityLevel).forEach { vul ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                                Icon(Icons.Default.Warning, contentDescription = "Vuln", tint = NeonRed, modifier = Modifier.size(10.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(vul, color = TextPrimary, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
                "CORP" -> {
                    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        Text("CORPORATE RECORD PROFILE", color = accentColor, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(4.dp))
                        DetailRow("Sector Classification", org.industry.displayName)
                        DetailRow("Annual Budget", "$${String.format("%,d", org.budget)}")
                        DetailRow("Personnel Count", String.format("%,d", org.employeeCount))
                        
                        // Fake cyberpunk simulated corp information
                        val founded = 2028 + (org.id.hashCode() % 15).coerceAtLeast(0)
                        val hq = org.dataCenters.firstOrNull()?.location ?: "Sector-9 Mega-City"
                        DetailRow("Founded Year", founded.toString())
                        DetailRow("Global HQ Location", hq)

                        Spacer(modifier = Modifier.height(6.dp))
                        Text("ACTIVE DEPARTMENTS", color = TechPurple, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        val depts = listOf("Cyber Defense Systems", "Cognitive Grid Management", "Strategic Infrastructure", "Financial Ledger Integration")
                        depts.forEach { dep ->
                            Text("• $dep", color = TextPrimary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text("CURRENT ACTIVE INITIATIVES", color = CyberCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        val initiative = when (org.industry) {
                            IndustryType.TECHNOLOGY -> "Project Genesis Core (Synthetic Intelligence Architecture)"
                            IndustryType.FINANCE -> "Project Quantum Vault (Distributed Ledger Protocol)"
                            IndustryType.HEALTHCARE -> "Project Aegis Bio (Synthetic Genomics Sequencing)"
                            IndustryType.MILITARY -> "Project Orion Grid (Autonomous Kinetic Control)"
                            else -> "Project Neural Lattice (Wide Mesh Bandwidth Operations)"
                        }
                        Text("• $initiative", color = TextPrimary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    }
                }
                "ACTIVITY" -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text("LIVE NETWORK CHRONICLE LOGS", color = accentColor, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        val incidents by osManager.livingWorldEngine.incidents.collectAsState()
                        val orgIncidents = incidents.filter { it.organizationId == org.id }

                        if (orgIncidents.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(AbyssSurface)
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("NO RECENT INCIDENTS REPORTED", color = StatusConnected, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                                items(orgIncidents.reversed()) { inc ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(AbyssSurface)
                                            .padding(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(inc.type.toString(), color = NeonRed, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                            Text(inc.timestamp, color = TextMuted, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                        }
                                        Text("Severity: ${inc.severity}", color = TextPrimary, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Global helper: Detail Row
@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
        Text(text = value, color = TextPrimary, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
    }
}

// Global helper: Node Detail Inspector
@Composable
private fun NodeDetailInspector(node: NetworkNode?, accentColor: Color) {
    if (node == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "SELECT NODE", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("LOCAL NODE", color = accentColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(4.dp))
        Text(node.hostname, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Text(node.ip, color = CyberCyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace)

        Spacer(modifier = Modifier.height(6.dp))
        DetailRow("MAC Address", node.mac)
        DetailRow("Node Type", node.nodeType.displayName)
        DetailRow("Owner ID", node.ownerId)
        DetailRow("Security Level", "Tier ${node.securityLevel}")
        DetailRow("Latency", "${node.latencyMs} ms")

        Spacer(modifier = Modifier.height(8.dp))
        Text("ACTIVE SERVICES (${node.services.size})", color = TechPurple, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(4.dp))
        node.services.forEach { s ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(AbyssSurface)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${s.name} :${s.port}", color = TextPrimary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Text(if (s.isOpen) "OPEN" else "CLOSED", color = if (s.isOpen) StatusConnected else NeonRed, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
            }
            Spacer(modifier = Modifier.height(2.dp))
        }
    }
}

// Global helper: Corporate Server Inspector
@Composable
private fun CorporateServerInspector(server: CorporateServer?, accentColor: Color) {
    if (server == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "SELECT NODE", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("SERVER NODE", color = accentColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(4.dp))
        Text(server.name, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Text(server.domain, color = CyberCyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace)

        Spacer(modifier = Modifier.height(6.dp))
        DetailRow("IP Address", server.ip)
        DetailRow("MAC Address", server.mac)
        DetailRow("Server Class", server.type.displayName)
        DetailRow("Security Tier", "Tier ${server.securityLevel}")
        DetailRow("Rack ID", server.rackId)

        Spacer(modifier = Modifier.height(8.dp))
        Text("EXPOSED SERVICES (${server.services.size})", color = TechPurple, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(4.dp))
        server.services.forEach { svc ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(AbyssSurface)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${svc.name} :${svc.port}", color = TextPrimary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Text(if (svc.isOpen) "OPEN" else "CLOSED", color = if (svc.isOpen) StatusConnected else NeonRed, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
            }
            Spacer(modifier = Modifier.height(2.dp))
        }
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

// ==========================================
// OPERATIONAL MODES IMPLEMENTATIONS
// ==========================================

@Composable
private fun CorporateGridModeView(
    orgs: List<Organization>,
    sortedOrgs: List<Organization>,
    selectedOrg: Organization?,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedIndustry: IndustryType?,
    onIndustrySelect: (IndustryType?) -> Unit,
    onSelectOrg: (String) -> Unit,
    currentSortMode: String,
    onSortChange: (String) -> Unit,
    selectedServerId: String?,
    onSelectServer: (String) -> Unit,
    zoomLevel: Float,
    onZoomChange: (Float) -> Unit,
    onNavigateToIntelligence: () -> Unit,
    onNavigateToSecurity: () -> Unit,
    accentColor: Color
) {
    var viewType by remember { mutableStateOf("GRID") } // GRID or TREE

    Column(modifier = Modifier.fillMaxSize()) {
        // Search & Filter Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Search field
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(AbyssSurface)
                    .border(0.5.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = TextMuted,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                BasicTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    singleLine = true,
                    textStyle = TextStyle(color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) {
                            Text("Search orgs, level...", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                        innerTextField()
                    }
                )
                if (searchQuery.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = TextMuted,
                        modifier = Modifier
                            .size(14.dp)
                            .clickable { onSearchChange("") }
                    )
                }
            }

            // View toggle button: GRID vs TREE
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(0.5.dp, accentColor, RoundedCornerShape(6.dp))
                    .clickable { viewType = if (viewType == "GRID") "TREE" else "GRID" }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (viewType == "GRID") "SHOW TREE" else "SHOW GRID",
                    color = accentColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Industry Filter chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
        ) {
            item {
                val isSel = selectedIndustry == null
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSel) accentColor.copy(alpha = 0.2f) else AbyssSurface)
                        .border(0.5.dp, if (isSel) accentColor else Color.Transparent, RoundedCornerShape(4.dp))
                        .clickable { onIndustrySelect(null) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("ALL", color = if (isSel) accentColor else TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
            items(IndustryType.entries.toTypedArray()) { ind ->
                val isSel = selectedIndustry == ind
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSel) accentColor.copy(alpha = 0.2f) else AbyssSurface)
                        .border(0.5.dp, if (isSel) accentColor else Color.Transparent, RoundedCornerShape(4.dp))
                        .clickable { onIndustrySelect(if (isSel) null else ind) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(ind.name, color = if (isSel) accentColor else TextMuted, fontSize = 9.sp)
                }
            }
        }

        // Large Visualization Area (takes maximum space)
        Box(modifier = Modifier.weight(1f)) {
            if (viewType == "GRID") {
                OrganizationDirectoryView(
                    orgs = sortedOrgs,
                    selectedOrgId = selectedOrg?.id,
                    onSelectOrg = onSelectOrg,
                    accentColor = accentColor
                )
            } else {
                CorporateGridTreeView(
                    orgs = sortedOrgs,
                    selectedServerId = selectedServerId,
                    onSelectServer = onSelectServer,
                    zoomLevel = zoomLevel,
                    onZoomChange = onZoomChange,
                    accentColor = accentColor
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Selected Corporation Bottom Window ("ОКНО ВЫБРАННОЙ КОРПОРАЦИИ")
        if (selectedOrg != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(containerColor = AbyssCard)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "ОКНО ВЫБРАННОЙ КОРПОРАЦИИ",
                        color = accentColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left side: Corporate Information
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 12.dp)
                        ) {
                            Text(
                                text = selectedOrg.name.uppercase(),
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "DOMAIN: ${selectedOrg.domain}",
                                color = CyberCyan,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${selectedOrg.description}\n" +
                                        "Sector: ${selectedOrg.industry.displayName} • Subnet: ${selectedOrg.subnet} • Employees: ${String.format("%,d", selectedOrg.employeeCount)} • Tier ${selectedOrg.securityLevel}",
                                color = TextMuted,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 4
                            )
                        }

                        // Right side: Stacked action buttons
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            // Button 1: STUDY FULL TREE
                            Box(
                                modifier = Modifier
                                    .width(135.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(StatusConnected.copy(alpha = 0.18f))
                                    .border(1.dp, StatusConnected, RoundedCornerShape(4.dp))
                                    .clickable { onNavigateToIntelligence() }
                                    .padding(vertical = 5.dp, horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "STUDY\nFULL TREE",
                                    color = StatusConnected,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }

                            // Button 2: SECURITY AUDIT
                            Box(
                                modifier = Modifier
                                    .width(135.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(accentColor.copy(alpha = 0.18f))
                                    .border(1.dp, accentColor, RoundedCornerShape(4.dp))
                                    .clickable { onNavigateToSecurity() }
                                    .padding(vertical = 5.dp, horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "SECURITY\nAUDIT",
                                    color = accentColor,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }

                            // Button 3: QUICK NETWORK ACCESS
                            Box(
                                modifier = Modifier
                                    .width(135.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(CyberCyan.copy(alpha = 0.18f))
                                    .border(1.dp, CyberCyan, RoundedCornerShape(4.dp))
                                    .clickable {
                                        onSelectOrg(selectedOrg.id)
                                    }
                                    .padding(vertical = 5.dp, horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "QUICK NETWORK\nACCESS",
                                    color = CyberCyan,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(AbyssCard)
                    .border(0.5.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "SELECT AN ORGANIZATION FROM THE VISUALIZATION AREA TO ACCESS INTEL",
                    color = TextMuted,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun NetworkMapModeView(
    osManager: AbyssOSManager,
    nodes: List<NetworkNode>,
    selectedNodeId: String?,
    onSelectNode: (String) -> Unit,
    selectedOrg: Organization?,
    accentColor: Color
) {
    var mapType by remember { mutableStateOf("LOCAL") } // LOCAL or TARGET

    Column(modifier = Modifier.fillMaxSize()) {
        // Main Map Area
        Box(modifier = Modifier.weight(1f)) {
            if (mapType == "LOCAL") {
                LocalTopologyMapView(
                    osManager = osManager,
                    nodes = nodes,
                    selectedNodeId = selectedNodeId,
                    onSelectNode = onSelectNode,
                    accentColor = accentColor
                )
            } else {
                if (selectedOrg != null) {
                    CustomNetworkTopologyView(
                        org = selectedOrg,
                        accentColor = accentColor
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                            .background(AbyssCard)
                            .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Target Subnet Empty",
                                tint = accentColor,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "NO TARGET CORPORATION SELECTED",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Navigate to CORPORATE GRID and select a target organization first.",
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

@Composable
private fun OrganizationIntelligenceModeView(
    orgs: List<Organization>,
    selectedOrg: Organization?,
    onSelectOrg: (String) -> Unit,
    osManager: AbyssOSManager,
    accentColor: Color
) {
    var dropdownExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Target Selector Row at the top to switch organizations easily
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(AbyssSurface)
                .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "ACTIVE INTELLIGENCE TARGET: ",
                    color = TextMuted,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Box {
                    Text(
                        text = "${selectedOrg?.name?.uppercase() ?: "NONE"} ▼",
                        color = accentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.clickable { dropdownExpanded = true }
                    )

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier
                            .background(AbyssCard)
                            .border(0.5.dp, accentColor, RoundedCornerShape(4.dp))
                    ) {
                        orgs.forEach { org ->
                            DropdownMenuItem(
                                text = {
                                    Text(org.name, color = TextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                },
                                onClick = {
                                    onSelectOrg(org.id)
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Expanded view
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(AbyssCard)
                .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            if (selectedOrg != null) {
                OrganizationProfileView(
                    org = selectedOrg,
                    osManager = osManager,
                    accentColor = accentColor
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("NO ORGANIZATION CHOSEN", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
private fun SecurityAnalysisModeView(
    osManager: AbyssOSManager,
    selectedOrg: Organization?,
    accentColor: Color
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(AbyssCard)
                .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            SecurityTabView(
                osManager = osManager,
                accentColor = accentColor
            )
        }
    }
}

@Composable
private fun ActivityCenterView(
    osManager: AbyssOSManager,
    accentColor: Color
) {
    val incidents by osManager.livingWorldEngine.incidents.collectAsState()
    val news by osManager.economyEngine.newsService.feed.collectAsState()
    val systemLogs by osManager.systemLogs.collectAsState()

    var selectedCategory by remember { mutableStateOf("ALL") } // ALL, INCIDENTS, NEWS, NETWORK, SECURITY

    // Compile events list
    val activityEvents = remember(incidents, news, systemLogs) {
        val list = mutableListOf<ActivityEvent>()
        
        incidents.forEach { inc ->
            list.add(
                ActivityEvent(
                    id = "inc_" + inc.id,
                    title = "INCIDENT - ${inc.type}",
                    message = "Severity: ${inc.severity}. Security breach reported at organization ${inc.organizationId}.",
                    timestamp = inc.timestamp,
                    category = "INCIDENTS",
                    level = NotificationLevel.WARNING
                )
            )
        }

        news.forEach { article ->
            list.add(
                ActivityEvent(
                    id = "news_" + article.id,
                    title = "NEWS - ${article.title}",
                    message = article.content,
                    timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(article.timestamp)),
                    category = "NEWS",
                    level = NotificationLevel.INFO
                )
            )
        }

        systemLogs.forEach { log ->
            val cat = when (log.tag) {
                "KERNEL", "BOOT", "SYSTEM" -> "NETWORK"
                "AUTH" -> "SECURITY"
                else -> "NETWORK"
            }
            list.add(
                ActivityEvent(
                    id = "log_" + log.id,
                    title = "${log.tag} LOG",
                    message = log.message,
                    timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(log.timestamp)),
                    category = cat,
                    level = when (log.level) {
                        com.example.backdoor.core.LogLevel.INFO -> NotificationLevel.INFO
                        com.example.backdoor.core.LogLevel.DEBUG -> NotificationLevel.INFO
                        com.example.backdoor.core.LogLevel.WARN -> NotificationLevel.WARNING
                        com.example.backdoor.core.LogLevel.ERROR -> NotificationLevel.ERROR
                        com.example.backdoor.core.LogLevel.CRITICAL -> NotificationLevel.ERROR
                    }
                )
            )
        }

        list.sortedByDescending { it.id }
    }

    val filteredEvents = remember(activityEvents, selectedCategory) {
        if (selectedCategory == "ALL") activityEvents
        else activityEvents.filter { it.category == selectedCategory }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .background(AbyssCard)
            .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LIVE ACTIVITY CHRONICLE CONSOLE",
                color = accentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(accentColor.copy(alpha = 0.1f))
                        .clickable {
                            osManager.showNotification(
                                title = "CONSOLE",
                                message = "Activity feed updated and refreshed.",
                                level = NotificationLevel.SUCCESS
                            )
                        }
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text("REFRESH", color = accentColor, fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Category Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val cats = listOf("ALL", "INCIDENTS", "NEWS", "NETWORK", "SECURITY")
            cats.forEach { cat ->
                val isSel = selectedCategory == cat
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSel) accentColor.copy(alpha = 0.2f) else AbyssSurface)
                        .border(0.5.dp, if (isSel) accentColor else Color.White.copy(alpha = 0.04f), RoundedCornerShape(4.dp))
                        .clickable { selectedCategory = cat }
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = cat,
                        color = if (isSel) accentColor else TextMuted,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Events List
        if (filteredEvents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Inbox,
                        contentDescription = "Empty",
                        tint = TextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "NO RECENT CHRONICLE ENTRIES DISCOVERED",
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filteredEvents, key = { it.id }) { ev ->
                    val borderCol = when (ev.level) {
                        NotificationLevel.ERROR -> NeonRed
                        NotificationLevel.WARNING -> AmberAlert
                        else -> CyberCyan
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(AbyssSurface)
                            .border(0.5.dp, borderCol.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(borderCol)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = ev.title.uppercase(),
                                    color = TextPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Text(
                                text = ev.timestamp,
                                color = TextMuted,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = ev.message,
                            color = TextMuted,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 12.sp
                        )
                    }
                }
            }
        }
    }
}

private data class ActivityEvent(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: String,
    val category: String,
    val level: NotificationLevel
)
