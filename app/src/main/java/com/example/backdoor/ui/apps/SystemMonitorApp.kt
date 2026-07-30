package com.example.backdoor.ui.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.backdoor.core.SystemStatus
import com.example.backdoor.game.AbyssOSManager
import com.example.ui.theme.AbyssBackground
import com.example.ui.theme.AbyssCard
import com.example.ui.theme.AbyssSurface
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

data class SimulatedProcess(
    val pid: Int,
    val name: String,
    val user: String,
    val cpuPercent: Int,
    val status: String
)

@Composable
fun SystemMonitorApp(
    osManager: AbyssOSManager,
    accentColor: Color = TerminalGreen,
    modifier: Modifier = Modifier
) {
    val status by osManager.systemStatus.collectAsState()

    val processes = listOf(
        SimulatedProcess(101, "kerneld", "root", 2, "RUNNING"),
        SimulatedProcess(102, "vfs_mount", "root", 1, "SLEEPING"),
        SimulatedProcess(105, "net_stack", "root", 3, "RUNNING"),
        SimulatedProcess(110, "terminald", status.userHandle, status.cpuUsagePercent / 2, "ACTIVE"),
        SimulatedProcess(114, "sec_monitor", "root", 1, "RUNNING"),
        SimulatedProcess(120, "crypto_engine", "root", 2, "STANDBY"),
        SimulatedProcess(128, "abyss_gui", status.userHandle, 4, "RUNNING")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AbyssBackground)
            .padding(10.dp)
    ) {
        Text(
            text = "=== SYSTEM MONITOR | AbyssOS 0.2.0 ===",
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

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "ACTIVE PROCESS TABLE (${processes.size} DAEMONS)",
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
                Text("PID", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.width(40.dp))
                Text("NAME", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                Text("USER", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.width(60.dp))
                Text("CPU%", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.width(45.dp))
                Text("STATE", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.width(60.dp))
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(processes) { proc ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${proc.pid}", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.width(40.dp))
                        Text(proc.name, color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                        Text(proc.user, color = CyberCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.width(60.dp))
                        Text("${proc.cpuPercent}%", color = accentColor, fontSize = 11.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.width(45.dp))
                        Text(proc.status, color = TextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.width(60.dp))
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
