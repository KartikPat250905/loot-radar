package com.example.lootradarkmp.data.auth

import cocoapods.FirebaseAuth.FIRAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlin.coroutines.CoroutineContext

actual typealias FirebaseAuth = FIRAuth

actual fun getFirebaseAuth(): FirebaseAuth = FIRAuth.auth()

actual fun getCoroutineContext(): CoroutineContext = Dispatchers.IO
