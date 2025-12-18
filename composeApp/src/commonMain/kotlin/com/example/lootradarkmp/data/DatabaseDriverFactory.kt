package com.example.lootradarkmp.data

import app.cash.sqldelight.db.SqlDriver

expect object DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}
