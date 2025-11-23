package be.ecam.server.services

import be.ecam.server.models.PersonTable
import be.ecam.server.models.AdminTable

//
import be.ecam.server.testutils.TestDatabase

//
import org.jetbrains.exposed.sql.Table
//
import kotlin.test.Test
import kotlin.test.BeforeTest
import kotlin.test.assertNotNull
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith
import java.sql.Connection
import java.sql.DriverManager



class PersonServiceTest : TestDatabase() {
    override val tables: Array<Table> = arrayOf(PersonTable)
    private lateinit var personService: PersonService
    private lateinit var connection: Connection

    @BeforeTest
    override fun setupDatabase() {
        super.setupDatabase()
        personService = PersonService()
    }


    @Test
    fun `create valid person succeeds and existsByEmail returns true`() {
        val dto = PersonCreateDTO(
            firstName = "Alice",
            lastName = "Smith",
            email = "alice@example.com",
            password = "Password123",
            createdAt = null
        )

        val person = personService.create(dto)

        assertNotNull(person.id.value, "Person should have an id after persistence")
        assertEquals("alice@example.com", person.email)
        assertTrue(personService.existsByEmail("alice@example.com"))
    }

    @Test
    fun `creating duplicate email throws IllegalArgumentException`() {
        val dto = PersonCreateDTO(
            firstName = "Bob",
            lastName = "Jones",
            email = "bob@example.com",
            password = "AnotherPass1",
            createdAt = null
        )

        // first create should succeed
        val created = personService.create(dto)
        assertNotNull(created.id.value)

        // second create with same email should fail
        val ex = assertFailsWith<IllegalArgumentException> {
            personService.create(dto)
        }
        assertTrue(ex.message?.contains("already registered", ignoreCase = true) == true)
    }
}