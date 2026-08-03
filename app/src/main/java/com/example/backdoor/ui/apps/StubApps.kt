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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SignalCellular4Bar
import androidx.compose.material.icons.filled.Wifi
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.backdoor.core.NotificationLevel
import com.example.backdoor.game.AbyssOSManager
import com.example.ui.theme.AbyssBackground
import com.example.ui.theme.AbyssCard
import com.example.ui.theme.AbyssSurface
import com.example.ui.theme.AbyssSurfaceVariant
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.NeonRed
import com.example.ui.theme.PurpleDarknet
import com.example.ui.theme.StatusConnected
import com.example.ui.theme.TechPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun DarkNetApp(
    osManager: AbyssOSManager,
    accentColor: Color = PurpleDarknet,
    modifier: Modifier = Modifier
) {
    val darknetEngine = osManager.darknetEngine
    val relays by darknetEngine.relayNodes.collectAsState()
    val hiddenServices by darknetEngine.hiddenServices.collectAsState()
    val rep by darknetEngine.playerReputation.collectAsState()
    val activeIdent by darknetEngine.activeIdentity.collectAsState()
    val identities by darknetEngine.identities.collectAsState()
    val factions by darknetEngine.factions.collectAsState()
    val factionRep by darknetEngine.factionReputation.collectAsState()
    val messages by darknetEngine.encryptedMessages.collectAsState()
    val rumors by darknetEngine.rumorEngine.rumors.collectAsState()
    val undergroundEvents by darknetEngine.rumorEngine.undergroundEvents.collectAsState()
    val marketListings by darknetEngine.marketListings.collectAsState()
    val isConnected = darknetEngine.isEncryptedOnionRouteActive()

    var selectedTab by remember { mutableStateOf("OVERVIEW") }
    val tabs = listOf("OVERVIEW", "FACTIONS", "MESSAGES", "RUMORS", "MARKET", "SERVICES")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AbyssBackground)
            .padding(12.dp)
    ) {
        // Status Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(AbyssCard)
                .border(1.dp, PurpleDarknet.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "DarkNet Security",
                            tint = PurpleDarknet,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ABYSS DARKNET LAYER v1.4.0",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isConnected) StatusConnected.copy(alpha = 0.2f) else NeonRed.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isConnected) "STATUS: ENCRYPTED TUNNEL" else "STATUS: DISCONNECTED",
                            color = if (isConnected) StatusConnected else NeonRed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val circuitString = if (relays.isNotEmpty()) {
                    relays.take(3).joinToString(" → ") { it.alias }
                } else {
                    "Relay Alpha → Relay Bravo → Exit Delta"
                }

                Text(
                    text = "Circuit: $circuitString",
                    color = TechPurple,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Active Alias: @${activeIdent.nickname} [PGP: ${activeIdent.pgpFingerprint}] | Heat: ${activeIdent.criminalHeat}%",
                    color = CyberCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            tabs.forEach { tabName ->
                val isSelected = selectedTab == tabName
                val unreadCount = if (tabName == "MESSAGES") messages.count { !it.isRead } else 0

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSelected) accentColor.copy(alpha = 0.25f) else AbyssSurface)
                        .border(0.5.dp, if (isSelected) accentColor else Color.Transparent, RoundedCornerShape(4.dp))
                        .clickable { selectedTab = tabName }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = tabName,
                            color = if (isSelected) accentColor else TextMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )
                        if (unreadCount > 0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(NeonRed)
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "$unreadCount",
                                    color = Color.White,
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

        Spacer(modifier = Modifier.height(10.dp))

        // Active Tab View
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                "OVERVIEW" -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        item {
                            Text("DIGITAL IDENTITIES & ALIASES", color = TechPurple, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        items(identities) { id ->
                            val isActive = id.id == activeIdent.id
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AbyssSurface)
                                    .border(1.dp, if (isActive) CyberCyan else Color.Transparent, RoundedCornerShape(6.dp))
                                    .padding(10.dp)
                            ) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("@${id.nickname}${if (isActive) " [ACTIVE]" else ""}", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                        Text("PGP: ${id.pgpFingerprint}", color = CyberCyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                        Text("Profile: ${id.hiddenProfile}", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                    }
                                    if (!isActive) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(PurpleDarknet)
                                                .clickable { darknetEngine.switchIdentity(id.id) }
                                                .padding(horizontal = 8.dp, vertical = 5.dp)
                                        ) {
                                            Text("SWITCH", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                        }
                                    }
                                }
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("RELAY CIRCUIT LATENCY & ENCRYPTION", color = TechPurple, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        items(relays) { r ->
                            Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(AbyssSurface).padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${r.alias} [${r.ip}]", color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                Text("${r.bandwidthMbps} Mbps | ${r.country}", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }

                "FACTIONS" -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        item {
                            Text("UNDERGROUND FACTIONS & REPUTATION", color = TechPurple, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        items(factions) { fac ->
                            val standing = darknetEngine.getFactionStanding(fac.id)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AbyssSurface)
                                    .border(0.5.dp, PurpleDarknet.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("${fac.tag} ${fac.name}", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                        Text("Standing: ${standing.trustTier} (${standing.reputation})", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(fac.ideology, color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Hidden Forums: ${fac.hiddenForums.joinToString(", ")}", color = TechPurple, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }
                }

                "MESSAGES" -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            Text("ENCRYPTED PRIVATE MESSAGES", color = TechPurple, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        items(messages) { msg ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AbyssSurface)
                                    .border(0.5.dp, if (!msg.isRead) NeonRed else Color.Transparent, RoundedCornerShape(6.dp))
                                    .clickable { darknetEngine.markMessageAsRead(msg.id) }
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("From: @${msg.senderHandle}", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                        Text(if (!msg.isRead) "UNREAD" else "READ", color = if (!msg.isRead) NeonRed else StatusConnected, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                    }
                                    Text("Subject: ${msg.subject}", color = CyberCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(msg.body, color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }
                }

                "RUMORS" -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        item {
                            Text("UNDERGROUND EVENTS & LEAK NOTICES", color = NeonRed, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        items(undergroundEvents) { evt ->
                            Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(AbyssSurface).border(0.5.dp, NeonRed.copy(alpha = 0.5f), RoundedCornerShape(6.dp)).padding(10.dp)) {
                                Column {
                                    Text("ALERT: ${evt.headline}", color = NeonRed, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    Text(evt.description, color = TextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("ACTIVE RUMOR MATRIX & CREDIBILITY", color = TechPurple, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        items(rumors) { r ->
                            Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(AbyssSurface).padding(10.dp)) {
                                Column {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(r.title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                        Text("Cred: ${(r.credibility * 100).toInt()}%", color = if (r.credibility > 0.7f) StatusConnected else AmberAlert, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                    }
                                    Text("Source: @${r.sourceHandle} | Target: ${r.targetHostOrCorp ?: "Grid"}", color = CyberCyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(r.description, color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }
                }

                "MARKET" -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            Text("SHADOW EXCHANGE MARKETPLACE", color = TechPurple, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        items(marketListings) { item ->
                            Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(AbyssSurface).padding(10.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                        Text("Category: ${item.category} | Seller: @${item.sellerHandle} (${item.sellerRating}★)", color = CyberCyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                        Text(item.description, color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("${item.priceCredits.toInt()} CR", color = AmberAlert, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (item.available) PurpleDarknet else TextMuted.copy(alpha = 0.3f))
                                                .clickable(enabled = item.available) {
                                                    val (success, msg) = darknetEngine.buyMarketItem(item.id)
                                                    osManager.eventBus.emit(
                                                        com.example.backdoor.core.SystemEvent.NotificationTriggered(
                                                            title = "DARK MARKET",
                                                            message = msg,
                                                            level = if (success) NotificationLevel.SUCCESS else NotificationLevel.WARNING
                                                        )
                                                    )
                                                }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(if (item.available) "BUY ITEM" else "SOLD OUT", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "SERVICES" -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            Text("INDEXED HIDDEN ONION SERVICES", color = TechPurple, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        items(hiddenServices) { hs: com.example.backdoor.darknet.model.HiddenService ->
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
                                            Text(
                                                text = hs.name,
                                                color = TextPrimary,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(PurpleDarknet.copy(alpha = 0.2f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = hs.accessLevel.displayName,
                                                    color = PurpleDarknet,
                                                    fontSize = 8.sp,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        }
                                        Text(
                                            text = hs.address,
                                            color = CyberCyan,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = hs.description,
                                            color = TextMuted,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(PurpleDarknet)
                                            .clickable {
                                                osManager.eventBus.emit(
                                                    com.example.backdoor.core.SystemEvent.AppRequested(
                                                        targetApp = com.example.backdoor.game.OsApp.BROWSER,
                                                        payload = hs.address
                                                    )
                                                )
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "LAUNCH",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
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
