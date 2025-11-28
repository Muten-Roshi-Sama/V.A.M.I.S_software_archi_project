//// server/src/test/kotlin/be/ecam/server/routes/CrudRoutesAddAdminTest.kt
package be.ecam.server.routes
//
//import io.ktor.client.request.*
//import io.ktor.client.statement.*
//import io.ktor.client.call.*
//import io.ktor.http.*
//import io.ktor.serialization.kotlinx.json.*
//import io.ktor.server.plugins.contentnegotiation.*
//import io.ktor.server.testing.*
//import kotlin.test.Test
//import kotlin.test.assertEquals
//import kotlin.test.assertTrue
//import io.mockk.every
//import io.mockk.mockk
//import be.ecam.server.services.AdminService
//import be.ecam.server.services.AdminCreateDTO
//import be.ecam.common.api.AdminDTO
//import be.ecam.server.routes.handlers.AdminHandler
//import be.ecam.server.routes.CrudRegistry
//
//class CrudRoutesAddAdminTest {
//
//    @Test
//    fun `POST _crud_admins creates admin and returns 201`() = testApplication {
//        // Arrange: mock AdminService to return a known AdminDTO when create(...) is called
//        val adminService = mockk<AdminService>()
//        val returnedDto = AdminDTO(
//            id = 1,
//            firstName = "Alice",
//            lastName = "Smith",
//            email = "alice@example.com",
//            password = null,
//            createdAt = "2025-11-23T00:00:00"
//        )
//        every { adminService.create(any<AdminCreateDTO>()) } returns returnedDto
//
//        // Build handler & registry
//        val adminHandler = AdminHandler(adminService)
//        val registry = CrudRegistry(mapOf("admins" to adminHandler))
//
//        // Install JSON and mount routing
//        application {
//            install(ContentNegotiation) {
//                json()
//            }
//            routing {
//                crudRoutes(registry)
//            }
//        }
//
//        // Act: perform POST
//        val createJson = """
//            {
//              "email": "alice@example.com",
//              "password": "s3cret123",
//              "firstName": "Alice",
//              "lastName": "Smith"
//            }
//        """.trimIndent()
//
//        val response = client.post("/crud/admins") {
//            contentType(ContentType.Application.Json)
//            setBody(createJson)
//        }
//
//        // Assert
//        assertEquals(HttpStatusCode.Created, response.status)
//        val bodyText = response.bodyAsText()
//        assertTrue(bodyText.contains("alice@example.com"), "Response body should contain the created admin email")
//    }
//}