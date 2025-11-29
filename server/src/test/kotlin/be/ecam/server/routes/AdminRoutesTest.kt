// server/src/test/kotlin/be/ecam/server/routes/CrudRoutesAddAdminTest.kt
package be.ecam.server.routes

// Tutorial : https://ktor.io/docs/server-testing.html#overview

//Ktor
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.testing.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.routing
import io.ktor.client.call.*

//Kotlin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import io.mockk.every
import io.mockk.mockk

//Admin imports
import be.ecam.server.services.AdminService
import be.ecam.server.services.AdminCreateDTO
import be.ecam.common.api.AdminDTO
import be.ecam.server.routes.handlers.AdminHandler
import be.ecam.server.routes.CrudRegistry

// Other
import be.ecam.server.routes.configureRoutes

class CrudRoutesAddAdminTest {

//    @Test
//    fun testRoot() = testApplication {
//        application {
//            configureRoutes()
//        }
//        val response = client.get('/')
//        assertEquals(HttpStatusCode.Ok, response.status)
////        assertEquals("")
//    }


    @Test
    fun `POST_crud_admins_creates_admin_and_returns_201`() = testApplication {
        val adminService = mockk<AdminService>()
        val returnedDto = AdminDTO(
            id = 1,
            firstName = "Alice",
            lastName = "Smith",
            email = "alice@example.com",
            password = null,
            createdAt = "2025-11-23T00:00:00"
        )
        every { adminService.create(any<AdminCreateDTO>()) } returns returnedDto

        val adminHandler = AdminHandler(adminService)
        val registry = CrudRegistry(mapOf("admins" to adminHandler))

        application {
            install(ContentNegotiation) {
                json()
            }
            routing {
                crudRoutes(registry)
            }
        }

        val createJson = """
            {
              "email": "alice@example.com",
              "password": "s3cret123",
              "firstName": "Alice",
              "lastName": "Smith"
            }
        """.trimIndent()

        val response = client.post("/crud/admins") {
            contentType(ContentType.Application.Json)
            setBody(createJson)
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val bodyText = response.bodyAsText()
        assertTrue(bodyText.contains("alice@example.com"))
    }
}


