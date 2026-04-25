package com.radarlabs.freegameradar.ui.viewmodel

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
            EARLY_ACCESS -> "Beta"
        }
    }
}
