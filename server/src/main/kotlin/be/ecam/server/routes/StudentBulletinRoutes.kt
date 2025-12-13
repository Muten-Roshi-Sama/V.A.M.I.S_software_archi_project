package be.ecam.server.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import be.ecam.server.services.StudentService


fun Route.studentBulletinRoutes() {
    println("Enregistrement de studentBulletinRoutes()...")

    route("/students") {
        println("   → Route /students created")
        get("/all/grades") {
            println("\n🔹 [ROUTE] GET /crud/students/all/grades called")
            try {
                val service = StudentService() //create the service
                val bulletins = service.getAllStudents() //calls the method that reads the database

                println(" ${bulletins.size} reports retrieved from the database\n")

                call.respond(bulletins) //responds with the data

            } catch (e: Exception) {
                println(" ERREUR: ${e.message}")
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Erreur serveur : ${e.message}")
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