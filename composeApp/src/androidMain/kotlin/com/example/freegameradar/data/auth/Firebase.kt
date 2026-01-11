package com.example.freegameradar.data.auth

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.CoroutineContext

actual fun getFirebaseAuth(): FirebaseAuthWrapper {
    return AndroidFirebaseAuthWrapper(FirebaseAuth.getInstance())
}

actual fun getCoroutineContext(): CoroutineContext {
    return Dispatchers.IO
}

class AndroidFirebaseAuthWrapper(private val auth: FirebaseAuth) : FirebaseAuthWrapper {
    override suspend fun signIn(email: String, password: String): AuthResult? {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        return result.user?.let {
            AuthResult(UserInfo(it.uid, it.email))
        }
    }
    
    override suspend fun signUp(email: String, password: String): AuthResult? {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        return result.user?.let {
            AuthResult(UserInfo(it.uid, it.email))
        }
    }
    
    override suspend fun signOut() {
        auth.signOut()
    }
    
    override fun getCurrentUser(): UserInfo? {
        return auth.currentUser?.let { UserInfo(it.uid, it.email) }
    }
}
