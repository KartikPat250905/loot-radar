package com.example.freegameradar.ui.viewmodel

enum class GameTypeFilter {
    ALL,
    GAMES,
    DLC,
    EARLY_ACCESS;

    fun toDisplayString(): String {
        return when (this) {
            ALL -> "All"
            GAMES -> "Games"
            DLC -> "DLC"
            EARLY_ACCESS -> "Early Access"
        }
    }
}
