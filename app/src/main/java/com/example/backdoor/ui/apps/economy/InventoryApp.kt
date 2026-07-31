package com.example.backdoor.ui.apps.economy

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.backdoor.game.AbyssOSManager
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun InventoryApp(
    osManager: AbyssOSManager,
    accentColor: Color = CyberCyan,
    modifier: Modifier = Modifier
) {
    val inventoryManager = osManager.economyEngine.inventoryManager
    val items by inventoryManager.items.collectAsState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F111A))
            .padding(16.dp)
    ) {
        Text("Digital Inventory", color = accentColor, fontSize = 20.sp, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(items) { item ->
                val rarityColor = when(item.rarity) {
                    com.example.backdoor.economy.models.ItemRarity.COMMON -> Color.Gray
                    com.example.backdoor.economy.models.ItemRarity.UNCOMMON -> Color.Green
                    com.example.backdoor.economy.models.ItemRarity.RARE -> Color.Blue
                    com.example.backdoor.economy.models.ItemRarity.EPIC -> Color.Magenta
                    com.example.backdoor.economy.models.ItemRarity.LEGENDARY -> Color.Yellow
                    com.example.backdoor.economy.models.ItemRarity.CLASSIFIED -> Color.Red
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, rarityColor.copy(alpha = 0.5f))
                        .background(Color(0xFF161925))
                        .padding(12.dp)
                ) {
                    Text(text = item.name, color = TextPrimary, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
                    Text(text = "${item.type} | ${item.rarity}", color = rarityColor, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = item.description, color = TextSecondary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}
