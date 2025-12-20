package com.example.lootradarkmp

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.example.lootradarkmp.db.GameDatabase


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
