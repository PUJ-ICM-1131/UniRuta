package com.uniruta.app.auth

import com.uniruta.app.data.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthRulesTest {

    @Test
    fun placeholderOrBlankRoleConfigurationGrantsNoRoles() {
        assertEquals(0, RoleDirectory.parse("NONE").size)
        assertEquals(0, RoleDirectory.parse("").size)
        assertEquals(0, RoleDirectory.parse(null).size)
        assertNull(RoleDirectory.parse("NONE").assignmentFor("admin@uniruta.com"))
    }

    @Test
    fun roleEntriesAreParsedCaseInsensitively() {
        val directory = RoleDirectory.parse(
            " Ana.Gomez@javeriana.edu.co:student:Ana Gómez ; carlos@javeriana.edu.co:CONDUCTOR, laura@javeriana.edu.co:Admin"
        )

        assertEquals(3, directory.size)
        assertEquals(UserRole.STUDENT, directory.assignmentFor("ana.gomez@JAVERIANA.edu.co")?.role)
        assertEquals("Ana Gómez", directory.assignmentFor("ana.gomez@javeriana.edu.co")?.displayName)
        assertEquals(UserRole.DRIVER, directory.assignmentFor("carlos@javeriana.edu.co")?.role)
        assertEquals(UserRole.ADMIN, directory.assignmentFor(" laura@javeriana.edu.co ")?.role)
    }

    @Test
    fun malformedEntriesAreIgnoredInsteadOfGrantingAccess() {
        val directory = RoleDirectory.parse("sin-correo:ADMIN,otro@javeriana.edu.co:SUPERUSER,valido@javeriana.edu.co")

        assertEquals(0, directory.size)
        assertNull(directory.assignmentFor("otro@javeriana.edu.co"))
        assertNull(directory.assignmentFor(null))
    }

    @Test
    fun firebaseErrorCodesMapToSpanishMessages() {
        assertEquals(AuthError.WRONG_PASSWORD, authErrorForCode("ERROR_WRONG_PASSWORD"))
        assertEquals(AuthError.USER_NOT_FOUND, authErrorForCode("ERROR_USER_NOT_FOUND"))
        assertEquals(AuthError.INVALID_CREDENTIALS, authErrorForCode("ERROR_INVALID_CREDENTIAL"))
        assertEquals(AuthError.INVALID_EMAIL, authErrorForCode("ERROR_INVALID_EMAIL"))
        assertEquals(AuthError.TOO_MANY_REQUESTS, authErrorForCode("ERROR_TOO_MANY_REQUESTS"))
        assertEquals(AuthError.NETWORK, authErrorForCode("ERROR_NETWORK_REQUEST_FAILED"))
        assertEquals(AuthError.NOT_CONFIGURED, authErrorForCode("ERROR_OPERATION_NOT_ALLOWED"))
        assertEquals(AuthError.UNEXPECTED, authErrorForCode("ERROR_SOMETHING_NEW"))
        assertTrue(AuthError.entries.none { it.message.contains("ERROR_") || it.message.contains("Exception") })
    }

    @Test
    fun emailFormatIsValidatedLocally() {
        assertTrue(isValidEmail("ana.gomez@javeriana.edu.co"))
        assertTrue(isValidEmail("  estudiante@uniruta.com "))
        assertFalse(isValidEmail("ana.gomez"))
        assertFalse(isValidEmail("ana@"))
        assertFalse(isValidEmail("@javeriana.edu.co"))
    }
}
