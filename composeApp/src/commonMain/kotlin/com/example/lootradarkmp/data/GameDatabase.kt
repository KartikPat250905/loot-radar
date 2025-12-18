package com.example.lootradarkmp.data

import com.example.lootradarkmp.db.GameDatabase

object GameDatabaseProvider {
    private var database: GameDatabase? = null

    fun getDatabase(): GameDatabase {
        if (database == null) {
            database = GameDatabase(DatabaseDriverFactory.createDriver())
        }
        return database!!
    }
}
