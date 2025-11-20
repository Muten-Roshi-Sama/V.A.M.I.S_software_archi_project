package be.ecam.server

import be.ecam.server.models.Admin
import be.ecam.server.models.AdminTable
import be.ecam.server.models.AdminTable.username
import io.ktor.server.testing.*
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.*

class ApplicationTest {

    @Test
    fun testRoot() = testApplication {
        application {
            module()
        }


    fun addAdmin(){
        transaction {
//            SchemaUtils.create(AdminTable)
//            println("✅ AdminTable created.")

//            if (Admin.all().empty()) {
                Admin.new {
                    username = "admin789"
                    password = "1234"
                    email = "admin@example.com"
                }
//            }


//        val admin = Admin.find()




        }
        assert(Admin.find{AdminTable.username eq username }.count() == 1L)
    }
//        val response = client.get("/")
//        assertEquals(HttpStatusCode.OK, response.status)
//        assertEquals("Ktor: ${Greeting().greet()}", response.bodyAsText())
    }
}
