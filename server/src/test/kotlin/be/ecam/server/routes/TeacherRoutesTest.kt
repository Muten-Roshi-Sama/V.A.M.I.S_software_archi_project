package be.ecam.server.routes

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.testApplication
import kotlin.test.*

import be.ecam.server.module

class TeacherRoutesTest {

    @Test
    fun `GET crud teachers - unauthenticated returns 401`() = testApplication {
        application { module() }
        val response = client.get("/crud/teachers")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `GET crud teachers - admin can list all teachers`() = testApplication {
        application { module() }
        val token = loginAdmin(client)

        val response = client.get("/crud/teachers") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains(TestLogin.teacherEmail))
    }

@Test
    fun `GET crud teachers count - admin can count teachers`() = testApplication {
        application { module() }
        val token = loginAdmin(client)

        val response = client.get("/crud/teachers/count") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("count"))
    }

    @Test
    fun `GET crud teachers me - teacher can fetch own profile`() = testApplication {
        application { module() }
        val token = loginTeacher(client)

        val response = client.get("/crud/teachers/me") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(response.bodyAsText().contains(TestLogin.teacherEmail))
        assertTrue(body.contains("teacherId"))
    }

    @Test
    fun `PUT crud teachers me - teacher can update own password only`() = testApplication {
        application { module() }
        val token = loginTeacher(client)

        val response = client.put("/crud/teachers/me") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"password":"newpass123"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)

        // ensure profile still accessible and unchanged fields are present
        val me = client.get("/crud/teachers/me") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, me.status)
        val body = me.bodyAsText()
        assertTrue(response.bodyAsText().contains(TestLogin.teacherEmail))
        assertTrue(body.contains("teacherId"))

        // reset seed user password to avoid breaking subsequent tests
        val reset = client.put("/crud/teachers/me") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"password":"${TestLogin.teacherPassword}"}""")
        }
        assertEquals(HttpStatusCode.OK, reset.status)
    }
    @Test
    fun `full CRUD lifecycle - admin creates, updates, deletes teacher`() = testApplication {
        application { module() }
        val token = loginAdmin(client)
        val testEmail = "test-teacher-${System.currentTimeMillis()}@test.com"
        var createdId: Int? = null

        try {
            // CREATE
            val createResponse = client.post("/crud/teachers") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody("""{"firstName":"Test","lastName":"Teacher","email":"$testEmail","password":"pass123","teacherId":"T9999"}""")
            }
            assertEquals(HttpStatusCode.Created, createResponse.status)
            createdId = extractId(createResponse.bodyAsText())

            // LIST
            val listResponse = client.get("/crud/teachers") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            assertTrue(listResponse.bodyAsText().contains(testEmail))

            // GET by ID
            val getResponse = client.get("/crud/teachers/by/$createdId") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            assertEquals(HttpStatusCode.OK, getResponse.status)
            assertTrue(getResponse.bodyAsText().contains("T9999"))


            // UPDATE
            val updateResponse = client.put("/crud/teachers/by/$createdId") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody("""{"firstName":"Updated","lastName":"Teacher","email":"$testEmail"}""")
            }
            assertEquals(HttpStatusCode.OK, updateResponse.status)

            // DELETE
            val deleteResponse = client.delete("/crud/teachers/by/$createdId") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            assertEquals(HttpStatusCode.OK, deleteResponse.status)

            // Verify deleted
            val get404 = client.get("/crud/teachers/by/$createdId") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            assertEquals(HttpStatusCode.NotFound, get404.status)

        } finally {
            createdId?.let {
                try {
                    client.delete("/crud/teachers/by/$it") {
                        header(HttpHeaders.Authorization, "Bearer $token")
                    }
                } catch (e: Exception) { /* already deleted */ }
            }
        }
    }
}



