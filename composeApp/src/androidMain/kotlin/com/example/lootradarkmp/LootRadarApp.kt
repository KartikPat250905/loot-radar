package com.example.lootradarkmp

import android.app.Application
import com.example.lootradarkmp.data.DatabaseDriverFactory

class LootRadarApp : Application() {
    override fun onCreate() {
        super.onCreate()
        init(this)
        DatabaseDriverFactory.init(this)
    }
}
