package com.example.freegameradar.data

import com.example.freegameradar.db.GameDatabase

object GameDatabaseProvider {
    private var database: GameDatabase? = null

    fun getDatabase(): GameDatabase {
        if (database == null) {
            database = GameDatabase(DatabaseDriverFactory.createDriver())
        }
        return database!!
    }

    fun clearAllData() {
        database?.gameQueries?.deleteAll()
        database?.notificationsQueries?.clearAllNotifications()
        database?.user_settingsQueries?.deleteSettings()
    }
}
