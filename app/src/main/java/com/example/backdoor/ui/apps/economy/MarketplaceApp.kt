package com.example.backdoor.ui.apps.economy

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.backdoor.economy.models.MarketListing
import com.example.backdoor.game.AbyssOSManager
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun MarketplaceApp(
    osManager: AbyssOSManager,
    accentColor: Color = CyberCyan,
    modifier: Modifier = Modifier
) {
    val marketManager = osManager.economyEngine.marketManager
    val listings by marketManager.listings.collectAsState()
    
    val process = osManager.processManager.getProcessForApp(com.example.backdoor.game.OsApp.MARKETPLACE)
    val appState = process?.appState as? com.example.backdoor.core.MarketplaceAppState
    
    var selectedCategory by remember { appState?.selectedCategory ?: mutableStateOf("ALL") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F111A))
            .padding(16.dp)
    ) {
        Text("Digital Marketplace", color = accentColor, fontSize = 20.sp, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            listOf("ALL", "HARDWARE", "SOFTWARE", "DOCUMENTS", "COSMETICS").forEach { cat ->
                Text(
                    text = cat,
                    color = if (selectedCategory == cat) accentColor else TextSecondary,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.clickable { selectedCategory = cat }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        val filtered = listings.filter { !it.isDarkMarket }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filtered) { listing ->
                ListingCard(listing, accentColor) {
                    // Buy logic
                    val wallet = osManager.economyEngine.walletManager
                    if (wallet.deductFunds(listing.price, listing.currency, "Purchased: ${listing.item.name}")) {
                        marketManager.decreaseStock(listing.id)
                        osManager.economyEngine.inventoryManager.addItem(listing.item)
                        osManager.showNotification("MARKET", "Purchased ${listing.item.name}", com.example.backdoor.core.NotificationLevel.SUCCESS)
                    } else {
                        osManager.showNotification("MARKET", "Insufficient Funds.", com.example.backdoor.core.NotificationLevel.ERROR)
                    }
                }
            }
        }
    }
}

@Composable
fun ListingCard(listing: MarketListing, accentColor: Color, onBuy: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, accentColor.copy(alpha = 0.5f))
            .background(Color(0xFF161925))
            .padding(12.dp)
    ) {
        Text(text = listing.item.name, color = TextPrimary, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
        Text(text = "Seller: ${listing.sellerId} | Rep: ${listing.sellerReputation}", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "${listing.price} ${listing.currency.symbol}", color = Color.Green, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
        Text(text = "Stock: ${listing.stock}", color = TextSecondary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
        
        Text(
            text = "[ BUY ]",
            color = accentColor,
            modifier = Modifier
                .padding(top = 8.dp)
                .clickable { onBuy() },
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp
        )
    }
}
