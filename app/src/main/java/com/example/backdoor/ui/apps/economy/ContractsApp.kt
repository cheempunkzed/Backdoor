package com.example.backdoor.ui.apps.economy

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.backdoor.core.NotificationLevel
import com.example.backdoor.economy.models.CompletionValidator
import com.example.backdoor.economy.models.Contract
import com.example.backdoor.economy.models.ContractStatus
import com.example.backdoor.economy.models.ValidationResult
import com.example.backdoor.game.AbyssOSManager
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ContractsApp(
    osManager: AbyssOSManager,
    accentColor: Color = CyberCyan,
    modifier: Modifier = Modifier
) {
    val contractManager = osManager.economyEngine.contractManager
    val contracts by contractManager.contracts.collectAsState()

    val process = osManager.processManager.getProcessForApp(com.example.backdoor.game.OsApp.CONTRACTS)
    val appState = process?.appState as? com.example.backdoor.core.ContractsAppState

    var selectedTab by remember { appState?.selectedTab ?: mutableStateOf("ACCEPTED") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F111A))
            .padding(16.dp)
    ) {
        // Tab Selector Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            listOf("AVAILABLE", "ACCEPTED", "HISTORY").forEach { tab ->
                val count = when (tab) {
                    "AVAILABLE" -> contracts.count { it.status == ContractStatus.AVAILABLE }
                    "ACCEPTED" -> contracts.count { it.status == ContractStatus.ACCEPTED }
                    else -> contracts.count { it.status == ContractStatus.COMPLETED || it.status == ContractStatus.FAILED }
                }
                val isSel = selectedTab == tab
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSel) accentColor.copy(alpha = 0.2f) else Color(0xFF161925))
                        .border(1.dp, if (isSel) accentColor else Color.White.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                        .clickable { selectedTab = tab }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$tab ($count)",
                        color = if (isSel) accentColor else TextSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        val filteredContracts = when (selectedTab) {
            "AVAILABLE" -> contracts.filter { it.status == ContractStatus.AVAILABLE }
            "ACCEPTED" -> contracts.filter { it.status == ContractStatus.ACCEPTED }
            else -> contracts.filter { it.status == ContractStatus.COMPLETED || it.status == ContractStatus.FAILED }
        }

        if (filteredContracts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "NO CONTRACTS IN '$selectedTab' QUEUE",
                    color = TextSecondary.copy(alpha = 0.6f),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredContracts, key = { it.id }) { contract ->
                    ContractCard(
                        contract = contract,
                        accentColor = accentColor,
                        onAccept = {
                            if (contractManager.acceptContract(contract.id)) {
                                osManager.showNotification(
                                    title = "CONTRACT ACCEPTED",
                                    message = "Active mission added: ${contract.title}",
                                    level = NotificationLevel.INFO
                                )
                            }
                        },
                        onComplete = {
                            val validation = CompletionValidator.validate(contract)
                            if (validation is ValidationResult.Success) {
                                if (contractManager.completeContract(contract.id)) {
                                    osManager.economyEngine.walletManager.addFunds(
                                        amount = contract.rewardAmount,
                                        currency = contract.rewardCurrency,
                                        description = "Contract Completion: ${contract.title}"
                                    )
                                    osManager.showNotification(
                                        title = "CONTRACT REWARD CLAIMED",
                                        message = "+${contract.rewardAmount} ${contract.rewardCurrency.symbol} received.",
                                        level = NotificationLevel.SUCCESS
                                    )
                                }
                            } else if (validation is ValidationResult.Failed) {
                                osManager.showNotification(
                                    title = "CANNOT COMPLETE CONTRACT",
                                    message = validation.reason,
                                    level = NotificationLevel.WARNING
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ContractCard(
    contract: Contract,
    accentColor: Color,
    onAccept: () -> Unit,
    onComplete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .border(1.dp, if (contract.status == ContractStatus.ACCEPTED) accentColor else Color.White.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
            .background(Color(0xFF141722))
            .padding(14.dp)
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = contract.title,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            val statusColor = when (contract.status) {
                ContractStatus.AVAILABLE -> CyberCyan
                ContractStatus.ACCEPTED -> Color(0xFFFFB74D)
                ContractStatus.COMPLETED -> Color(0xFF66BB6A)
                ContractStatus.FAILED -> Color(0xFFEF5350)
                ContractStatus.EXPIRED -> Color.Gray
            }
            Text(
                text = "[${contract.status.name}]",
                color = statusColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Client: ${contract.issuer} | Category: ${contract.category} | Diff: ${contract.difficulty}/10",
            color = TextSecondary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = contract.description,
            color = TextPrimary.copy(alpha = 0.85f),
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(10.dp))

        // OBJECTIVE CHECKLIST SECTION
        if (contract.objectives.isNotEmpty()) {
            val completedCount = contract.completedObjectivesCount
            val totalCount = contract.totalObjectivesCount
            val progressPercent = if (totalCount > 0) (completedCount.toFloat() / totalCount) else 1.0f

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF0B0D14))
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "OBJECTIVES PROGRESS",
                        color = accentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "$completedCount/$totalCount Completed (${(progressPercent * 100).toInt()}%)",
                        color = if (completedCount == totalCount) Color(0xFF66BB6A) else TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                contract.objectives.forEach { obj ->
                    val isDone = obj.isDone()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isDone) "✓ " else "✗ ",
                            color = if (isDone) Color(0xFF66BB6A) else Color(0xFFEF5350),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = obj.description,
                            color = if (isDone) TextPrimary.copy(alpha = 0.9f) else TextSecondary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Reward and Actions Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Reward: ${contract.rewardAmount} ${contract.rewardCurrency.symbol}",
                color = Color(0xFF66BB6A),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            when (contract.status) {
                ContractStatus.AVAILABLE -> {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(accentColor.copy(alpha = 0.2f))
                            .border(1.dp, accentColor, RoundedCornerShape(4.dp))
                            .clickable { onAccept() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "ACCEPT CONTRACT",
                            color = accentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                ContractStatus.ACCEPTED -> {
                    val canClaim = contract.isCompleted()
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (canClaim) Color(0xFF66BB6A).copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f))
                            .border(1.dp, if (canClaim) Color(0xFF66BB6A) else Color.White.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .clickable { onComplete() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (canClaim) "CLAIM REWARD" else "OBJECTIVES INCOMPLETE (${contract.completedObjectivesCount}/${contract.totalObjectivesCount})",
                            color = if (canClaim) Color(0xFF66BB6A) else TextSecondary.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                ContractStatus.COMPLETED -> {
                    Text(
                        text = "✓ COMPLETED",
                        color = Color(0xFF66BB6A),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                else -> {
                    Text(
                        text = "[${contract.status.name}]",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
