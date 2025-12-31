package com.example.freegameradar.core

import android.content.Context
import com.example.freegameradar.FreeGameRadarApp
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SharedPreferencesSettings

actual fun createSettings(): ObservableSettings {
    val context = FreeGameRadarApp.instance.applicationContext
    val delegate = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    return SharedPreferencesSettings(delegate)
}
