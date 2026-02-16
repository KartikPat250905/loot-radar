package com.radarlabs.freegameradar

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.radarlabs.freegameradar.db.GameDatabase

fun createDatabase(context: Context): GameDatabase {
    val driver = AndroidSqliteDriver(GameDatabase.Schema, context, "loot_radar.db")
    return GameDatabase(driver)
}
