package com.example.lootradarkmp.data

import com.example.lootradarkmp.db.GameDatabase

interface Database {
    fun getDatabase(): GameDatabase
}
