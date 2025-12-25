package com.example.freegameradar

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.example.freegameradar.db.GameDatabase

object DatabaseProvider {

    private var database: GameDatabase? = null

    fun getDatabase(context: Context): GameDatabase {
        if (database == null) {
            val driver = AndroidSqliteDriver(GameDatabase.Schema, context, "loot_radar.db")
            database = GameDatabase(driver)
        }
        return database!!
    }
}
