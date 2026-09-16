package com.radarlabs.freegameradar.data

import com.radarlabs.freegameradar.db.GameDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GameDatabaseProvider {
    private var database: GameDatabase? = null

    fun getDatabase(): GameDatabase {
        if (database == null) {
            database = GameDatabase(DatabaseDriverFactory.createDriver())
        }
        return database!!
    }

    suspend fun clearAllData() {
        withContext(Dispatchers.IO) {
            database?.gameQueries?.deleteAll()
            database?.notificationsQueries?.clearAllNotifications()
            database?.user_settingsQueries?.deleteSettings()
        }
    }

    suspend fun clearAllNotifications() {
        database?.notificationsQueries?.clearAllNotifications()
    }
}