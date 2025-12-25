package com.example.freegameradar.data

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.example.freegameradar.db.GameDatabase

actual object DatabaseDriverFactory {
    lateinit var context: Context

    fun init(context: Context) {
        this.context = context
    }

    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(GameDatabase.Schema, context, "loot_radar.db")
    }
}
