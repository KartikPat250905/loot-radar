package com.radarlabs.freegameradar.data

import com.radarlabs.freegameradar.db.GameDatabase

interface Database {
    fun getDatabase(): GameDatabase
}
