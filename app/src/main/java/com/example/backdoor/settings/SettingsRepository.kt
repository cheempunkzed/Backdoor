package com.example.backdoor.settings

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TechPurple
import com.example.ui.theme.TerminalGreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

import com.example.backdoor.terminal.TerminalSettings
import com.example.backdoor.terminal.PromptStyle

enum class OsTheme(val displayName: String, val primaryColor: Color) {
    TECH_PURPLE("Tech Purple", TechPurple),
    MATRIX_GREEN("Matrix Green", TerminalGreen),
    CYBER_CYAN("Cyber Cyan", CyberCyan),
    AMBER_ALERT("Amber Alert", AmberAlert),
    NEON_RED("Neon Red", NeonRed)
}

data class OsSettings(
    val theme: OsTheme = OsTheme.TECH_PURPLE,
    val crtEffectEnabled: Boolean = true,
    val soundFxEnabled: Boolean = true,
    val bootAnimationSpeedMs: Long = 350L,
    val fontScale: Float = 1.0f,
    val userHandle: String = "operator",
    val hostname: String = "abyss-node-01",
    val terminalSettings: TerminalSettings = TerminalSettings()
)

class SettingsRepository {
    private val _settings = MutableStateFlow(OsSettings())
    val settings: StateFlow<OsSettings> = _settings.asStateFlow()

    fun updateTheme(theme: OsTheme) {
        _settings.value = _settings.value.copy(theme = theme)
    }

    fun toggleCrtEffect(enabled: Boolean) {
        _settings.value = _settings.value.copy(crtEffectEnabled = enabled)
    }

    fun toggleSoundFx(enabled: Boolean) {
        _settings.value = _settings.value.copy(soundFxEnabled = enabled)
    }

    fun updateFontScale(scale: Float) {
        _settings.value = _settings.value.copy(fontScale = scale)
    }

    fun updateUserHandle(handle: String) {
        if (handle.isNotBlank()) {
            _settings.value = _settings.value.copy(userHandle = handle.lowercase().trim())
        }
    }

    fun updateTerminalSettings(terminalSettings: TerminalSettings) {
        _settings.value = _settings.value.copy(terminalSettings = terminalSettings)
    }
}
