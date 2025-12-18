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

class StudentDAOTest : TestDatabase() {
    override val tables: Array<Table> = arrayOf(PersonTable, StudentTable)

    @BeforeTest
    override fun setupDatabase() {
        super.setupDatabase()
    }

    @Test
    fun createStudent() {
        transaction {
            val person = Person.new {
                firstName = "Alice"
                lastName = "Dupont"
                email = "alice@student.school.com"
                password = "pass123"
                createdAt = Instant.now().toString()
            }
            
            val student = Student.createForPerson(person)
            
            assertNotNull(student.id)
            assertEquals(person.id, student.person.id)
            assertEquals("alice@student.school.com", student.email)
        }
    }


    @Test
    fun convertsToDto() {
        transaction {
            val person = Person.new {
                firstName = "Charlie"
                lastName = "Wilson"
                email = "charlie@student.com"
                password = "pass123"
                createdAt = Instant.now().toString()
            }
            
            val student = Student.createForPerson(person)
            student.studentId = "123456"
            student.studyYear = "MA2"
            student.optionCode = "ELEC"
            
            val dto = student.toDto()
            
            assertEquals(student.id.value, dto.id)
            assertEquals("123456", dto.studentId)
            assertEquals("MA2", dto.studyYear)
            assertEquals("ELEC", dto.optionCode)
            assertEquals("Charlie", dto.firstName)
            assertEquals("Wilson", dto.lastName)
            assertEquals("charlie@student.com", dto.email)
            assertNull(dto.password) //no password exposed into DTO
        }
    }

    @Test
    fun cascadeDelete() {
        val studentId = transaction {
            val person = Person.new {
                firstName = "Diana"
                lastName = "Prince"
                email = "diana@student.com"
                password = "pass123"
                createdAt = Instant.now().toString()
            }
            
            val student = Student.createForPerson(person)
            val id = student.id.value
            
            person.delete()
            
            id //return the id of this student --> studentId = id
        }
        
        
        transaction {
            val student = Student.findById(studentId)
            assertNull(student, "Student should be deleted when Person is deleted")
        }
    }

    @Test
    fun accessToPersonData() {
        transaction {
            val person = Person.new {
                firstName = "Eve"
                lastName = "Adams"
                email = "eve@test.com"
                password = "pass123"
                createdAt = Instant.now().toString()
            }
            
            val student = Student.createForPerson(person)
            
            assertEquals(person.id.value, student.personId)
            assertEquals("Eve", student.firstName)
            assertEquals("Adams", student.lastName)
            assertEquals("eve@test.com", student.email)
            assertNotNull(student.createdAt)
        }
    }

    @Test
    fun findStudentById() {
        val studentId = transaction {
            val person = Person.new {
                firstName = "Frank"
                lastName = "Green"
                email = "frank@test.com"
                password = "pass123"
                createdAt = Instant.now().toString()
            }
            
            val student = Student.createForPerson(person)
            student.id.value
        }
        
        transaction {
            val found = Student.findById(studentId)
            assertNotNull(found)
            assertEquals("frank@test.com", found.email)
        }
    }

    @Test
    fun allStudents_returnsAllRecords() {
        transaction {
            val person1 = Person.new {
                firstName = "Student1"
                lastName = "Test"
                email = "student1@test.com"
                password = "pass123"
                createdAt = Instant.now().toString()
            }
            Student.createForPerson(person1)
            
            val person2 = Person.new {
                firstName = "Student2"
                lastName = "Test"
                email = "student2@test.com"
                password = "pass123"
                createdAt = Instant.now().toString()
            }
            Student.createForPerson(person2)
            
            val allStudents = Student.all()
            assertEquals(2, allStudents.count())
        }
    }
}