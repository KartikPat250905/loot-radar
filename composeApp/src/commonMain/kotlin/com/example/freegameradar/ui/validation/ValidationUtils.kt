package com.example.freegameradar.ui.validation

/**
 * Input validation utilities shared across platforms
 */
object ValidationUtils {

    fun isValidEmail(email: String): Boolean {
        // Simple email validation - for full validation, use regex or library
        return email.trim().isNotEmpty() &&
               email.contains("@") &&
               email.contains(".") &&
               email.length >= 6
    }

    fun getEmailError(email: String): String? {
        val trimmed = email.trim()
        return when {
            trimmed.isEmpty() -> "Email is required"
            !isValidEmail(trimmed) -> "Please enter a valid email address"
            else -> null
        }
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    fun getPasswordError(password: String): String? {
        return when {
            password.isEmpty() -> "Password is required"
            password.length < 6 -> "Password must be at least 6 characters"
            else -> null
        }
    }
}