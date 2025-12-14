package be.ecam.server.models

// DAO
import be.ecam.server.models.PersonTable
import be.ecam.server.models.AdminTable

// Services / DTOs
import be.ecam.server.services.AdminService
import be.ecam.common.api.AdminDTO
import be.ecam.server.services.PersonService

// Kotlin / testing
import org.jetbrains.exposed.sql.Table
import kotlin.test.BeforeTest
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import java.time.Instant

// Test database utils (for test files only)
import be.ecam.server.testutils.TestDatabase

class AdminCRUDTest : TestDatabase() {
    override val tables: Array<Table> = arrayOf(PersonTable, AdminTable)

    private lateinit var adminService: AdminService

    @BeforeTest
    override fun setupDatabase() {
        super.setupDatabase()
        adminService = AdminService()
    }

    @Test
    fun addAdmin_directDbFlow() {
        transaction {
            val person = Person.new {
                firstName = "Admin"
                lastName = "User"
                email = "admin@example.com"
                password = "123456"
                createdAt = Instant.now().toString()
            }
            Admin.createForPerson(person)

            assertEquals(1L, Person.find { PersonTable.email eq "admin@example.com" }.count(), "Expected one person")
            assertEquals(1L, Admin.all().count(), "Expected one admin")
        }
    }

    @Test
    fun createAdmin_missingPassword_throws() {
        val svc = AdminService()

        val dto = AdminDTO(
            id = null,
            firstName = "NoPw",
            lastName = "User",
            email = "nopw@example.com",
            password = null,
            createdAt = Instant.now().toString()
        )

        assertFailsWith<IllegalArgumentException> {
            svc.createAdminFromDto(dto)
        }
    }

    @Test
    fun createAdmin_missingEmail_throws() {
        val svc = AdminService()

        val dto = AdminDTO(
            id = null,
            firstName = "NoEmail",
            lastName = "User",
            email = "",
            password = "somepass",
            createdAt = Instant.now().toString()
        )

        assertFailsWith<IllegalArgumentException> {
            svc.createAdminFromDto(dto)
        }
    }
}