package com.radarlabs.freegameradar.data.auth

import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

actual typealias FirebaseAuth = com.google.firebase.auth.FirebaseAuth

actual fun getFirebaseAuth(): FirebaseAuth = Firebase.auth

actual fun getCoroutineContext(): CoroutineContext = Dispatchers.IO
