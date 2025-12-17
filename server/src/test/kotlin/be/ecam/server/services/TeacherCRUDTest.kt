package be.ecam.server.models

// DAO
import be.ecam.server.models.PersonTable
import be.ecam.server.models.TeacherTable

// Services / DTOs
import be.ecam.server.services.TeacherService
import be.ecam.server.services.TeacherCreateDTO
import be.ecam.server.services.TeacherUpdateDTO
import be.ecam.common.api.TeacherDTO
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

class TeacherCRUDTest : TestDatabase() {
    override val tables: Array<Table> = arrayOf(PersonTable, TeacherTable)

    private lateinit var teacherService: TeacherService

    @BeforeTest
    override fun setupDatabase() {
        super.setupDatabase()
        teacherService = TeacherService()
    }

    @Test
    fun addTeacher_directDbFlow() {
        transaction {
            val person = Person.new {
                firstName = "Alice"
                lastName = "Dupont"
                email = "alice@teacher.school.com"
                password = "226213"
                createdAt = Instant.now().toString()
            }
            Teacher.createForPerson(person)

            assertEquals(1L, Person.find { PersonTable.email eq "alice@teacher.school.com" }.count(), "Expected one person")
            assertEquals(1L, Teacher.all().count(), "Expected one teacher")
        }
    }

    @Test
    fun createTeacher_missingPassword_throws() {
        val svc = TeacherService()

        val dto = TeacherDTO(
            id = null,
            firstName = "Noah",
            lastName = "Alain",
            email = "Noah@teacher.school.com",
            password = null,
            createdAt = Instant.now().toString()
        )

        assertFailsWith<IllegalArgumentException> {
            svc.create(TeacherCreateDTO(
                firstName = dto.firstName,
                lastName = dto.lastName,
                email = dto.email,
                password = dto.password ?: throw IllegalArgumentException("password required")
            ))
        }
    }

    @Test
    fun createTeacher_missingEmail_throws() {
        val svc = TeacherService()

        val dto = TeacherDTO(
            id = null,
            firstName = "Jean",
            lastName = "Dupont",
            email = "",
            password = "password",
            createdAt = Instant.now().toString()
        )

        assertFailsWith<IllegalArgumentException> {
            svc.create(TeacherCreateDTO(
                firstName = dto.firstName,
                lastName = dto.lastName,
                email = dto.email,
                password = dto.password ?: "default"
            ))
        }
    }

    @Test
    fun getAll_returnsAllTeachers() {
        transaction {
            val person1 = Person.new {
                firstName = "Bob"
                lastName = "Martin"
                email = "bob@teacher.school.com"
                password = "243456"
                createdAt = Instant.now().toString()
            }
            Teacher.createForPerson(person1)

            val person2 = Person.new {
                firstName = "Charlie"
                lastName = "Wilson"
                email = "charlie@teacher.school.com"
                password = "243456"
                createdAt = Instant.now().toString()
            }
            Teacher.createForPerson(person2)

            assertEquals(2L, Teacher.all().count(), "Expected two teachers")
        }

        val teachers = teacherService.getAll()
        assertEquals(2, teachers.size, "Expected 2 teachers from service")
    }

    @Test
    fun getById_returnsCorrectTeacher() {
        val createdDto = transaction {
            val person = Person.new {
                firstName = "Diana"
                lastName = "Prince"
                email = "diana@teacher.school.com"
                password = "123456"
                createdAt = Instant.now().toString()
            }
            val teacher = Teacher.createForPerson(person)
            teacher.toDto()
        }

        val retrieved = teacherService.getById(createdDto.id!!)
        assertEquals(createdDto.email, retrieved?.email, "Retrieved teacher should match created one")
    }

    @Test
    fun getById_nonexistent_returnsNull() {
        val retrieved = teacherService.getById(9999)
        assertNull(retrieved, "Should return null for non-existent teacher")
    }

    @Test
    fun count_returnsCorrectNumber() {
        transaction {
            val person = Person.new {
                firstName = "Eve"
                lastName = "Adams"
                email = "eve@teacher.school.com"
                password = "123456"
                createdAt = Instant.now().toString()
            }
            Teacher.createForPerson(person)
        }

        val count = teacherService.count()
        assertEquals(1L, count, "Expected count to be 1")
    }

    @Test
    fun update_modifiesTeacherData() {
        val createdDto = transaction {
            val person = Person.new {
                firstName = "Frank"
                lastName = "Green"
                email = "frank@teacher.school.com"
                password = "123456"
                createdAt = Instant.now().toString()
            }
            val teacher = Teacher.createForPerson(person)
            teacher.teacherId = "TCH123"
            teacher.toDto()
        }

        val updateDto = TeacherUpdateDTO(
            firstName = "Franklin",
            lastName = null,
            email = null,
            password = null,
            teacherId = "TCH124"
        )

        val updated = teacherService.update(createdDto.id!!, updateDto)
        assertEquals("Franklin", updated.firstName, "First name should be updated")
        assertEquals("TCH124", updated.teacherId, "Teacher ID should be updated")
    }

    @Test
    fun delete_removesTeacher() {
        val createdDto = transaction {
            val person = Person.new {
                firstName = "Grace"
                lastName = "Harris"
                email = "grace@teacher.school.com"
                password = "123456"
                createdAt = Instant.now().toString()
            }
            val teacher = Teacher.createForPerson(person)
            teacher.toDto()
        }

        val deleted = teacherService.delete(createdDto.id!!)
        assertTrue(deleted, "Delete should return true for successful deletion")

        val retrieved = teacherService.getById(createdDto.id!!)
        assertNull(retrieved, "Deleted teacher should not be retrievable")
    }

    @Test
    fun getByPersonId_returnsCorrectTeacher() {
        val personId = transaction {
            val person = Person.new {
                firstName = "Henry"
                lastName = "Johnson"
                email = "henry@teacher.school.com"
                password = "123456"
                createdAt = Instant.now().toString()
            }
            val teacher = Teacher.createForPerson(person)
            person.id.value
        }

        val teacher = teacherService.getByPersonId(personId)
        assertEquals("henry@teacher.school.com", teacher?.email, "Should retrieve correct teacher by person ID")
    }

    @Test
    fun existsByEmail_returnsTrueForExistingEmail() {
        transaction {
            val person = Person.new {
                firstName = "Ivy"
                lastName = "King"
                email = "ivy@teacher.school.com"
                password = "123456"
                createdAt = Instant.now().toString()
            }
            Teacher.createForPerson(person)
        }

        val exists = teacherService.existsByEmail("ivy@teacher.school.com")
        assertTrue(exists, "Should return true for existing email")
    }

    @Test
    fun existsByEmail_returnsFalseForNonexistentEmail() {
        val exists = teacherService.existsByEmail("nonexistent@teacher.school.com")
        assertEquals(false, exists, "Should return false for non-existent email")
    }
}