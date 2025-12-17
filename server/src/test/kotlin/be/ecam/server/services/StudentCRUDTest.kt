package be.ecam.server.models

// DAO
import be.ecam.server.models.PersonTable
import be.ecam.server.models.StudentTable

// Services / DTOs
import be.ecam.server.services.StudentService
import be.ecam.server.services.StudentCreateDTO
import be.ecam.server.services.StudentUpdateDTO
import be.ecam.common.api.StudentDTO
import be.ecam.server.services.PersonService

// Kotlin / testing
import org.jetbrains.exposed.sql.Table
import kotlin.test.BeforeTest
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.test.assertNull
import java.time.Instant

// Test database utils (for test files only)
import be.ecam.server.testutils.TestDatabase

class StudentCRUDTest : TestDatabase() {
    override val tables: Array<Table> = arrayOf(PersonTable, StudentTable)

    private lateinit var studentService: StudentService

    @BeforeTest
    override fun setupDatabase() {
        super.setupDatabase()
        studentService = StudentService()
    }

    @Test
    fun addStudent_directDbFlow() {
        transaction {
            val person = Person.new {
                firstName = "Alice"
                lastName = "Dupont"
                email = "alice@student.school.com"
                password = "226213"
                createdAt = Instant.now().toString()
            }
            Student.createForPerson(person)

            assertEquals(1L, Person.find { PersonTable.email eq "alice@student.school.com" }.count(), "Expected one person")
            assertEquals(1L, Student.all().count(), "Expected one student")
        }
    }

    @Test
    fun createStudent_missingPassword_throws() {
        val svc = StudentService()

        val dto = StudentDTO(
            id = null,
            firstName = "Noah",
            lastName = "Alain",
            email = "Noah@student.school.com",
            password = null,
            createdAt = Instant.now().toString()
        )

        assertFailsWith<IllegalArgumentException> {
            svc.createStudentFromDto(dto)
        }
    }

    @Test
    fun createStudent_missingEmail_throws() {
        val svc = StudentService()

        val dto = StudentDTO(
            id = null,
            firstName = "Jean",
            lastName = "Dupont",
            email = "",
            password = "password",
            createdAt = Instant.now().toString()
        )

        assertFailsWith<IllegalArgumentException> {
            svc.create(StudentCreateDTO(
                firstName = dto.firstName,
                lastName = dto.lastName,
                email = dto.email,
                password = dto.password ?: "default"
            ))
        }
    }

    @Test
    fun getAll_returnsAllStudents() {
        transaction {
            val person1 = Person.new {
                firstName = "Bob"
                lastName = "Martin"
                email = "bob@student.school.com"
                password = "243456"
                createdAt = Instant.now().toString()
            }
            Student.createForPerson(person1)

            val person2 = Person.new {
                firstName = "Charlie"
                lastName = "Wilson"
                email = "charlie@student.school.com"
                password = "243456"
                createdAt = Instant.now().toString()
            }
            Student.createForPerson(person2)

            assertEquals(2L, Student.all().count(), "Expected two students")
        }

        val students = studentService.getAll()
        assertEquals(2, students.size, "Expected 2 students from service")
    }

    @Test
    fun getById_returnsCorrectStudent() {
        val createdDto = transaction {
            val person = Person.new {
                firstName = "Diana"
                lastName = "Prince"
                email = "diana@student.school.com"
                password = "123456"
                createdAt = Instant.now().toString()
            }
            val student = Student.createForPerson(person)
            student.toDto()
        }

        val retrieved = studentService.getById(createdDto.id!!)
        assertEquals(createdDto.email, retrieved?.email, "Retrieved student should match created one")
    }

    @Test
    fun getById_nonexistent_returnsNull() {
        val retrieved = studentService.getById(9999)
        assertNull(retrieved, "Should return null for non-existent student")
    }

    @Test
    fun count_returnsCorrectNumber() {
        transaction {
            val person = Person.new {
                firstName = "Eve"
                lastName = "Adams"
                email = "eve@student.school.com"
                password = "123456"
                createdAt = Instant.now().toString()
            }
            Student.createForPerson(person)
        }

        val count = studentService.count()
        assertEquals(1L, count, "Expected count to be 1")
    }

    @Test
    fun update_modifiesStudentData() {
        val createdDto = transaction {
            val person = Person.new {
                firstName = "Frank"
                lastName = "Green"
                email = "frank@student.school.com"
                password = "123456"
                createdAt = Instant.now().toString()
            }
            val student = Student.createForPerson(person)
            student.studentId = "STU123"
            student.studyYear = "BA1"
            student.toDto()
        }

        val updateDto = StudentUpdateDTO(
            firstName = "Franklin",
            lastName = null,
            email = null,
            password = null,
            studentId = "STU124",
            studyYear = "BA2",
            optionCode = "INFO"
        )

        val updated = studentService.update(createdDto.id!!, updateDto)
        assertEquals("Franklin", updated.firstName, "First name should be updated")
        assertEquals("STU124", updated.studentId, "Student ID should be updated")
        assertEquals("BA2", updated.studyYear, "Study year should be updated")
        assertEquals("INFO", updated.optionCode, "Option code should be updated")
    }

    @Test
    fun delete_removesStudent() {
        val createdDto = transaction {
            val person = Person.new {
                firstName = "Grace"
                lastName = "Harris"
                email = "grace@student.school.com"
                password = "123456"
                createdAt = Instant.now().toString()
            }
            val student = Student.createForPerson(person)
            student.toDto()
        }

        val deleted = studentService.delete(createdDto.id!!)
        assertTrue(deleted, "Delete should return true for successful deletion")

        val retrieved = studentService.getById(createdDto.id!!)
        assertNull(retrieved, "Deleted student should not be retrievable")
    }

    @Test
    fun getByPersonId_returnsCorrectStudent() {
        val personId = transaction {
            val person = Person.new {
                firstName = "Henry"
                lastName = "Johnson"
                email = "henry@student.school.com"
                password = "123456"
                createdAt = Instant.now().toString()
            }
            val student = Student.createForPerson(person)
            person.id.value
        }

        val student = studentService.getByPersonId(personId)
        assertEquals("henry@student.school.com", student?.email, "Should retrieve correct student by person ID")
    }

    @Test
    fun existsByEmail_returnsTrueForExistingEmail() {
        transaction {
            val person = Person.new {
                firstName = "Ivy"
                lastName = "King"
                email = "ivy@student.school.com"
                password = "123456"
                createdAt = Instant.now().toString()
            }
            Student.createForPerson(person)
        }

        val exists = studentService.existsByEmail("ivy@student.school.com")
        assertTrue(exists, "Should return true for existing email")
    }

    @Test
    fun existsByEmail_returnsFalseForNonexistentEmail() {
        val exists = studentService.existsByEmail("nonexistent@student.school.com")
        assertEquals(false, exists, "Should return false for non-existent email")
    }
}