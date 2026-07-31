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
import com.example.backdoor.game.AbyssOSManager
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun MailApp(
    osManager: AbyssOSManager,
    accentColor: Color = CyberCyan,
    modifier: Modifier = Modifier
) {
    val mailService = osManager.economyEngine.mailService
    val inbox by mailService.inbox.collectAsState()
    
    val process = osManager.processManager.getProcessForApp(com.example.backdoor.game.OsApp.MAIL)
    val appState = process?.appState as? com.example.backdoor.core.MailAppState
    
    var selectedMsgId by remember { appState?.selectedMessageId ?: mutableStateOf<String?>(null) }

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F111A))
    ) {
        // Inbox list
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .border(1.dp, Color.DarkGray)
                .padding(8.dp)
        ) {
            Text("INBOX", color = accentColor, fontSize = 16.sp, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn {
                items(inbox) { email ->
                    val color = if (email.isRead) TextSecondary else TextPrimary
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedMsgId = email.id
                                mailService.markAsRead(email.id)
                            }
                            .padding(vertical = 8.dp)
                    ) {
                        Text(email.sender, color = color, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
                        Text(email.subject, color = color, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
        
        // Message view
        Column(
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            val currentMsg = inbox.find { it.id == selectedMsgId }
            if (currentMsg != null) {
                Text("From: ${currentMsg.sender}", color = accentColor, fontSize = 16.sp, fontFamily = FontFamily.Monospace)
                Text("Subject: ${currentMsg.subject}", color = TextPrimary, fontSize = 18.sp, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(16.dp))
                Text(currentMsg.body, color = TextSecondary, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
                
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "[ DELETE ]",
                    color = Color.Red,
                    modifier = Modifier.clickable { 
                        mailService.deleteEmail(currentMsg.id)
                        selectedMsgId = null
                    },
                    fontFamily = FontFamily.Monospace
                )
            } else {
                Text("Select a message to read.", color = TextSecondary, fontFamily = FontFamily.Monospace)
            }
        }
    }
}
