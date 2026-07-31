package com.example.backdoor.ui.apps.economy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.backdoor.economy.models.CurrencyType
import com.example.backdoor.game.AbyssOSManager
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun WalletApp(
    osManager: AbyssOSManager,
    accentColor: Color = CyberCyan,
    modifier: Modifier = Modifier
) {
    val walletManager = osManager.economyEngine.walletManager
    val balances by walletManager.balances.collectAsState()
    val transactions by walletManager.transactions.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F111A))
            .padding(16.dp)
    ) {
        Text("Digital Banking / Wallet", color = accentColor, fontSize = 20.sp, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(16.dp))
        
        // Balances
        balances.forEach { (currency, amount) ->
            Text(
                text = "${currency.displayName}: $amount ${currency.symbol}",
                color = TextPrimary,
                fontSize = 16.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Transaction History", color = accentColor, fontSize = 16.sp, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(transactions.reversed()) { tx ->
                val sign = if (tx.isIncoming) "+" else "-"
                val color = if (tx.isIncoming) Color.Green else Color.Red
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(
                        text = "$sign${tx.amount} ${tx.currency.symbol}",
                        color = color,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.width(100.dp)
                    )
                    Text(
                        text = tx.description,
                        color = TextSecondary,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = tx.status.name,
                        color = TextSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
