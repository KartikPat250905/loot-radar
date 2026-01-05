package com.example.freegameradar.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.example.freegameradar.db.GameDatabase
import java.io.File

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val dbFile = File(System.getProperty("user.home"), ".freegameradar/GameDatabase.db")
        dbFile.parentFile?.mkdirs() // Create directory if it doesn't exist

        val driver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")

        // Only create schema if tables don't exist yet
        try {
            GameDatabase.Schema.create(driver)
            println("Database schema created successfully")
        } catch (e: Exception) {
            if (e.message?.contains("already exists") == true) {
                println("Database already initialized, using existing schema")
            } else {
                throw e
            }
        }

        return driver
    }
}
