package com.example.freegameradar.data.auth

import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

actual fun getFirebaseAuth(): FirebaseAuthWrapper {
    return DesktopFirebaseAuthStub()
}

actual fun getCoroutineContext(): CoroutineContext {
    return Dispatchers.IO
}

class DesktopFirebaseAuthStub : FirebaseAuthWrapper {
    override suspend fun signIn(email: String, password: String): AuthResult? {
        println("Desktop Firebase stub: signIn called")
        return null // Implement REST API later
    }
    
    override suspend fun signUp(email: String, password: String): AuthResult? {
        println("Desktop Firebase stub: signUp called")
        return null
    }
    
    override suspend fun signOut() {
        println("Desktop Firebase stub: signOut called")
    }
    
    override fun getCurrentUser(): UserInfo? {
        return null
    }
}
