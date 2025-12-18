package com.example.lootradarkmp

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.example.lootradarkmp.db.GameDatabase

fun createDatabase(): GameDatabase {
    val driver = NativeSqliteDriver(GameDatabase.Schema, "loot_radar.db")
    return GameDatabase(driver)
}
