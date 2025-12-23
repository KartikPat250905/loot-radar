package com.example.freegameradar.data

import com.example.freegameradar.db.GameDatabase

interface Database {
    fun getDatabase(): GameDatabase
}
