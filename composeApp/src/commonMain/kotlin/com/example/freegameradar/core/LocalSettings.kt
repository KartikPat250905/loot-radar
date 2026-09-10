package com.radarlabs.freegameradar.core

import com.russhwolf.settings.Settings
import com.russhwolf.settings.boolean

object LocalSettings {
    private val settings: Settings = createSettings()

    private const val KEY_SETUP_COMPLETE = "setup_complete"
    private const val KEY_LAST_KNOWN_WORTH = "last_known_worth"

    var isSetupComplete: Boolean by settings.boolean(KEY_SETUP_COMPLETE, false)

    var lastKnownWorth: String?
        get() = settings.getStringOrNull(KEY_LAST_KNOWN_WORTH)
        set(value) {
            if (value != null) {
                settings.putString(KEY_LAST_KNOWN_WORTH, value)
            } else {
                settings.remove(KEY_LAST_KNOWN_WORTH)
            }
        }
}
