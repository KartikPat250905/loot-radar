package com.example.freegameradar.data

import androidx.compose.runtime.Composable
import app.cash.sqldelight.db.SqlDriver

@Composable
expect fun rememberSqlDriver(): SqlDriver
