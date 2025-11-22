package be.ecam.server.models

// DAO
import be.ecam.server.db.DatabaseFactory

// Services / DTOs
import be.ecam.server.services.AdminService
import be.ecam.common.api.AdminDTO

// Kotlin / testing
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import java.time.Instant
import java.io.File


class AdminCRUDTest {

    private fun ensureDbReady() {
        // Delete any existing test SQLite DB so tests start with a clean schema.
        // This is safe for local/dev/CI test DBs; do NOT do this against production DBs.
        val dbFile = File("data/sqlite.db")
        if (dbFile.exists()) {
            dbFile.delete()
        }

        // Connect to DB (DatabaseFactory.connect creates data dir and jdbc url)
        DatabaseFactory.connect()

        // Create the tables (parents first). This avoids ALTER TABLE operations.
        transaction {
            SchemaUtils.create(
                // create parent tables first
                PersonTable,
                // then role tables
                AdminTable
            )
        }
    }

    @Test
    fun addAdmin_directDbFlow() {
        ensureDbReady()

        // Keep DB operations local to a single transaction for test determinism
        transaction {
            // Create a Person and corresponding Admin directly via DAO
            val person = Person.new {
                firstName = "Admin"
                lastName = "User"
                email = "admin@example.com"
                password = "123456"
                createdAt = Instant.now().toString()
            }
            Admin.createForPerson(person)

            // Assertions inside transaction to avoid visibility issues
            assertEquals(1L, Person.find { PersonTable.email eq "admin@example.com" }.count(), "Expected one person")
            assertEquals(1L, Admin.all().count(), "Expected one admin")
        }
    }

    @Test
    fun createAdmin_missingPassword_throws() {
        ensureDbReady()

        val svc = AdminService()

        val dto = AdminDTO(
            id = null,
            firstName = "NoPw",
            lastName = "User",
            email = "nopw@example.com",
            password = null, // missing password
            createdAt = Instant.now().toString()
        )

        // Expect IllegalArgumentException because password is required by service
        assertFailsWith<IllegalArgumentException> {
            svc.createAdminFromDto(dto)
        }
    }

    @Test
    fun createAdmin_missingEmail_throws() {
        ensureDbReady()

        val svc = AdminService()

        val dto = AdminDTO(
            id = null,
            firstName = "NoEmail",
            lastName = "User",
            email = "", // empty email
            password = "somepass",
            createdAt = Instant.now().toString()
        )

        // Expect IllegalArgumentException because email is required and validated
        assertFailsWith<IllegalArgumentException> {
            svc.createAdminFromDto(dto)
        }
    }
}