package be.ecam.server.routes

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.testApplication
import kotlin.test.*

import be.ecam.server.module
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.selectAll
import be.ecam.server.models.StudentTable

class StudentRoutesTest {

    @Test
    fun `GET crud students - admin can list all students`() = testApplication {
        application { module() }
        val token = loginAdmin(client)
    
        val response = client.get("/crud/students") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("alice@student.com"))
    }
    
    @Test
    fun `GET crud students me - student can fetch own profile`() = testApplication {
        application { module() }
        val token = loginStudent(client)
    
        val response = client.get("/crud/students/me") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("alice@student.com"))
        assertTrue(body.contains("studentId"))
    }
    
    @Test
    fun `PUT crud students me - student can update own password only`() = testApplication {
        application { module() }
        val token = loginStudent(client)
    
        val response = client.put("/crud/students/me") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"password":"newpass123"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }


    @Test
    fun `full CRUD lifecycle - admin creates, updates, deletes student`() = testApplication {
        application { module() }
        val token = loginAdmin(client)
        val testEmail = "test-student-${System.currentTimeMillis()}@test.com"
        var createdId: Int? = null

        try {
            // CREATE
            val createResponse = client.post("/crud/students") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody("""{"firstName":"Test","lastName":"Student","email":"$testEmail","password":"pass123","studentId":"99999","studyYear":"BA1"}""")
            }
            assertEquals(HttpStatusCode.Created, createResponse.status)
            createdId = extractId(createResponse.bodyAsText())

            // LIST
            val listResponse = client.get("/crud/students") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            assertTrue(listResponse.bodyAsText().contains(testEmail))

            // GET by ID
            val getResponse = client.get("/crud/students/by/$createdId") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            assertEquals(HttpStatusCode.OK, getResponse.status)
            assertTrue(getResponse.bodyAsText().contains("99999"))

            // UPDATE
            val updateResponse = client.put("/crud/students/by/$createdId") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody("""{"firstName":"Updated","lastName":"Student","email":"$testEmail","studyYear":"BA2"}""")
            }
            assertEquals(HttpStatusCode.OK, updateResponse.status)

            // DELETE
            val deleteResponse = client.delete("/crud/students/by/$createdId") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            assertEquals(HttpStatusCode.OK, deleteResponse.status)

            // Verify deleted
            val get404 = client.get("/crud/students/by/$createdId") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            assertEquals(HttpStatusCode.NotFound, get404.status)

        } finally {
            createdId?.let {
                try {
                    client.delete("/crud/students/by/$it") {
                        header(HttpHeaders.Authorization, "Bearer $token")
                    }
                } catch (e: Exception) { /* already deleted */ }
            }
        }
    }


        @Test
    fun `DEBUG - verify student seed exists`() = testApplication {
        application { module() }
        
        transaction {
            val count = StudentTable.selectAll().count()
            val students = StudentTable.selectAll().map { it[StudentTable.studentId] }
            println("\n📊 STUDENTS IN DB: $count")
            println("Student IDs: $students")
        }
    }



}