package com.example.freegameradar.data

import app.cash.sqldelight.db.SqlDriver
import com.example.freegameradar.db.GameDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

object GameDatabaseProvider {
    private var database: GameDatabase? = null

    fun init(driver: SqlDriver) {
        if (database == null) {
            database = GameDatabase(driver)
        }
    }

    fun getDatabase(): GameDatabase {
        return database ?: throw IllegalStateException("GameDatabaseProvider must be initialized before use.")
    }

    suspend fun clearAllData() {
        withContext(Dispatchers.IO) {
            database?.gameQueries?.deleteAll()
            database?.notificationsQueries?.clearAllNotifications()
            database?.user_settingsQueries?.deleteSettings()
        }
    }
}
