package be.ecam.server


// Kotlin / testing
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import java.time.Instant
import java.io.File
import io.ktor.server.testing.*
import kotlin.test.*

class ApplicationTest {

    @Test
    fun testRoot() = testApplication {
        application {
            module()
        }
//        val response = client.get("/")
//        assertEquals(HttpStatusCode.OK, response.status)
//        assertEquals("Ktor: ${Greeting().greet()}", response.bodyAsText())
    }
}