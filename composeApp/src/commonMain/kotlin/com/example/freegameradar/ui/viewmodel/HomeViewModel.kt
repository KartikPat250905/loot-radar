package com.example.freegameradar.ui.viewmodel

import com.example.freegameradar.data.models.GameDto
import com.example.freegameradar.data.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val gameRepository: GameRepository
) : KmpViewModel() { // Inherit from KmpViewModel

    private val _games = MutableStateFlow<List<GameDto>>(emptyList())
    val games: StateFlow<List<GameDto>> = _games.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadGames()
    }

    fun loadGames() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Use the correct GameRepository method
                gameRepository.getFreeGames().collect { gameList ->
                    _games.value = gameList
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load games"
                _isLoading.value = false
            }
        }
    }

    fun refresh() {
        loadGames()
    }
}
