package be.ecam.server.util

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class ValidationTest {
    @Test fun email_valid() {
        assertTrue(isValidEmail("alice@example.com"))
        assertTrue(isValidEmail("bob.smith+tag@sub.domain.co"))
    }
    @Test fun email_invalid() {
        assertFalse(isValidEmail(null))
        assertFalse(isValidEmail(""))
        assertFalse(isValidEmail("no-at-sign"))
        assertFalse(isValidEmail("a@b")) // no .com
    }
    @Test fun password_rules() {
        assertTrue(isValidPassword("123456", 6))
        assertFalse(isValidPassword("12345", 6))
    }
    @Test fun require_valid_helpers_throw() {
        assertFailsWith<ValidationException> { requireValidEmail("bad") }
        assertFailsWith<ValidationException> { requireValidPassword("123") }
    }
}