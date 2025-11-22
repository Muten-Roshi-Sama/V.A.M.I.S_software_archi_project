package be.ecam.server


//DAO
import be.ecam.server.db.DatabaseFactory
import be.ecam.server.models.*

// Services / DTOs
import be.ecam.server.services.AdminService
import be.ecam.common.api.AdminDTO

//Kotlin
import io.ktor.server.testing.*
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.*
import kotlin.test.assertEquals
import java.time.Instant


// ==============================================================

class ApplicationTest {

    @Test
    fun addOneAdmin() {
        // Connect to the same sqlite file used by the server (creates data dir if missing)
        DatabaseFactory.connect()

        // Keep DB operations local to a single transaction for test determinism
        transaction {
            // ASSERT PersonTable and AdminTable exist

            // Create a Person and corresponding Admin
            val pw = "123456"
            val em = "admin75875@example.com"
            val person = Person.new {
                firstName = "Admin"
                lastName = "User"
                email = em
                password = pw
                createdAt = Instant.now().toString()
            }
            Admin.createForPerson(person)

            // Assertions inside transaction to avoid visibility issues
            assertEquals(1L, Person.find { PersonTable.email eq em }.count(), "Expected one person")
//            assertEquals(1L, Admin.all().count(), "Expected one admin")
        }
    }


    @Test
    fun createAdmin_missingPassword_throws() {
        val svc = AdminService()
        // Build an AdminDTO with no password (password is nullable on DTO)
        val dto = AdminDTO(
            firstName = "NoPw",
            lastName = "User",
            email = "nopw@example.com",
            password = null,
            createdAt = Instant.now().toString()
        )

        // createAdminFromDto requires a password — expect throwing IllegalArgumentException
        assertFailsWith<IllegalArgumentException> {
            svc.createAdminFromDto(dto)
        }
    }


}
