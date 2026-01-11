package com.example.freegameradar.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import app.cash.sqldelight.db.SqlDriver

@Composable
actual fun rememberSqlDriver(): SqlDriver {
    val context = LocalContext.current
    return remember { DatabaseDriverFactory(context).createDriver() }
}
