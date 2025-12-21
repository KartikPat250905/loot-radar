package com.example.lootradarkmp.data.auth

import kotlin.coroutines.CoroutineContext

expect abstract class FirebaseAuth

expect fun getFirebaseAuth(): FirebaseAuth

expect fun getCoroutineContext(): CoroutineContext
