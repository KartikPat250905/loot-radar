package com.example.freegameradar

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.example.freegameradar.db.GameDatabase


object DatabaseProvider {

    private var database: GameDatabase? = null

    fun getDatabase(): GameDatabase {
        if (database == null) {
            val driver = NativeSqliteDriver(GameDatabase.Schema, "loot_radar.db")
            database = GameDatabase(driver)
        }
        return database!!
    }
}
