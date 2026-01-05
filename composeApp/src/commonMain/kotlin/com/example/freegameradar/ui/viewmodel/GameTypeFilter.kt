package com.example.freegameradar.ui.viewmodel

enum class GameTypeFilter {
    ALL,
    GAME,
    LOOT,
    DLC;

    fun toDisplayString(): String = when (this) {
        ALL -> "All"
        GAME -> "Game"
        LOOT -> "Loot"
        DLC -> "DLC"
    }
    
    fun toApiValue(): String? = when (this) {
        ALL -> null
        GAME -> "Game"
        LOOT -> "Loot"
        DLC -> "DLC"
    }
}