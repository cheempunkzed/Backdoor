package com.example.backdoor.ui.apps

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.KeyboardTab
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import com.example.backdoor.terminal.PromptStyle
import com.example.backdoor.terminal.TerminalSettings
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
    val settingsState by osManager.settingsRepository.settings.collectAsState()
    val termSettings = settingsState.terminalSettings

    val process = osManager.processManager.getProcessForApp(com.example.backdoor.game.OsApp.TERMINAL)
    val appState = process?.appState as? com.example.backdoor.core.TerminalAppState

    var inputCommand by remember { appState?.inputCommand ?: mutableStateOf("") }
    var historyIndex by remember { appState?.historyIndex ?: mutableStateOf(-1) }
    var showSettingsModal by remember { mutableStateOf(false) }

    val history = remember {
        if (appState != null) {
            if (appState.history.isEmpty()) {
                appState.history.add(
                    TerminalHistoryItem(
                        prompt = osManager.terminalSession.formatPrompt(termSettings.promptStyle),
                        command = "version",
                        output = "AbyssOS 0.4.0 Terminal Core initialized. Type 'help' for command list.",
                        error = null
                    )
                )
            }
            appState.history as androidx.compose.runtime.snapshots.SnapshotStateList<TerminalHistoryItem>
        } else {
            mutableStateListOf(
                TerminalHistoryItem(
                    prompt = osManager.terminalSession.formatPrompt(termSettings.promptStyle),
                    command = "version",
                    output = "AbyssOS 0.4.0 Terminal Core initialized. Type 'help' for command list.",
                    error = null
                )
            )
        }
    }

    LaunchedEffect(Unit) {
        val saved = osManager.saveManager.getTerminalHistory()
        if (saved.isNotEmpty()) {
            osManager.commandExecutor.setHistory(saved)
        }
    }

    val listState = rememberLazyListState()

    // Blinking cursor animation
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

    val promptText = osManager.terminalSession.formatPrompt(termSettings.promptStyle)

    // Autocomplete Suggestions
    val suggestions = remember(inputCommand) {
        if (inputCommand.isNotEmpty()) {
            osManager.commandRegistry.getAutocompleteSuggestions(inputCommand, osManager.terminalSession)
        } else {
            emptyList()
        }
    }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

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

                // Persist command history
                val executedHistory = osManager.commandExecutor.getHistory()
                osManager.saveManager.saveTerminalHistory(executedHistory)

                if (result.action is CommandAction.ExitTerminal) {
                    osManager.closeActiveApp()
                } else if (result.action is CommandAction.OpenApp) {
                    val targetAppName = (result.action as CommandAction.OpenApp).appName
                    osManager.getCommandContext().onOpenAppRequested(targetAppName)
                }

                inputCommand = ""
                historyIndex = -1
                listState.animateScrollToItem((history.size).coerceAtLeast(0))
            }
        }
    }

    val handleTabAutocomplete = {
        if (suggestions.isNotEmpty()) {
            val best = suggestions.first()
            val tokens = inputCommand.trimStart().split("\\s+".toRegex())
            if (tokens.size <= 1) {
                inputCommand = "$best "
            } else {
                val prefix = tokens.dropLast(1).joinToString(" ")
                inputCommand = "$prefix $best "
            }
        }
    }

    val navigateHistoryUp = {
        val allHistory = osManager.commandExecutor.getHistory()
        if (allHistory.isNotEmpty()) {
            if (historyIndex == -1) {
                historyIndex = allHistory.size - 1
            } else if (historyIndex > 0) {
                historyIndex--
            }
            if (historyIndex in allHistory.indices) {
                inputCommand = allHistory[historyIndex]
            }
        }
    }

    val navigateHistoryDown = {
        val allHistory = osManager.commandExecutor.getHistory()
        if (historyIndex != -1) {
            if (historyIndex < allHistory.size - 1) {
                historyIndex++
                inputCommand = allHistory[historyIndex]
            } else {
                historyIndex = -1
                inputCommand = ""
            }
        }
    }

    val effectiveTextColor = try {
        Color(android.graphics.Color.parseColor(termSettings.textColorHex))
    } catch (e: Exception) {
        accentColor
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AbyssBackground.copy(alpha = termSettings.terminalOpacity))
            .padding(8.dp)
    ) {
        // Top Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TERMINAL CORE 0.4.0",
                color = effectiveTextColor.copy(alpha = 0.7f),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { showSettingsModal = !showSettingsModal }, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Terminal Settings",
                        tint = effectiveTextColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Settings Modal View
        AnimatedVisibility(visible = showSettingsModal) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AbyssSurfaceVariant)
                    .padding(8.dp)
            ) {
                Text("TERMINAL SETTINGS", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Cursor Blink", color = TextMuted, fontSize = 11.sp)
                    Switch(
                        checked = termSettings.cursorBlink,
                        onCheckedChange = {
                            osManager.settingsRepository.updateTerminalSettings(termSettings.copy(cursorBlink = it))
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Font Size (${termSettings.fontSize} sp)", color = TextMuted, fontSize = 11.sp)
                    Slider(
                        value = termSettings.fontSize.toFloat(),
                        onValueChange = {
                            osManager.settingsRepository.updateTerminalSettings(termSettings.copy(fontSize = it.toInt()))
                        },
                        valueRange = 10f..18f,
                        modifier = Modifier.width(120.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Prompt Style", color = TextMuted, fontSize = 11.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(PromptStyle.entries.toTypedArray()) { style ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (termSettings.promptStyle == style) effectiveTextColor else AbyssBackground)
                                    .clickable {
                                        osManager.settingsRepository.updateTerminalSettings(termSettings.copy(promptStyle = style))
                                    }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = style.name,
                                    color = if (termSettings.promptStyle == style) AbyssBackground else TextPrimary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Output Stream & Active Input Prompt
        SelectionContainer(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        focusRequester.requestFocus()
                    }
            ) {
                // History Items
                items(history) { item ->
                    Column(modifier = Modifier.padding(vertical = 3.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.prompt,
                                color = effectiveTextColor,
                                fontSize = termSettings.fontSize.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = item.command,
                                color = TextPrimary,
                                fontSize = termSettings.fontSize.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        item.output?.let { out ->
                            Text(
                                text = out,
                                color = effectiveTextColor.copy(alpha = 0.9f),
                                fontSize = (termSettings.fontSize - 1).sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                            )
                        }

                        item.error?.let { err ->
                            Text(
                                text = err,
                                color = NeonRed,
                                fontSize = (termSettings.fontSize - 1).sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                            )
                        }
                    }
                }

                // Active Input Prompt Line
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = promptText,
                            color = effectiveTextColor,
                            fontSize = termSettings.fontSize.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        BasicTextField(
                            value = inputCommand,
                            onValueChange = { inputCommand = it },
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester),
                            textStyle = TextStyle(
                                color = TextPrimary,
                                fontSize = termSettings.fontSize.sp,
                                fontFamily = FontFamily.Monospace
                            ),
                            cursorBrush = SolidColor(
                                if (termSettings.cursorBlink) effectiveTextColor.copy(alpha = cursorAlpha) else effectiveTextColor
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { submitCommand(inputCommand) }),
                            singleLine = true,
                            decorationBox = { innerTextField ->
                                innerTextField()
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Autocomplete Suggestion Chips
        if (suggestions.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(suggestions) { sugg ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(effectiveTextColor.copy(alpha = 0.15f))
                            .border(0.5.dp, effectiveTextColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .clickable {
                                handleTabAutocomplete()
                            }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = sugg,
                            color = effectiveTextColor,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Quick Command Toolbar & Navigation Buttons
        val quickCommands = listOf("help", "ls -la", "pwd", "tree", "history", "whoami", "clear")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(quickCommands) { cmd ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(AbyssSurfaceVariant)
                            .border(0.5.dp, effectiveTextColor.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                            .clickable { submitCommand(cmd) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = cmd,
                            color = effectiveTextColor,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { handleTabAutocomplete() }, modifier = Modifier.size(26.dp)) {
                    Icon(imageVector = Icons.Default.KeyboardTab, contentDescription = "Tab", tint = effectiveTextColor)
                }
                IconButton(onClick = { navigateHistoryUp() }, modifier = Modifier.size(26.dp)) {
                    Icon(imageVector = Icons.Default.ArrowDropUp, contentDescription = "Up", tint = effectiveTextColor)
                }
                IconButton(onClick = { navigateHistoryDown() }, modifier = Modifier.size(26.dp)) {
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Down", tint = effectiveTextColor)
                }
                IconButton(
                    onClick = { submitCommand(inputCommand) },
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Run",
                        tint = effectiveTextColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
