package com.example.backdoor.ui.apps.economy

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.backdoor.game.AbyssOSManager
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun NewsApp(
    osManager: AbyssOSManager,
    accentColor: Color = CyberCyan,
    modifier: Modifier = Modifier
) {
    val newsService = osManager.economyEngine.newsService
    val feed by newsService.feed.collectAsState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F111A))
            .padding(16.dp)
    ) {
        Text("Global News Network", color = accentColor, fontSize = 20.sp, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn {
            items(feed) { article ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .border(1.dp, Color.DarkGray)
                        .padding(12.dp)
                ) {
                    Text(text = "[${article.category.name}] ${article.source}", color = accentColor, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = article.title, color = TextPrimary, fontSize = 16.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = article.content, color = TextSecondary, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}
