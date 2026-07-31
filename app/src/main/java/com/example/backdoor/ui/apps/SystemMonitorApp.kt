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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun SystemMonitorApp(
    osManager: AbyssOSManager,
    accentColor: Color = TerminalGreen,
    modifier: Modifier = Modifier
) {
    val status by osManager.systemStatus.collectAsState()
    val processes by osManager.processManager.processes.collectAsState()
    val orgs by osManager.corporateRepository.organizations.collectAsState()
    val totalServers by osManager.corporateRepository.totalServersCount.collectAsState()
    val totalDcs by osManager.corporateRepository.totalDataCentersCount.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AbyssBackground)
            .padding(10.dp)
    ) {
        Text(
            text = "=== SYSTEM MONITOR | AbyssOS 0.7.0 ===",
            color = accentColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Top Row: Meter Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MeterCard(
                title = "CPU LOAD",
                percent = status.cpuUsagePercent,
                valueText = "${status.cpuUsagePercent}%",
                color = accentColor,
                modifier = Modifier.weight(1f)
            )

            MeterCard(
                title = "RAM USAGE",
                percent = status.ramUsagePercent,
                valueText = "${status.usedRamMb} / ${status.totalRamMb} MB",
                color = CyberCyan,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Corporate Network Infrastructure Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(AbyssCard)
                .border(0.5.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                .padding(10.dp)
        ) {
            Column {
                Text(
                    text = "NETWORK INFRASTRUCTURE MONITOR (ABYSSNET CORPORATE GRID)",
                    color = CyberCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Active Orgs: ${orgs.size}", color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        Text("Data Centers: $totalDcs", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                    Column {
                        Text("Corporate Nodes: $totalServers", color = StatusConnected, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Text("Monitored Subnets: 105", color = TechPurple, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Grid Status: NOMINAL", color = StatusConnected, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Text("Virtual Link: 10 Gbps", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Security Framework Telemetry Card
        val secTasks by osManager.securityFramework.activeTasks.collectAsState()
        val secReports by osManager.securityFramework.completedReports.collectAsState()
        val runningCount = secTasks.count { it.status == com.example.backdoor.security.framework.TaskStatus.RUNNING }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(AbyssCard)
                .border(0.5.dp, TechPurple.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                .padding(10.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "OFFENSIVE SECURITY TELEMETRY ENGINE (v0.7.0)",
                        color = TechPurple,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = if (runningCount > 0) "ANALYSIS IN PROGRESS" else "SECURITY ENGINE IDLE",
                        color = if (runningCount > 0) StatusConnected else TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Active Tasks: ${secTasks.size}", color = TextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        Text("Running: $runningCount", color = CyberCyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                    Column {
                        Text("Audit Reports: ${secReports.size}", color = StatusConnected, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Text("Modules Loaded: 4", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Security Posture: AUDITED", color = StatusConnected, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Text("Saved VFS Reports: /home/operator/reports/", color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "ACTIVE PROCESS KERNEL (${processes.size} PROCESSES)",
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Process Table
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(6.dp))
                .background(AbyssSurface)
                .padding(8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("PID", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.width(36.dp))
                Text("NAME", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                Text("STATUS", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.width(64.dp))
                Text("CPU%", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.width(44.dp))
                Text("RAM", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.width(52.dp))
                Text("KILL", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.width(36.dp))
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(processes, key = { it.pid }) { proc ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${proc.pid}", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.width(36.dp))
                        Text(
                            text = proc.name + if (proc.isDaemon) " [sys]" else "",
                            color = if (proc.isDaemon) CyberCyan else TextPrimary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.weight(1f)
                        )
                        Text(proc.status.name, color = accentColor, fontSize = 10.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.width(64.dp))
                        Text("${proc.cpuUsagePercent}%", color = accentColor, fontSize = 11.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.width(44.dp))
                        Text("${proc.ramUsageMb}M", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.width(52.dp))

                        Box(modifier = Modifier.width(36.dp)) {
                            if (!proc.isDaemon) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(NeonRed.copy(alpha = 0.2f))
                                        .border(0.5.dp, NeonRed, RoundedCornerShape(4.dp))
                                        .clickable {
                                            osManager.processManager.killProcess(proc.pid)
                                            proc.app?.let { app -> osManager.windowManager.closeWindow(app) }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Kill", tint = NeonRed, modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MeterCard(
    title: String,
    percent: Int,
    valueText: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(AbyssCard)
            .border(0.5.dp, color.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(title, color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Text(valueText, color = color, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { (percent / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = color,
                trackColor = AbyssSurface
            )
        }
    }
}
