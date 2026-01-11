package com.example.freegameradar.data.repository

import com.example.freegameradar.data.auth.AuthRepository

// Don't mark individual methods with 'expect'
expect class UserSettingsRepositoryImpl(
    authRepository: AuthRepository
) : UserSettingsRepository
