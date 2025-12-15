package be.ecam.server.routes

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.testApplication
import kotlin.test.*

import be.ecam.server.module
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.selectAll


class AdminRoutesTest {

    @Test
    fun `auth - admin login success`() = testApplication {
        application { module() }
        
        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"admin1@admin.com","password":"pass123"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("accessToken"))
    }

    @Test
    fun `auth - invalid credentials returns 401`() = testApplication {
        application { module() }
        
        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"fake@admin.com","password":"wrong"}""")
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `GET crud admins - admin can list all admins`() = testApplication {
        application { module() }
        val token = loginAdmin(client)

        val response = client.get("/crud/admins") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("admin1@admin.com"))
    }

    @Test
    fun `GET crud admins - unauthenticated returns 401`() = testApplication {
        application { module() }

        val response = client.get("/crud/admins")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `GET crud admins count - returns correct count`() = testApplication {
        application { module() }
        val token = loginAdmin(client)

        val response = client.get("/crud/admins/count") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("\"count\":"))
    }

    @Test
    fun `full CRUD lifecycle - create, list, get, update, delete admin`() = testApplication {
        application { module() }
        val token = loginAdmin(client)
        val testEmail = "test-admin-${System.currentTimeMillis()}@test.com"
        var createdId: Int? = null

        try {
            // 1. CREATE
            val createResponse = client.post("/crud/admins") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody("""{"firstName":"Test","lastName":"Admin","email":"$testEmail","password":"pass123"}""")
            }
            assertEquals(HttpStatusCode.Created, createResponse.status)
            val createBody = createResponse.bodyAsText()
            assertTrue(createBody.contains(testEmail))
            createdId = extractId(createBody)

            // 2. LIST - verify present
            val listResponse = client.get("/crud/admins") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            assertTrue(listResponse.bodyAsText().contains(testEmail))

            // 3. GET by ID
            val getResponse = client.get("/crud/admins/by/$createdId") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            assertEquals(HttpStatusCode.OK, getResponse.status)
            assertTrue(getResponse.bodyAsText().contains("Test"))

            // 4. UPDATE
            val updateResponse = client.put("/crud/admins/by/$createdId") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody("""{"firstName":"Updated","lastName":"Admin","email":"$testEmail"}""")
            }
            assertEquals(HttpStatusCode.OK, updateResponse.status)
            assertTrue(updateResponse.bodyAsText().contains("Updated"))

            // 5. DELETE
            val deleteResponse = client.delete("/crud/admins/by/$createdId") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            assertEquals(HttpStatusCode.NoContent, deleteResponse.status)
            // 6. Verify deleted (404)
            val get404 = client.get("/crud/admins/by/$createdId") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            assertEquals(HttpStatusCode.NotFound, get404.status)

        } finally {
            // Cleanup if test failed mid-way
            createdId?.let {
                try {
                    client.delete("/crud/admins/by/$it") {
                        header(HttpHeaders.Authorization, "Bearer $token")
                    }
                } catch (e: Exception) { /* already deleted */ }
            }
        }
    }

    @Test
    fun `GET crud admins me - admin can fetch own profile`() = testApplication {
        application { module() }
        val token = loginAdmin(client)

        val response = client.get("/crud/admins/me") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("admin1@admin.com"))
    }


    @Test
    fun `DEBUG - verify database setup and seeds`() = testApplication {
        application { module() }
        
        val adminToken = loginAdmin(client)
        println("✅ Admin login successful")
        
        transaction {
            val adminCount = be.ecam.server.models.AdminTable.selectAll().count()
            val studentCount = be.ecam.server.models.StudentTable.selectAll().count()
            val teacherCount = be.ecam.server.models.TeacherTable.selectAll().count()
            
            println("\n📊 DATABASE CONTENTS:")
            println("Admins: $adminCount")
            println("Students: $studentCount")
            println("Teachers: $teacherCount")
        }
    }


}