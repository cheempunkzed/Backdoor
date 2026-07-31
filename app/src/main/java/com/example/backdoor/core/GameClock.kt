package com.example.backdoor.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WorldTime(
    val year: Int = 2026,
    val month: Int = 1,
    val day: Int = 1,
    val hour: Int = 8,
    val minute: Int = 0
) {
    override fun toString(): String {
        return String.format("%04d-%02d-%02d %02d:%02d", year, month, day, hour, minute)
    }
}

class GameClock(private val scope: CoroutineScope) {
    private val _time = MutableStateFlow(WorldTime())
    val time: StateFlow<WorldTime> = _time.asStateFlow()

    private var isRunning = false
    private val tickRateMs = 1000L // 1 second real time = 1 minute game time

    fun start() {
        if (isRunning) return
        isRunning = true
        scope.launch {
            while (isRunning) {
                delay(tickRateMs)
                tick()
            }
        }
    }

    fun stop() {
        isRunning = false
    }

    private fun tick() {
        val current = _time.value
        var m = current.minute + 1
        var h = current.hour
        var d = current.day
        var mo = current.month
        var y = current.year

        if (m >= 60) {
            m = 0
            h++
        }
        if (h >= 24) {
            h = 0
            d++
        }
        if (d > 30) { // Simplified 30-day months
            d = 1
            mo++
        }
        if (mo > 12) {
            mo = 1
            y++
        }

        _time.value = WorldTime(y, mo, d, h, m)
    }

    fun setTime(newTime: WorldTime) {
        _time.value = newTime
    }
}
