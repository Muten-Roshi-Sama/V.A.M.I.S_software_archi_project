package be.ecam.server

// Ktor
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.testing.*
import io.ktor.server.routing.*
import io.ktor.server.response.*

// Kotlin
import kotlin.test.*

// Other
import be.ecam.server.routes.configureRoutes


class ApplicationTest {
    @Test
    fun testRoot() = testApplication {
        application {

            installCommonPlugins()

//            configureRoutes()
            // Register the root route (GET /)
            routing {
                get("/") {
                    call.respondText("Ktor Status: OK")
                }
            }
        }

        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Ktor Status: OK", response.bodyAsText())
    }
}