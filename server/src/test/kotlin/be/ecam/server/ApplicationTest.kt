package be.ecam.server

// Ktor
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.testing.*

// Kotlin
import kotlin.test.*

// Other
import be.ecam.server.routes.configureRoutes


class ApplicationTest {
    @Test
    fun testRoot() = testApplication {
        application {
            configureRoutes()
        }
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Ktor Status: OK", response.bodyAsText())
    }
}