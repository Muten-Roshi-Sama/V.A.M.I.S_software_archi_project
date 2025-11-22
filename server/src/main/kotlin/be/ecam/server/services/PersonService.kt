package be.ecam.server.services

import be.ecam.server.models.Person
import be.ecam.server.models.PersonTable
import be.ecam.server.util.requireValidEmail
import be.ecam.server.util.requireValidPassword
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.exceptions.ExposedSQLException
import java.time.LocalDateTime

/**
 * Internal DTO for person creation used by role services.
 */
data class PersonCreateDTO(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val createdAt: String? = null
)

/**
 * Centralized Person lifecycle logic:
 * - validation
 * - existence checks
 * - creation (persistence)
 *
 * Role services should call this to create/find persons.
 */
class PersonService {

    fun existsByEmail(email: String): Boolean = transaction {
        Person.find { PersonTable.email eq email }.empty().not()
    }

    fun findById(id: Int): Person? = transaction {
        Person.findById(id)
    }

    fun getAll(): List<Person> = transaction {
        Person.all().toList()
    }

    fun count(): Long = transaction {
        Person.all().count()
    }

    /**
     * Create and persist a Person after validating inputs.
     *
     * NOTE: This code stores the password as provided. Replace the assignment
     * with a hashed password using a Security util (e.g. BCrypt) when ready.
     */
    fun create(createDto: PersonCreateDTO): Person = transaction {
        val emailField = createDto.email
        val passwordField = createDto.password

        // Validate inputs (throw IllegalArgumentException for invalid data)
        requireValidEmail(emailField)
        requireValidPassword(passwordField, minLength = 6)

        if (existsByEmail(emailField)) {
            throw IllegalArgumentException("Email '$emailField' is already registered")
        }

        try {
            val person = Person.new {
                firstName = createDto.firstName
                lastName = createDto.lastName
                email = emailField
                // TODO: hash password before storing; use hashPassword(passwordField)
                password = passwordField
                createdAt = createDto.createdAt ?: LocalDateTime.now().toString()
            }
            person
        } catch (ex: ExposedSQLException) {
            val msg = ex.message ?: ""
            if (msg.contains("UNIQUE", true) || msg.contains("constraint", true)) {
                throw IllegalArgumentException("Email '$emailField' is already registered (unique constraint).")
            }
            throw ex
        }
    }
}