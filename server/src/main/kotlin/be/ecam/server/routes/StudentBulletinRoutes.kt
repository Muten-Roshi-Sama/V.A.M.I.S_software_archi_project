package be.ecam.server.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import be.ecam.server.services.StudentService
import org.jetbrains.exposed.sql.transactions.transaction




fun Route.studentBulletinRoutes() {
    println("Enregistrement de studentBulletinRoutes()...")

    route("/students") {
        println("   → Route /students created")

        get("/all/grades") {
            println("\n🔹 [ROUTE] GET /crud/students/all/grades called")
            try {
                val service = StudentService() //create the service
                val bulletins = service.getAllStudentsWithBulletins() //calls the method that reads the database

                println(" ${bulletins.size} reports retrieved from the database\n")

                call.respond(bulletins) //responds with the data

            } catch (e: Exception) {
                println(" ERREUR: ${e.message}")
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Erreur serveur : ${e.message}")
            }
        }

        authenticate("auth-jwt") {
            get("/grades/me") {
                println("\n🔹 [ROUTE] GET /crud/students/grades/me called")
                try {
                    val principal = call.principal<JWTPrincipal>()
                    if (principal == null) {
                        call.respond(HttpStatusCode.Unauthorized, "Not authenticated")
                        return@get
                    }

                    val userId = principal.payload.getClaim("id").asInt()
                    val service = StudentService()

                    val person = transaction { be.ecam.server.models.Person.findById(userId) }
                    if (person == null) {
                        call.respond(HttpStatusCode.NotFound, "Person not found")
                        return@get
                    }

                    val bulletin = service.getStudentByEmail(person.email)
                    if (bulletin == null) {
                        call.respond(HttpStatusCode.NotFound, "Student grades not found")
                    } else {
                        call.respond(bulletin)
                    }
                } catch (e: Exception) {
                    println(" ERREUR: ${e.message}")
                    e.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError, "Erreur serveur : ${e.message}")
                }
            }
        }

        get("/{email}") {
            val email = call.parameters["email"]
            if (email.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Missing email parameter")
                return@get
            }

            try {
                val service = StudentService()
                val student = service.getStudentByEmail(email)

                if (student == null) {
                    call.respond(HttpStatusCode.NotFound, "Student not found")
                } else {
                    call.respond(student)
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Erreur: ${e.message}")
            }
        }
    }
}