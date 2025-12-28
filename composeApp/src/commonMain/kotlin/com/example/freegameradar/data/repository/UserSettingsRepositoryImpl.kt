package com.example.freegameradar.data.repository

import com.example.freegameradar.data.auth.AuthRepository

expect class UserSettingsRepositoryImpl(
    authRepository: AuthRepository
) : UserSettingsRepository
