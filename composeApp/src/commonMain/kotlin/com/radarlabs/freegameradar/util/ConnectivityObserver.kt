package com.radarlabs.freegameradar.util

import kotlinx.coroutines.flow.Flow

expect class ConnectivityObserver() {
    val isOnline: Flow<Boolean>
}
