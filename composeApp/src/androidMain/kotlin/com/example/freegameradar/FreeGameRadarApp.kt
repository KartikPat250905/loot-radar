package com.example.freegameradar

import android.app.Application
import com.example.freegameradar.core.image.AndroidContextHolder
import com.example.freegameradar.data.DatabaseDriverFactory

class FreeGameRadarApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize context holders first
        AndroidContextHolder.context = this
        DatabaseDriverFactory.init(this)
    }
}
