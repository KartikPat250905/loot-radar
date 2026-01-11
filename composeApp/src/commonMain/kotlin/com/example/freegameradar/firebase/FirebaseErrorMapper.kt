package com.example.freegameradar.firebase

/**
 * Maps Firebase REST API error codes to user-friendly messages
 * Based on Firebase Auth REST API error codes [web:38][web:41][web:47]
 */
object FirebaseErrorMapper {

    fun mapError(error: FirebaseError): String {
        return when {
            error.message.contains("EMAIL_NOT_FOUND", ignoreCase = true) ->
                "No account found with this email address."

            error.message.contains("INVALID_PASSWORD", ignoreCase = true) ->
                "Incorrect password. Please try again."

            error.message.contains("WEAK_PASSWORD", ignoreCase = true) ||
            error.message.contains("PASSWORD_TOO_SHORT", ignoreCase = true) ->
                "Password must be at least 6 characters long."

            error.message.contains("EMAIL_EXISTS", ignoreCase = true) ->
                "An account with this email already exists."

            error.message.contains("INVALID_EMAIL", ignoreCase = true) ->
                "Please enter a valid email address."

            error.message.contains("USER_DISABLED", ignoreCase = true) ->
                "This account has been disabled."

            error.message.contains("TOO_MANY_ATTEMPTS_TRY_LATER", ignoreCase = true) ->
                "Too many failed attempts. Please try again later."

            error.message.contains("INVALID_REFRESH_TOKEN", ignoreCase = true) ->
                "Session expired. Please sign in again."

            error.message.contains("OPERATION_NOT_ALLOWED", ignoreCase = true) ->
                "Email/password authentication is not enabled. Please contact support."

            error.message.contains("NETWORK_REQUEST_FAILED", ignoreCase = true) ->
                "Network error. Please check your connection and try again."

            else -> {
                "Something went wrong: ${error.message.take(100)}"
            }
        }
    }

    fun mapException(e: Throwable): String {
        return when {
            e.message?.contains("EMAIL_NOT_FOUND") == true -> "No account found with this email."
            e.message?.contains("INVALID_PASSWORD") == true -> "Incorrect password."
            e.message?.contains("EMAIL_EXISTS") == true -> "Account already exists."
            e.message?.contains("Network") == true || e.message?.contains("timeout") == true ->
                "Network error. Please check your connection."
            else -> e.message ?: "An unexpected error occurred. Please try again."
        }
    }
}