package com.example.backdoor.ui.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
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
 * Resolves local and virtual domain names via AbyssNet DomainResolver.
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
                Icon(
                    imageVector = if (resolvedIp != null) Icons.Default.Lock else Icons.Default.Warning,
                    contentDescription = "Security",
                    tint = if (resolvedIp != null) StatusConnected else NeonRed,
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

        Spacer(modifier = Modifier.height(10.dp))

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
                currentUrl.equals("router.local", ignoreCase = true) || currentUrl.equals("192.168.1.1") -> {
                    RouterWebPage(osManager = osManager, accentColor = accentColor)
                }
                currentUrl.equals("localhost", ignoreCase = true) || currentUrl.equals("127.0.0.1") -> {
                    LocalhostWebPage(osManager = osManager, accentColor = accentColor)
                }
                currentUrl.equals("about:network", ignoreCase = true) -> {
                    AboutNetworkPage(osManager = osManager, accentColor = accentColor)
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
        Text(text = "Kernel Version: 0.5.0 ABYSSNET", color = TextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
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
