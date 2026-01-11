package com.example.freegameradar.core

import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.PreferencesSettings
import java.util.prefs.Preferences

actual fun createSettings(): ObservableSettings {
    val preferences = Preferences.userRoot().node("com.example.freegameradar.settings")
    return PreferencesSettings(preferences)
}
