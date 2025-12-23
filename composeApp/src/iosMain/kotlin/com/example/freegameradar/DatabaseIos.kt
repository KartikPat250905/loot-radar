package com.example.freegameradar

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.example.freegameradar.db.GameDatabase

fun createDatabase(): GameDatabase {
    val driver = NativeSqliteDriver(GameDatabase.Schema, "loot_radar.db")
    return GameDatabase(driver)
}
