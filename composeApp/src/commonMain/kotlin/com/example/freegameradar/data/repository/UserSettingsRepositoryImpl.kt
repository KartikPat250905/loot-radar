package com.radarlabs.freegameradar.data.repository

import com.radarlabs.freegameradar.data.auth.AuthRepository

expect class UserSettingsRepositoryImpl(
    authRepository: AuthRepository,
    context: Any // Use Any for cross-platform compatibility, will be cast to Context on Android
) : UserSettingsRepository
