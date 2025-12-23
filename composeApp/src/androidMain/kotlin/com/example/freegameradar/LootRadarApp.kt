package com.example.freegameradar

import android.app.Application
import com.example.freegameradar.data.DatabaseDriverFactory

class LootRadarApp : Application() {
    override fun onCreate() {
        super.onCreate()
        init(this)
        DatabaseDriverFactory.init(this)
    }
}
