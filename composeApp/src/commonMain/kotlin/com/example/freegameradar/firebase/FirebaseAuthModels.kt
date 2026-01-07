package com.example.freegameradar.firebase

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Firebase Auth REST API request/response models
 * Based on: https://firebase.google.com/docs/reference/rest/auth
 */

// Sign In Request
@Serializable
data class SignInRequest(
    val email: String,
    val password: String,
    val returnSecureToken: Boolean = true
)

// Sign Up Request
@Serializable
data class SignUpRequest(
    val email: String,
    val password: String,
    val returnSecureToken: Boolean = true
)

// Password Reset Request
@Serializable
data class PasswordResetRequest(
    val requestType: String = "PASSWORD_RESET",
    val email: String
)

// Delete Account Request
@Serializable
data class DeleteAccountRequest(
    val idToken: String
)

// Refresh Token Request
@Serializable
data class RefreshTokenRequest(
    @SerialName("grant_type")
    val grantType: String = "refresh_token",
    @SerialName("refresh_token")
    val refreshToken: String
)

// Get User Data Request
@Serializable
data class GetUserDataRequest(
    val idToken: String
)

// Auth Response (Sign In / Sign Up)
@Serializable
data class FirebaseAuthResponse(
    val idToken: String,
    val email: String? = null,
    val refreshToken: String,
    val expiresIn: String,
    val localId: String,
    val registered: Boolean? = null
)

// Refresh Token Response
@Serializable
data class RefreshTokenResponse(
    @SerialName("id_token")
    val idToken: String,
    @SerialName("refresh_token")
    val refreshToken: String,
    @SerialName("expires_in")
    val expiresIn: String,
    @SerialName("user_id")
    val userId: String
)

// User Data Response
@Serializable
data class UserDataResponse(
    val users: List<FirebaseUser>
)

@Serializable
data class FirebaseUser(
    val localId: String,
    val email: String? = null,
    val emailVerified: Boolean = false,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val passwordHash: String? = null,
    val passwordUpdatedAt: Long? = null,
    val validSince: String? = null,
    val disabled: Boolean = false,
    val lastLoginAt: String? = null,
    val createdAt: String? = null,
    val customAuth: Boolean = false
)

// Error Response
@Serializable
data class FirebaseErrorResponse(
    val error: FirebaseError
)

@Serializable
data class FirebaseError(
    val code: Int,
    val message: String,
    val errors: List<FirebaseErrorDetail>? = null
)

@Serializable
data class FirebaseErrorDetail(
    val message: String,
    val domain: String,
    val reason: String
)