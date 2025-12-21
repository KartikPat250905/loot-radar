package com.example.lootradarkmp.data.auth

import kotlin.coroutines.CoroutineContext

expect class FirebaseAuth

expect fun getFirebaseAuth(): FirebaseAuth

expect fun getCoroutineContext(): CoroutineContext
