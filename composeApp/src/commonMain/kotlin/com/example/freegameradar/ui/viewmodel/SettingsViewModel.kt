package com.example.freegameradar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freegameradar.data.auth.AuthRepository
import com.example.freegameradar.data.models.User
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class SettingsViewModel(private val authRepository: AuthRepository) : ViewModel() {

    val user: StateFlow<User?> = authRepository.getAuthStateFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isGuest: StateFlow<Boolean> = user.map { it?.isAnonymous == true }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun signOut() {
        // TODO: Implement sign out logic
    }

    fun deleteAccount() {
        // TODO: Implement delete account logic
    }
}
