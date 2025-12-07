package be.ecam.server.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import be.ecam.server.services.TeacherService

fun Route.teacherDataRoute() {
    val service = TeacherService()

    route("/teachers") {
        get { // GET /crud/teachers
            try {
                val teachers = service.getAllTeachers()
                call.respond(teachers)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Erreur: ${e.message}")
            }
        }

        get("/{email}") { // GET /crud/teachers/{email}
            val email = call.parameters["email"]
            if (email.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Missing email")
                return@get
            }
            val teacher = service.getTeacherByEmail(email)
            if (teacher == null) {
                call.respond(HttpStatusCode.NotFound, "Teacher not found")
            } else {
                call.respond(teacher)
            }
        }

        // GET /crud/teachers/search?name=Dupont
        get("/search") {
            val nameQuery = call.request.queryParameters["name"]
            if (nameQuery.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Missing query parameter 'name'")
                return@get
            }
            try {
                val results = service.findTeachersByName(nameQuery)
                call.respond(results)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Erreur: ${e.message}")
            }
        }
    }
}