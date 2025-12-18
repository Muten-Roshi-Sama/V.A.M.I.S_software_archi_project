package be.ecam.server.models

import be.ecam.server.testutils.TestDatabase
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import java.time.Instant

class TeacherDAOTest : TestDatabase() {
    override val tables: Array<Table> = arrayOf(PersonTable, TeacherTable)

    @BeforeTest
    override fun setupDatabase() {
        super.setupDatabase()
    }

    @Test
    fun createTeacher() {
        transaction {
            val person = Person.new {
                firstName = "Alice"
                lastName = "Dupont"
                email = "alice@test.com"
                password = "pass123"
                createdAt = Instant.now().toString()
            }
            
            val teacher = Teacher.createForPerson(person)
            assertEquals(person.id, teacher.person.id)
            assertEquals("alice@test.com", teacher.email)
        }
    }


    @Test
    fun convertsToDto() {
        transaction {
            val person = Person.new {
                firstName = "Charlie"
                lastName = "Wilson"
                email = "charlie@test.com"
                password = "pass123"
                createdAt = Instant.now().toString()
            }
            
            val teacher = Teacher.createForPerson(person)
            teacher.teacherId = "TCH456"
            
            val dto = teacher.toDto()
            
            assertEquals(teacher.id.value, dto.id)
            assertEquals("TCH456", dto.teacherId)
            assertEquals("Charlie", dto.firstName)
            assertEquals("Wilson", dto.lastName)
            assertEquals("charlie@test.com", dto.email)
            assertNull(dto.password)
        }
    }

    @Test
    //removes Teacher when Person is deleted
    fun cascadeDelete() {
        val teacherId = transaction {
            val person = Person.new {
                firstName = "Diana"
                lastName = "Prince"
                email = "diana@test.com"
                password = "pass123"
                createdAt = Instant.now().toString()
            }
            
            val teacher = Teacher.createForPerson(person)
            val id = teacher.id.value
            
            person.delete()
            
            id
        }
        
        transaction {
            val teacher = Teacher.findById(teacherId)
            assertNull(teacher, "Teacher should be deleted when Person is deleted")
        }
    }

    @Test
    fun accesToPersonData() {
        transaction {
            val person = Person.new {
                firstName = "Eve"
                lastName = "Adams"
                email = "eve@test.com"
                password = "pass123"
                createdAt = Instant.now().toString()
            }
            
            val teacher = Teacher.createForPerson(person)
            
            assertEquals(person.id.value, teacher.personId)
            assertEquals("Eve", teacher.firstName)
            assertEquals("Adams", teacher.lastName)
            assertEquals("eve@test.com", teacher.email)
            assertNotNull(teacher.createdAt)
        }
    }

    @Test
    fun findById() {
        val teacherId = transaction {
            val person = Person.new {
                firstName = "Frank"
                lastName = "Green"
                email = "frank@test.com"
                password = "pass123"
                createdAt = Instant.now().toString()
            }
            
            val teacher = Teacher.createForPerson(person)
            teacher.id.value
        }
        
        transaction {
            val found = Teacher.findById(teacherId)
            assertNotNull(found)
            assertEquals("frank@test.com", found.email)
        }
    }

    @Test
    fun allTeachers_returnsAllRecords() {
        transaction {
            val person1 = Person.new {
                firstName = "Teacher1"
                lastName = "Test"
                email = "teacher1@test.com"
                password = "pass123"
                createdAt = Instant.now().toString()
            }
            Teacher.createForPerson(person1)
            
            val person2 = Person.new {
                firstName = "Teacher2"
                lastName = "Test"
                email = "teacher2@test.com"
                password = "pass123"
                createdAt = Instant.now().toString()
            }
            Teacher.createForPerson(person2)
            
            val allTeachers = Teacher.all()
            assertEquals(2, allTeachers.count())
        }
    }
}
