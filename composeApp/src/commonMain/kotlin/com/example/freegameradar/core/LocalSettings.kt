package com.example.freegameradar.core

import com.russhwolf.settings.Settings
import com.russhwolf.settings.boolean

object LocalSettings {
    private val settings: Settings = createSettings()

    private const val KEY_SETUP_COMPLETE = "setup_complete"

    var isSetupComplete: Boolean by settings.boolean(KEY_SETUP_COMPLETE, false)
}
