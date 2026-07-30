package com.example.backdoor.ui.apps

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.backdoor.game.AbyssOSManager
import com.example.backdoor.terminal.CommandAction
import com.example.ui.theme.AbyssBackground
import com.example.ui.theme.AbyssSurfaceVariant
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import kotlinx.coroutines.launch

data class TerminalHistoryItem(
    val prompt: String,
    val command: String,
    val output: String?,
    val error: String?
)

@Composable
fun TerminalApp(
    osManager: AbyssOSManager,
    accentColor: Color = TerminalGreen,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var inputCommand by remember { mutableStateOf("") }
    val history = remember {
        mutableStateListOf(
            TerminalHistoryItem(
                prompt = "system@abyssos:~",
                command = "version",
                output = "AbyssOS 0.1 Alpha Terminal Engine. Type 'help' for available commands.",
                error = null
            )
        )
    }

    LaunchedEffect(Unit) {
        val saved = osManager.saveManager.getTerminalHistory()
        if (saved.isNotEmpty()) {
            saved.forEach { cmd ->
                history.add(
                    TerminalHistoryItem(
                        prompt = "${osManager.systemStatus.value.userHandle}@${osManager.systemStatus.value.hostname}:~$",
                        command = cmd,
                        output = "Loaded command from persistent session history.",
                        error = null
                    )
                )
            }
        }
    }

    val listState = rememberLazyListState()

    // Blinking cursor
    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursorAlpha"
    )

    val cwd = osManager.vfs.getCwd()
    val promptText = "${osManager.systemStatus.value.userHandle}@${osManager.systemStatus.value.hostname}:${cwd}$ "

    val submitCommand = { cmdToRun: String ->
        val cmd = cmdToRun.trim()
        if (cmd.isNotEmpty()) {
            scope.launch {
                val context = osManager.getCommandContext()
                val result = osManager.commandExecutor.execute(cmd, context)

                if (result.action is CommandAction.ClearScreen) {
                    history.clear()
                } else {
                    history.add(
                        TerminalHistoryItem(
                            prompt = promptText,
                            command = cmd,
                            output = result.output.ifEmpty { null },
                            error = result.error
                        )
                    )
                }

                if (result.action is CommandAction.ExitTerminal) {
                    osManager.closeActiveApp()
                } else if (result.action is CommandAction.OpenApp) {
                    val targetAppName = (result.action as CommandAction.OpenApp).appName
                    osManager.getCommandContext().onOpenAppRequested(targetAppName)
                }

                inputCommand = ""
                listState.animateScrollToItem((history.size - 1).coerceAtLeast(0))
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AbyssBackground)
            .padding(8.dp)
    ) {
        // Command Output Stream
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(history) { item ->
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.prompt,
                            color = accentColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = item.command,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    item.output?.let { out ->
                        Text(
                            text = out,
                            color = accentColor.copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                        )
                    }

                    item.error?.let { err ->
                        Text(
                            text = err,
                            color = NeonRed,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Quick Command Chips
        val quickCommands = listOf("help", "ls", "pwd", "tree", "version", "whoami", "clear")
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(quickCommands) { cmd ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(AbyssSurfaceVariant)
                        .border(0.5.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        .clickable { submitCommand(cmd) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = cmd,
                        color = accentColor,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Active Prompt & Input Field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AbyssSurfaceVariant)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = promptText,
                color = accentColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.width(4.dp))

            BasicTextField(
                value = inputCommand,
                onValueChange = { inputCommand = it },
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace
                ),
                cursorBrush = SolidColor(accentColor),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submitCommand(inputCommand) }),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (inputCommand.isEmpty()) {
                            Text(
                                text = "enter command...",
                                color = TextMuted,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        } else {
                            innerTextField()
                        }
                        Box(
                            modifier = Modifier
                                .width(8.dp)
                                .height(14.dp)
                                .alpha(cursorAlpha)
                                .background(accentColor)
                        )
                    }
                }
            )
        }
    }
}
