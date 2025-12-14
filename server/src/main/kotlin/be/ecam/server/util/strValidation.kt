package be.ecam.server.util

import java.util.regex.Pattern

class ValidationException(message: String) : IllegalArgumentException(message)

/**
 * Very small email validator:
 * - requires an '@' and a domain that contains a dot
 * - simple but practical; replace the regex if you need stricter/looser rules.
 */
fun isValidEmail(email: String?): Boolean {
    if (email == null) return false
    val trimmed = email.trim()
    if (trimmed.isEmpty()) return false
    // Basic pattern: local@domain.tld (doesn't cover all RFC cases but is practical)
    val emailRegex =
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$"
    return Pattern.compile(emailRegex).matcher(trimmed).matches()
}

/**
 * Password policy:
 * - non-null
 * - min length (6)
 * - optional: you can extend to require digits, upper/lowercase, symbols
 */
fun isValidPassword(password: String?, minLength: Int = 6): Boolean {
    if (password == null) return false
    val p = password.trim()
    if (p.length < minLength) return false
    return true
}

/**
 * Helper that throws a ValidationException with a friendly message when check fails.
 */
fun requireValidEmail(email: String?, fieldName: String = "email") {
    if (!isValidEmail(email)) throw ValidationException("Invalid $fieldName: '$email'")
}

fun requireValidPassword(password: String?, minLength: Int = 6, fieldName: String = "password") {
    if (!isValidPassword(password, minLength)) throw ValidationException("$fieldName must be at least $minLength characters")
}