package be.ecam.server.services

import be.ecam.server.models.Person
import be.ecam.server.models.PersonTable
import be.ecam.server.util.requireValidEmail
import be.ecam.server.util.requireValidPassword
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.exceptions.ExposedSQLException
import java.time.LocalDateTime

/**
 * Internal DTO for person creation used by role services.
 */
@Serializable
data class PersonCreateDTO(
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String,
    val password: String,
    val createdAt: String? = null
)
@Serializable
data class PersonUpdateDTO(
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val password: String? = null
)
/**
 * Centralized Person (admin, student, teacher) creation:
 * - validation (pswd length > 6, email should contain "@",...)
 * - existence checks
 * - creation (persistence)
 *
 * All Role services should call this to create/find persons.
 */
class PersonService {

    // BOOL
    fun existsByEmail(email: String): Boolean = transaction {
        Person.find { PersonTable.email eq email }.empty().not()
    }

    // Returns PERSON
    fun findByEmail(email: String): Person? = transaction {
        Person.find { PersonTable.email eq email }.firstOrNull()
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
                firstName = createDto.firstName ?: ""
                lastName = createDto.lastName ?: ""
                email = emailField
                // TODO: hash password before storing; use hashPassword(passwordField)
                password = passwordField
//                createdAt = createDto.createdAt ?: LocalDateTime.now().toString()    // let the clientDefault fill it.
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

    fun update(personId: Int, dto: PersonUpdateDTO): Person = transaction {
        val person = Person.findById(personId) ?: throw IllegalArgumentException("person not found")
        // Email
        dto.email?.let { newEmail ->
            if (newEmail != person.email) {
                requireValidEmail(newEmail)
                val existing = findByEmail(newEmail)
                if (existing != null && existing.id.value != person.id.value) {
                    throw IllegalArgumentException("Email '$newEmail' is already registered")
                }
                person.email = newEmail
            }
        }
        // Names
        dto.firstName?.let { person.firstName = it }
        dto.lastName?.let { person.lastName = it }

        // Password (optional update)
        dto.password?.let { pw ->
            requireValidPassword(pw, minLength = 6)
            person.password = pw // TODO: use hashing
        }

        person
    }


}
