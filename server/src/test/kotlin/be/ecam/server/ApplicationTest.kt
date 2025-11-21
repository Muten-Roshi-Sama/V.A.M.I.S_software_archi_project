package be.ecam.server


//DAO
import be.ecam.server.db.DatabaseFactory
import be.ecam.server.models.Admin
import be.ecam.server.models.AdminTable
import be.ecam.server.models.Person
import be.ecam.server.models.PersonTable

//Kotlin
import io.ktor.server.testing.*
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.*
import kotlin.test.assertEquals


// ==============================================================

class ApplicationTest {
//    @Test
//    fun testRoot() = testApplication {
//        application {
//            module()
//        }

    @Test
    fun addAdmin_directDbFlow() {
        // Connect to the same sqlite file used by the server (creates data dir if missing)
        DatabaseFactory.connect()

        // Keep DB operations local to a single transaction for test determinism
        transaction {
            // Ensure required tables exist for the test (no-op if already present)
            SchemaUtils.create(PersonTable, AdminTable)

            // Create a Person and corresponding Admin
            val person = Person.new {
                firstName = "Admin"
                lastName = "User"
                email = "admin@example.com"
                password = "1234"
            }
            Admin.createForPerson(person)

            // Assertions inside transaction to avoid visibility issues
            assertEquals(1L, Person.find { PersonTable.email eq "admin@example.com" }.count(), "Expected one person")
            assertEquals(1L, Admin.all().count(), "Expected one admin")
        }
    }



//    @Test
//    fun testRootAndAdminCreate() = testApplication {
//        // Start the application (this will call DatabaseFactory.connect()/resetDb() as in Application.module)
//        application {
//            module()
//        }
//
//        // Ensure tables exist for this test run and perform a small create/assert cycle.
//        transaction {
//            // create tables used by the test (no-op if already present)
//            SchemaUtils.create(PersonTable, AdminTable)
//
//            // create a Person and an Admin that references it
//            val person = Person.new {
//                firstName = "Admin"
//                lastName = "User"
//                email = "admin@example.com"
//                password = "1234"
//            }
//            Admin.createForPerson(person)
//
//            // Assertions inside the same transaction
//            assertEquals(
//                1L,
//                Person.find { PersonTable.email eq "admin@example.com" }.count(),
//                "Expected one person with the test email"
//            )
//            assertEquals(1L, Admin.all().count(), "Expected exactly one admin")
//        }
//
//        // Optional: verify HTTP root is responding
//        val response = client.get("/")
//        assertEquals("Ktor: ${Greeting().greet()}", response.bodyAsText())
//    }



}




//        fun addAdmin() {
//            transaction {
//                // create a Person then an Admin that references it
//                val person = Person.new {
//                    firstName = "Admin"
//                    lastName = "User"
//                    email = "admin@example.com"
//                    password = "1234"
//                }
//                Admin.createForPerson(person)
//            }
//
//            // assert there is exactly one admin with the email used above
//            assert(Admin.find { PersonTable.email eq "admin@example.com" }.count() == 1L)
//        }
//        val response = client.get("/")
//        assertEquals(HttpStatusCode.OK, response.status)
//        assertEquals("Ktor: ${Greeting().greet()}", response.bodyAsText())


