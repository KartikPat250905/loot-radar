package com.radarlabs.freegameradar.data

import app.cash.sqldelight.db.SqlDriver

expect object DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}
