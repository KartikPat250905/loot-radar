package com.example.freegameradar

import com.example.freegameradar.data.DatabaseDriverFactory
import com.example.freegameradar.data.GameDatabaseProvider

fun initializeDatabase() {
    val driver = DatabaseDriverFactory().createDriver()
    GameDatabaseProvider.init(driver)
}
