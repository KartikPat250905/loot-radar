package com.radarlabs.freegameradar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radarlabs.freegameradar.data.auth.AuthRepository
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.concurrent.TimeUnit

data class AdFreeStatus(
    val isAdFree: Boolean = false,
    val expiryDate: Date? = null,
    val daysRemaining: Int = 0,
    val supportTier: String? = null
)

class AdFreeViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _adFreeStatus = MutableStateFlow(AdFreeStatus())
    val adFreeStatus: StateFlow<AdFreeStatus> = _adFreeStatus.asStateFlow()

    private val firestore = Firebase.firestore

    init {
        loadAdFreeStatus()
    }

    fun loadAdFreeStatus() {
        viewModelScope.launch {
            val isAdFree = authRepository.isAdFree()

            if (isAdFree) {
                try {
                    // Get user from auth flow
                    authRepository.getAuthStateFlow().collect { user ->
                        if (user != null && !user.isAnonymous) {
                            val userDoc = firestore.collection("users")
                                .document(user.uid)
                                .get()
                                .await()

                            val adFreeUntil = userDoc.getTimestamp("adFreeUntil")?.toDate()
                            val supportTier = userDoc.getString("supportTier")

                            if (adFreeUntil != null) {
                                val now = Date()
                                val diffInMillis = adFreeUntil.time - now.time
                                val daysRemaining = TimeUnit.MILLISECONDS.toDays(diffInMillis).toInt()

                                _adFreeStatus.value = AdFreeStatus(
                                    isAdFree = true,
                                    expiryDate = adFreeUntil,
                                    daysRemaining = daysRemaining,
                                    supportTier = supportTier
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    println("Error loading ad-free details: ${e.message}")
                }
            } else {
                _adFreeStatus.value = AdFreeStatus(isAdFree = false)
            }
        }
    }

    fun shouldShowAd(): Boolean {
        return !_adFreeStatus.value.isAdFree
    }
}
