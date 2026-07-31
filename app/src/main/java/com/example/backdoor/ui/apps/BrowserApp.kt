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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.backdoor.corporate.Organization
import com.example.backdoor.game.AbyssOSManager
import com.example.ui.theme.AbyssBackground
import com.example.ui.theme.AbyssCard
import com.example.ui.theme.AbyssSurface
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.NeonRed
import com.example.ui.theme.StatusConnected
import com.example.ui.theme.TechPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

/**
 * AbyssOS Virtual Web Browser.
 * Resolves local, virtual, and corporate domain names via AbyssNet DomainResolver and Corporate Grid.
 */
@Composable
fun BrowserApp(
    osManager: AbyssOSManager,
    accentColor: Color = CyberCyan,
    modifier: Modifier = Modifier
) {
    var urlInput by remember { mutableStateOf("router.local") }
    var currentUrl by remember { mutableStateOf("router.local") }

    val domainResolver = osManager.networkEngine.domainResolver
    val resolvedIp = domainResolver.resolveDomain(currentUrl)
    val targetNode = resolvedIp?.let { osManager.networkEngine.repository.getNodeByIp(it) }
    val corporateOrg = osManager.corporateRepository.getOrganizationByDomain(currentUrl)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AbyssBackground)
            .padding(10.dp)
    ) {
        // Address Bar Navigation Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(AbyssSurface)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(AbyssSurface)
                    .clickable { urlInput = currentUrl },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reload",
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // URL Bar
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(AbyssBackground)
                    .border(0.5.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isSecure = resolvedIp != null || corporateOrg != null
                Icon(
                    imageVector = if (isSecure) Icons.Default.Lock else Icons.Default.Warning,
                    contentDescription = "Security",
                    tint = if (isSecure) StatusConnected else NeonRed,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                BasicTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(onGo = { currentUrl = urlInput.trim() }),
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.2f))
                        .clickable { currentUrl = urlInput.trim() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Go",
                        tint = accentColor,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Quick Bookmarks Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("BOOKMARKS:", color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            val bookmarks = listOf("dir.onion", "abyss-forum.onion", "blackvault.onion", "darkmarket.onion", "wiki.abyss", "router.local")
            bookmarks.forEach { bmark ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (bmark.endsWith(".onion")) TechPurple.copy(alpha = 0.2f) else AbyssSurface)
                        .border(0.5.dp, if (bmark.endsWith(".onion")) TechPurple.copy(alpha = 0.5f) else Color.Transparent, RoundedCornerShape(4.dp))
                        .clickable {
                            currentUrl = bmark
                            urlInput = bmark
                        }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(bmark, color = if (bmark.endsWith(".onion")) TechPurple else TextPrimary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Page Render Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(AbyssCard)
                .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            when {
                currentUrl.endsWith(".onion", ignoreCase = true) || currentUrl.equals("darknet", ignoreCase = true) -> {
                    OnionWebPage(
                        url = currentUrl,
                        osManager = osManager,
                        accentColor = accentColor,
                        onNavigate = { newUrl ->
                            currentUrl = newUrl
                            urlInput = newUrl
                        }
                    )
                }
                currentUrl.equals("router.local", ignoreCase = true) || currentUrl.equals("192.168.1.1") -> {
                    RouterWebPage(osManager = osManager, accentColor = accentColor)
                }
                currentUrl.equals("localhost", ignoreCase = true) || currentUrl.equals("127.0.0.1") -> {
                    LocalhostWebPage(osManager = osManager, accentColor = accentColor)
                }
                currentUrl.equals("about:network", ignoreCase = true) -> {
                    AboutNetworkPage(osManager = osManager, accentColor = accentColor)
                }
                currentUrl.contains("wiki", ignoreCase = true) || currentUrl.contains("docs", ignoreCase = true) -> {
                    WikiWebPage(osManager = osManager, accentColor = accentColor)
                }
                corporateOrg != null -> {
                    CorporateWebPage(org = corporateOrg, accentColor = accentColor)
                }
                resolvedIp != null && targetNode != null -> {
                    NodeWebPage(node = targetNode, accentColor = accentColor)
                }
                else -> {
                    ErrorWebPage(url = currentUrl)
                }
            }
        }
    }
}

@Composable
private fun CorporateWebPage(org: Organization, accentColor: Color) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Corporate Brand Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(AbyssSurface)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Business,
                contentDescription = org.name,
                tint = accentColor,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = org.name.uppercase(),
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "${org.industry.displayName} • Code: ${org.code} • Subnet ${org.subnet}",
                    color = CyberCyan,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Mission & Infrastructure Overview Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(AbyssSurface)
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = "CORPORATE MISSION STATEMENT",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = org.description,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Grid Specs Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(AbyssSurface)
                    .padding(10.dp)
            ) {
                Column {
                    Text("SECURITY CLEARANCE", color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Text("Tier ${org.securityLevel} Encrypted", color = StatusConnected, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(AbyssSurface)
                    .padding(10.dp)
            ) {
                Column {
                    Text("ACTIVE DATA CENTERS", color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Text("${org.dataCenters.size} Grid Facilities", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(AbyssSurface)
                    .padding(10.dp)
            ) {
                Column {
                    Text("MANAGED NODES", color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Text("${org.servers.size} Active Servers", color = TechPurple, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "PUBLIC CORPORATE SERVER NODES",
            color = TechPurple,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(6.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(org.servers.take(8)) { server ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(AbyssSurface)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = server.domain, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Text(text = "IP: ${server.ip} • Class: ${server.type.displayName}", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                    Text(text = "ONLINE", color = StatusConnected, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
private fun RouterWebPage(osManager: AbyssOSManager, accentColor: Color) {
    val nodes by osManager.networkEngine.nodes.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(AbyssSurface)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.Router, contentDescription = "Router", tint = accentColor, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = "ABYSSROUTER OS GATEWAY v2.1", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Text(text = "WAN IP: 185.220.101.5 | Subnet Gateway 192.168.1.1", color = StatusConnected, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(text = "CONNECTED DHCP DEVICES (${nodes.size})", color = TechPurple, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(nodes) { node ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(AbyssSurface)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = node.hostname, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Text(text = "MAC: ${node.mac}", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                    Text(text = node.ip, color = CyberCyan, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
private fun LocalhostWebPage(osManager: AbyssOSManager, accentColor: Color) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = "ABYSSOS LOCALHOST DASHBOARD", color = CyberCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Text(text = "Running on 127.0.0.1:8080", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Local System Status: ACTIVE", color = StatusConnected, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
        Text(text = "Virtual File System: MOUNTED (/)", color = TextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
        Text(text = "Kernel Version: 0.7.0 OFFENSIVE SECURITY FRAMEWORK", color = TextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun AboutNetworkPage(osManager: AbyssOSManager, accentColor: Color) {
    val records = osManager.networkEngine.domainResolver.getAllRecords()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = "ABYSSNET DNS DIRECTORY", color = accentColor, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(records.toList()) { (domain, ip) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(AbyssSurface)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = domain, color = TextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    Text(text = ip, color = CyberCyan, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
private fun NodeWebPage(node: com.example.backdoor.network.models.NetworkNode, accentColor: Color) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = node.hostname.uppercase(), color = CyberCyan, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Text(text = "IP Address: ${node.ip} | Type: ${node.nodeType.displayName}", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Welcome to ${node.hostname} HTTP portal.", color = TextPrimary, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Security Clearance: Level ${node.securityLevel}", color = StatusConnected, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun ErrorWebPage(url: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(NeonRed.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Default.WifiOff, contentDescription = "Error", tint = NeonRed, modifier = Modifier.size(32.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "DNS Resolution Failed", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = "Host Unreachable: $url", color = NeonRed, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Verify that the domain name or IP address is registered on AbyssNet.", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace, textAlign = TextAlign.Center)
    }
}

@Composable
private fun WikiWebPage(osManager: AbyssOSManager, accentColor: Color) {
    val kb = osManager.securityFramework.knowledgeDatabase
    val articles = kb.getAllArticles()
    var selectedArticle by remember { mutableStateOf(articles.firstOrNull()) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = "=== ABYSSOS TECHNICAL WIKI & DOCUMENTATION PORTAL ===", color = CyberCyan, fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Text(text = "Official System Architecture & Security Manuals", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxSize()) {
            // Article List
            LazyColumn(
                modifier = Modifier
                    .width(220.dp)
                    .fillMaxHeight()
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(articles) { art ->
                    val isSel = art.id == selectedArticle?.id
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSel) accentColor.copy(alpha = 0.2f) else AbyssSurface)
                            .border(0.5.dp, if (isSel) accentColor else Color.Transparent, RoundedCornerShape(6.dp))
                            .clickable { selectedArticle = art }
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(art.title, color = if (isSel) accentColor else TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text(art.category, color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            // Article Content Reader
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(AbyssSurface)
                    .padding(12.dp)
            ) {
                val art = selectedArticle
                if (art != null) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            Text(art.title, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text("Category: ${art.category} | Tags: ${art.tags.joinToString(", ")}", color = CyberCyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(art.content, color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OnionWebPage(
    url: String,
    osManager: AbyssOSManager,
    accentColor: Color,
    onNavigate: (String) -> Unit
) {
    val darknet = osManager.darknetEngine
    val hiddenServices by darknet.hiddenServices.collectAsState()
    val threads by darknet.forumThreads.collectAsState()
    val marketListings by darknet.marketListings.collectAsState()
    val rep by darknet.playerReputation.collectAsState()
    val relays by darknet.relayNodes.collectAsState()

    var selectedThreadId by remember { mutableStateOf(threads.firstOrNull()?.id) }
    var replyInput by remember { mutableStateOf("") }
    var newThreadTitle by remember { mutableStateOf("") }
    var newThreadCategory by remember { mutableStateOf("General") }
    var newThreadContent by remember { mutableStateOf("") }
    var showNewThreadDialog by remember { mutableStateOf(false) }

    val activeService = hiddenServices.find { it.address.equals(url, ignoreCase = true) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Onion Circuit Header Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(TechPurple.copy(alpha = 0.15f))
                .border(0.5.dp, TechPurple.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Onion Circuit",
                        tint = TechPurple,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ABYSSNET ONION ROUTED SESSION | HOPS: ${relays.take(3).joinToString(" → ") { it.alias }}",
                        color = TechPurple,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    text = "REPUTATION: ${rep.rank.title} (Trust: ${rep.trust} | Fame: ${rep.fame})",
                    color = StatusConnected,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Content Display based on URL
        when {
            url.equals("dir.onion", ignoreCase = true) || url.equals("darknet", ignoreCase = true) -> {
                Text("=== HIDDEN SERVICES DIRECTORY (DIR.ONION) ===", color = TechPurple, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Text("Indexed Hidden Nodes on AbyssNet Onion Network", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(hiddenServices) { hs ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(AbyssSurface)
                                .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(hs.name, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(TechPurple.copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(hs.accessLevel.displayName, color = TechPurple, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                        }
                                    }
                                    Text(hs.address, color = CyberCyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(hs.description, color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(TechPurple)
                                        .clickable { onNavigate(hs.address) }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text("CONNECT", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }
                }
            }

            url.equals("abyss-forum.onion", ignoreCase = true) -> {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Threads Sidebar
                    Column(
                        modifier = Modifier
                            .width(260.dp)
                            .fillMaxHeight()
                            .padding(end = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ABYSS FORUM THREADS", color = TechPurple, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(TechPurple.copy(alpha = 0.2f))
                                    .clickable { showNewThreadDialog = !showNewThreadDialog }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text("+ NEW THREAD", color = TechPurple, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(threads) { th ->
                                val isSel = th.id == selectedThreadId
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSel) TechPurple.copy(alpha = 0.2f) else AbyssSurface)
                                        .border(0.5.dp, if (isSel) TechPurple else Color.Transparent, RoundedCornerShape(6.dp))
                                        .clickable { selectedThreadId = th.id }
                                        .padding(8.dp)
                                ) {
                                    Column {
                                        Text(th.title, color = if (isSel) TechPurple else TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("By ${th.authorHandle}", color = CyberCyan, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                            Text("${th.posts.size} posts", color = TextMuted, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Thread Posts & Reply Reader
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(6.dp))
                            .background(AbyssSurface)
                            .padding(10.dp)
                    ) {
                        val activeThread = threads.find { it.id == selectedThreadId }
                        if (activeThread != null) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Text(activeThread.title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                Text("Category: ${activeThread.category} | Started by @${activeThread.authorHandle}", color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)

                                Spacer(modifier = Modifier.height(8.dp))

                                LazyColumn(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(activeThread.posts) { p ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(AbyssBackground)
                                                .border(0.5.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                                                .padding(8.dp)
                                        ) {
                                            Column {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text("@${p.authorHandle}", color = CyberCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                                    Text("▲ ${p.upvotes}", color = StatusConnected, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(p.content, color = TextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Reply Box
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(AbyssBackground)
                                        .border(0.5.dp, TechPurple.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                        .padding(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    BasicTextField(
                                        value = replyInput,
                                        onValueChange = { replyInput = it },
                                        singleLine = true,
                                        textStyle = TextStyle(color = TextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(TechPurple)
                                            .clickable {
                                                if (replyInput.isNotBlank()) {
                                                    darknet.postReplyToThread(activeThread.id, replyInput, "operator")
                                                    replyInput = ""
                                                }
                                            }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("REPLY", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            url.equals("darkmarket.onion", ignoreCase = true) -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text("=== SHADOW EXCHANGE MARKET FOUNDATION ===", color = TechPurple, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Text("Decentralized Directory of Tools & Hardware Schematics", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(marketListings) { item ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AbyssSurface)
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                        Text("Seller: @${item.sellerHandle} (★ ${item.sellerRating}) | Category: ${item.category}", color = CyberCyan, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(item.description, color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("${item.priceCredits} CREDITS", color = StatusConnected, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(TechPurple.copy(alpha = 0.2f))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text("LISTING VERIFIED", color = TechPurple, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            else -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text("=== ${activeService?.name ?: url.uppercase()} ===", color = TechPurple, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Text("Service Address: $url", color = CyberCyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(AbyssSurface)
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("Service Type: ${activeService?.type?.displayName ?: "Encrypted Node"}", color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Text("Access Clearance: ${activeService?.accessLevel?.displayName ?: "Public Access"}", color = StatusConnected, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text("Owner: @${activeService?.ownerHandle ?: "anonymous"}", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(activeService?.description ?: "Encrypted darknet node active on AbyssNet Onion Network.", color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    }
}

