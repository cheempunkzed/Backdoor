package com.example.backdoor.ui.apps.economy

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.backdoor.economy.models.Contract
import com.example.backdoor.economy.models.ContractStatus
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
    
    var selectedTab by remember { appState?.selectedTab ?: mutableStateOf("AVAILABLE") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F111A))
            .padding(16.dp)
    ) {
        Row {
            listOf("AVAILABLE", "ACCEPTED", "HISTORY").forEach { tab ->
                Text(
                    text = tab,
                    color = if (selectedTab == tab) accentColor else TextSecondary,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .clickable { selectedTab = tab }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        val filteredContracts = when (selectedTab) {
            "AVAILABLE" -> contracts.filter { it.status == ContractStatus.AVAILABLE }
            "ACCEPTED" -> contracts.filter { it.status == ContractStatus.ACCEPTED }
            else -> contracts.filter { it.status == ContractStatus.COMPLETED || it.status == ContractStatus.FAILED }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(filteredContracts) { contract ->
                ContractCard(contract, accentColor, onAccept = {
                    contractManager.acceptContract(contract.id)
                }, onComplete = {
                    if (contractManager.completeContract(contract.id)) {
                        osManager.economyEngine.walletManager.addFunds(
                            amount = contract.rewardAmount,
                            currency = contract.rewardCurrency,
                            description = "Contract Completion: ${contract.title}"
                        )
                        osManager.showNotification("CONTRACT", "Payment received.", com.example.backdoor.core.NotificationLevel.SUCCESS)
                    }
                })
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ContractCard(contract: Contract, accentColor: Color, onAccept: () -> Unit, onComplete: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, accentColor.copy(alpha = 0.5f))
            .background(Color(0xFF161925))
            .padding(12.dp)
    ) {
        Text(text = contract.title, color = TextPrimary, fontSize = 16.sp, fontFamily = FontFamily.Monospace)
        Text(text = "Issuer: ${contract.issuer} | Diff: ${contract.difficulty}/10", color = TextSecondary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = contract.description, color = TextSecondary, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Reward: ${contract.rewardAmount} ${contract.rewardCurrency.symbol}", color = Color.Green, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
        
        if (contract.status == ContractStatus.AVAILABLE) {
            Text(
                text = "[ ACCEPT CONTRACT ]",
                color = accentColor,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clickable { onAccept() },
                fontFamily = FontFamily.Monospace
            )
        } else if (contract.status == ContractStatus.ACCEPTED) {
            Text(
                text = "[ COMPLETE CONTRACT ]",
                color = Color.Yellow,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clickable { onComplete() },
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
