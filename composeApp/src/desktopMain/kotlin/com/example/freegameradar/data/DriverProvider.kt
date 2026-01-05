package com.example.freegameradar.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import app.cash.sqldelight.db.SqlDriver

@Composable
actual fun rememberSqlDriver(): SqlDriver {
    return remember { DatabaseDriverFactory().createDriver() }
}
