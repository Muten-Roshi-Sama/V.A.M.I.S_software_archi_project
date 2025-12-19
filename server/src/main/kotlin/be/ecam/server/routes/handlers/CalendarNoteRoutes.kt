package be.ecam.server.routes.handlers

import be.ecam.common.api.CalendarNoteCreateRequest
import be.ecam.server.auth.withRoles
import be.ecam.server.routes.InterfaceRoutes
import be.ecam.server.services.CalendarNoteService
import be.ecam.server.services.StudentService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.principal
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post

class CalendarNoteRoutes(
    private val studentService: StudentService,
    private val calendarNoteService: CalendarNoteService,
) : InterfaceRoutes {
    override fun registerRoutes(parent: Route) {
        parent.apply {
            withRoles("student", "admin") {
                // GET /crud/calendar-notes/me?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD
                get("/me") {
                    val principal = call.principal<JWTPrincipal>()
                        ?: return@get call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Not authenticated"))

                    val personId = principal.payload.getClaim("id").asInt()
                    val student = studentService.getByPersonId(personId)
                        ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Student profile not found"))

                    val studentId = student.id
                        ?: return@get call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Student ID is null"))

                    val startDate = call.request.queryParameters["startDate"]
                    val endDate = call.request.queryParameters["endDate"]

                    val notes = calendarNoteService.listForStudent(
                        studentId = studentId,
                        startDate = startDate,
                        endDate = endDate,
                    )

                    call.respond(HttpStatusCode.OK, notes)
                }

                // POST /crud/calendar-notes/me
                post("/me") {
                    val principal = call.principal<JWTPrincipal>()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Not authenticated"))

                    val personId = principal.payload.getClaim("id").asInt()
                    val student = studentService.getByPersonId(personId)
                        ?: return@post call.respond(HttpStatusCode.NotFound, mapOf("error" to "Student profile not found"))

                    val studentId = student.id
                        ?: return@post call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Student ID is null"))

                    val req = call.receive<CalendarNoteCreateRequest>()
                    val created = calendarNoteService.upsertForStudent(studentId, req)

                    call.respond(HttpStatusCode.OK, created)
                }
            }
        }
    }
}
