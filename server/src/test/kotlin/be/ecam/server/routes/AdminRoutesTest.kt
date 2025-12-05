//// server/src/test/kotlin/be/ecam/server/routes/CrudRoutesAddAdminTest.kt
package be.ecam.server.routes
//
//// Tutorial : https://ktor.io/docs/server-testing.html#overview
//package be.ecam.server.routes
//
//import kotlin.test.Test
//import kotlin.test.assertEquals
//
//import io.ktor.server.testing.testApplication
//import io.ktor.client.request.*
//import io.ktor.client.statement.*
//import io.ktor.http.*
//import io.ktor.server.application.*
//import io.ktor.server.routing.*
//
//import io.mockk.every
//import io.mockk.mockk
//
//import be.ecam.server.services.AdminService
//import be.ecam.common.api.AdminDTO
//import be.ecam.server.routes.handlers.AdminRoutes
//import be.ecam.server.ServerPlugins
//
//class AdminRoutesTest {
//
//    @Test
//    fun `GET crud admins returns list`() = testApplication {
//        application {
//            // Install common plugins (ContentNegotiation, etc.)
//            installCommonPlugins()
//
//            // Mock AdminService
//            val adminService = mockk<AdminService>()
//            every { adminService.getAll() } returns listOf(
//                AdminDTO(id = 1, firstName = "Alice", lastName = "Smith", email = "alice@example.com", password = null, createdAt = "2025-01-01")
//            )
//
//            // Build registry with AdminRoutes
//            val adminRoutes = AdminRoutes(adminService)
//            val registry = CrudRegistry(mapOf("admins" to adminRoutes))
//
//            routing {
//                registry.registerAllUnder(this, "/crud")
//            }
//        }
//
//        // Act
//        val response = client.get("/crud/admins")
//
//        // Assert
//        assertEquals(HttpStatusCode.OK, response.status)
//        val body = response.bodyAsText()
//        assert(body.contains("alice@example.com")) { "Response should contain admin email" }
//    }
//}
//
