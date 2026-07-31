package com.example.backdoor.ui.apps

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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

/**
 * AbyssNet Graphical Control Panel, Corporate Grid & Topology Visualizer.
 * Displays local network nodes, corporate organizations, data centers, server racks, and interface details.
 */
@Composable
fun NetworkApp(
    osManager: AbyssOSManager,
    accentColor: Color = AmberAlert,
    modifier: Modifier = Modifier
) {
    val nodes by osManager.networkEngine.nodes.collectAsState()
    val orgs by osManager.corporateRepository.organizations.collectAsState()

    var activeTab by remember { mutableIntStateOf(0) } // 0: Local Map, 1: Corporate Grid, 2: Organizations, 3: Interfaces

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AbyssBackground)
            .padding(10.dp)
    ) {
        // Top Action & Navigation Bar
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
                    imageVector = Icons.Default.GridOn,
                    contentDescription = "Corporate Grid",
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CORPORATE GRID 0.6.0",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Tab Buttons
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(AbyssBackground)
                        .padding(2.dp)
                ) {
                    val tabs = listOf("LOCAL MAP", "GRID TREE", "ORGANIZATIONS", "INTERFACES")
                    tabs.forEachIndexed { index, label ->
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

                // Scan Subnet Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(accentColor.copy(alpha = 0.2f))
                        .border(0.5.dp, accentColor, RoundedCornerShape(6.dp))
                        .clickable {
                            val scanned = osManager.networkEngine.scanNetwork()
                            osManager.showNotification(
                                title = "GRID SCAN COMPLETE",
                                message = "Monitored ${scanned.size} nodes across ${orgs.size} organizations.",
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
                            text = "SCAN GRID",
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

        // Main Content View
        when (activeTab) {
            0 -> LocalTopologyMapView(nodes = nodes, accentColor = accentColor)
            1 -> CorporateGridTreeView(orgs = orgs, accentColor = accentColor)
            2 -> OrganizationsDirectoryView(osManager = osManager, orgs = orgs, accentColor = accentColor)
            3 -> NetworkInterfacesView(osManager = osManager, accentColor = accentColor)
        }
    }
}

@Composable
private fun LocalTopologyMapView(
    nodes: List<NetworkNode>,
    accentColor: Color
) {
    var selectedNode by remember { mutableStateOf<NetworkNode?>(nodes.firstOrNull()) }

    Row(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(8.dp))
                .background(AbyssCard)
                .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = "LOCAL SUBNET TOPOLOGY (192.168.1.0/24)",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                .clickable { selectedNode = node }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(statusColor.copy(alpha = 0.15f))
                                        .border(1.dp, statusColor.copy(alpha = 0.4f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = node.hostname,
                                        tint = statusColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = node.hostname,
                                        color = TextPrimary,
                                        fontSize = 12.sp,
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
private fun CorporateGridTreeView(
    orgs: List<Organization>,
    accentColor: Color
) {
    var expandedOrgId by remember { mutableStateOf<String?>(orgs.firstOrNull()?.id) }
    var expandedDcId by remember { mutableStateOf<String?>(null) }
    var selectedServer by remember { mutableStateOf<CorporateServer?>(null) }

    Row(modifier = Modifier.fillMaxSize()) {
        // Tree Hierarchy
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(8.dp))
                .background(AbyssCard)
                .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = "GLOBAL CORPORATE INFRASTRUCTURE HIERARCHY (${orgs.size} ORGANIZATIONS)",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(orgs) { org ->
                        val isOrgExpanded = expandedOrgId == org.id

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(AbyssSurface)
                                .border(0.5.dp, if (isOrgExpanded) accentColor else Color.White.copy(alpha = 0.06f), RoundedCornerShape(6.dp))
                        ) {
                            // Organization Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expandedOrgId = if (isOrgExpanded) null else org.id }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Business,
                                        contentDescription = "Org",
                                        tint = accentColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "${org.name} [${org.code}]",
                                            color = TextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = "${org.industry.displayName} • Subnet: ${org.subnet} • ${org.dataCenters.size} DCs, ${org.servers.size} Servers",
                                            color = TextMuted,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = if (isOrgExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = "Toggle",
                                    tint = TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Data Centers list under expanded Org
                            AnimatedVisibility(visible = isOrgExpanded) {
                                Column(modifier = Modifier.padding(start = 16.dp, end = 8.dp, bottom = 8.dp)) {
                                    org.dataCenters.forEach { dc ->
                                        val isDcExpanded = expandedDcId == dc.id
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 2.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(AbyssBackground)
                                                .padding(6.dp)
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
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "${dc.name} (${dc.location})",
                                                        color = CyberCyan,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontFamily = FontFamily.Monospace
                                                    )
                                                }
                                                Text(
                                                    text = "${dc.serversCount} Nodes",
                                                    color = TextMuted,
                                                    fontSize = 10.sp,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }

                                            // Servers under Data Center
                                            AnimatedVisibility(visible = isDcExpanded) {
                                                Column(modifier = Modifier.padding(start = 12.dp, top = 4.dp)) {
                                                    val dcServers = org.servers.filter { it.dataCenterId == dc.id }
                                                    dcServers.forEach { server ->
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(vertical = 2.dp)
                                                                .clip(RoundedCornerShape(4.dp))
                                                                .background(if (selectedServer?.id == server.id) accentColor.copy(alpha = 0.2f) else AbyssSurfaceVariant)
                                                                .clickable { selectedServer = server }
                                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            Text(
                                                                text = "${server.name} (${server.ip})",
                                                                color = TextPrimary,
                                                                fontSize = 10.sp,
                                                                fontFamily = FontFamily.Monospace
                                                            )
                                                            Text(
                                                                text = server.type.displayName,
                                                                color = StatusConnected,
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
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Server Detail Side Pane
        Box(
            modifier = Modifier
                .width(260.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(8.dp))
                .background(AbyssCard)
                .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            CorporateServerInspector(server = selectedServer, accentColor = accentColor)
        }
    }
}

@Composable
private fun OrganizationsDirectoryView(
    osManager: AbyssOSManager,
    orgs: List<Organization>,
    accentColor: Color
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedIndustry by remember { mutableStateOf<IndustryType?>(null) }
    var selectedOrg by remember { mutableStateOf<Organization?>(orgs.firstOrNull()) }

    val filteredOrgs = remember(searchQuery, selectedIndustry, orgs) {
        osManager.corporateRepository.filterOrganizations(
            industry = selectedIndustry,
            query = searchQuery
        )
    }

    Row(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            // Search & Industry Filter Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(AbyssSurface)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = TextMuted, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    singleLine = true,
                    textStyle = TextStyle(color = TextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Industry Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (selectedIndustry == null) accentColor else AbyssSurface)
                            .clickable { selectedIndustry = null }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("ALL INDUSTRIES", color = if (selectedIndustry == null) AbyssBackground else TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                items(IndustryType.entries.toTypedArray()) { ind ->
                    val isSel = selectedIndustry == ind
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSel) accentColor else AbyssSurface)
                            .clickable { selectedIndustry = if (isSel) null else ind }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(ind.displayName, color = if (isSel) AbyssBackground else TextMuted, fontSize = 10.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Organizations List Table
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filteredOrgs) { org ->
                    val isSel = selectedOrg?.id == org.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSel) accentColor.copy(alpha = 0.15f) else AbyssCard)
                            .border(0.5.dp, if (isSel) accentColor else Color.White.copy(alpha = 0.06f), RoundedCornerShape(6.dp))
                            .clickable { selectedOrg = org }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = org.name, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text(text = "${org.industry.displayName} • Domain: ${org.domain}", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Security Tier ${org.securityLevel}", color = CyberCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text(text = "${org.servers.size} Servers", color = StatusConnected, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Organization Detail Inspector Panel
        Box(
            modifier = Modifier
                .width(270.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(8.dp))
                .background(AbyssCard)
                .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            OrganizationDetailInspector(org = selectedOrg, accentColor = accentColor)
        }
    }
}

@Composable
private fun CorporateServerInspector(server: CorporateServer?, accentColor: Color) {
    if (server == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Select a corporate server from the tree to inspect.", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("SERVER NODE INSPECTOR", color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(8.dp))

        Text(server.name, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Text(server.domain, color = CyberCyan, fontSize = 12.sp, fontFamily = FontFamily.Monospace)

        Spacer(modifier = Modifier.height(10.dp))

        DetailRow("IP Address", server.ip)
        DetailRow("MAC Address", server.mac)
        DetailRow("Server Class", server.type.displayName)
        DetailRow("Security Clearance", "Level ${server.securityLevel}")
        DetailRow("Rack Assignment", server.rackId)
        DetailRow("Data Center ID", server.dataCenterId)

        Spacer(modifier = Modifier.height(12.dp))

        Text("EXPOSED SERVICES (${server.services.size})", color = TechPurple, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(4.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(server.services) { svc ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(AbyssSurface)
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${svc.name} :${svc.port}", color = TextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text(if (svc.isOpen) "OPEN" else "CLOSED", color = if (svc.isOpen) StatusConnected else NeonRed, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
private fun OrganizationDetailInspector(org: Organization?, accentColor: Color) {
    if (org == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Select an organization to view detailed corporate specs.", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("ORGANIZATION PROFILE", color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(8.dp))

        Text(org.name, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Text("Code: ${org.code} • ${org.domain}", color = CyberCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace)

        Spacer(modifier = Modifier.height(10.dp))

        Text(org.description, color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)

        Spacer(modifier = Modifier.height(12.dp))

        DetailRow("Industry Sector", org.industry.displayName)
        DetailRow("Security Level", "Tier ${org.securityLevel} / 5")
        DetailRow("Reputation Rating", "${org.reputation} / 100")
        DetailRow("Annual Budget", "$${String.format("%,d", org.budget)}")
        DetailRow("Employees", String.format("%,d", org.employeeCount))
        DetailRow("Subnet Range", org.subnet)
        DetailRow("Topology Scheme", org.topologyType.displayName)
        DetailRow("Data Centers", "${org.dataCenters.size} DC facilities")
        DetailRow("Active Servers", "${org.servers.size} Nodes")
    }
}

@Composable
private fun NetworkInterfacesView(osManager: AbyssOSManager, accentColor: Color) {
    val ifconfig = osManager.networkEngine.getIfconfig()
    val netstat = osManager.networkEngine.getNetstat()

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(AbyssCard)
                .padding(12.dp)
        ) {
            Column {
                Text("NETWORK INTERFACES CONFIGURATION", color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn {
                    items(ifconfig) { line ->
                        Text(line, color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(AbyssCard)
                .padding(12.dp)
        ) {
            Column {
                Text("ACTIVE SOCKET CONNECTIONS (NETSTAT)", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn {
                    items(netstat) { line ->
                        Text(line, color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

@Composable
private fun NodeDetailInspector(node: NetworkNode?, accentColor: Color) {
    if (node == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Select a node to inspect.", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("LOCAL NODE INSPECTOR", color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(10.dp))

        Text(node.hostname, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Text(node.ip, color = CyberCyan, fontSize = 12.sp, fontFamily = FontFamily.Monospace)

        Spacer(modifier = Modifier.height(12.dp))

        DetailRow("MAC Address", node.mac)
        DetailRow("Node Type", node.nodeType.displayName)
        DetailRow("Owner ID", node.ownerId)
        DetailRow("Security Level", "Level ${node.securityLevel}")
        DetailRow("Latency", "${node.latencyMs} ms")

        Spacer(modifier = Modifier.height(14.dp))

        Text("ACTIVE SERVICES (${node.services.size})", color = TechPurple, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
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
                    Text("${s.name} :${s.port}", color = TextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text(if (s.isOpen) "OPEN" else "CLOSED", color = if (s.isOpen) StatusConnected else NeonRed, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
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
