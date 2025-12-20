package com.example.lootradarkmp.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.example.lootradarkmp.db.GameDatabase

actual object DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(GameDatabase.Schema, "loot_radar.db")
    }
}
