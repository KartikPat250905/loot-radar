package com.example.freegameradar

import android.app.Application
import com.example.freegameradar.data.DatabaseDriverFactory

class FreeGameRadarApp : Application() {
    override fun onCreate() {
        super.onCreate()
        DatabaseDriverFactory.init(this)
    }
}
