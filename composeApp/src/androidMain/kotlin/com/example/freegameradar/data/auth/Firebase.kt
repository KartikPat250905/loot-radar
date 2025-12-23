package com.example.freegameradar.data.auth

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

actual typealias FirebaseAuth = com.google.firebase.auth.FirebaseAuth

actual fun getFirebaseAuth(): FirebaseAuth = Firebase.auth

actual fun getCoroutineContext(): CoroutineContext = Dispatchers.IO
