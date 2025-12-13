package be.ecam.server.routes.handlers

import be.ecam.common.api.CountResponse
import be.ecam.common.api.TeacherDTO
import be.ecam.server.routes.InterfaceRoutes
import be.ecam.server.services.TeacherCreateDTO
import be.ecam.server.services.TeacherService
import be.ecam.server.services.TeacherUpdateDTO
import be.ecam.server.auth.withRoles
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class TeacherRoutes(private val service: TeacherService) : InterfaceRoutes {
    override fun registerRoutes(parent: Route) {
        parent.apply {
            withRoles("admin") {
                get {
                    call.respond(HttpStatusCode.OK, service.getAll())
                }
                get("/count") {
                    call.respond(HttpStatusCode.OK, CountResponse(service.count()))
                }
                get("/by/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid ID"))
                    val teacher = service.getById(id)
                        ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Teacher not found"))
                    call.respond(HttpStatusCode.OK, teacher)
                }
                post {
                    val dto = call.receive<TeacherCreateDTO>()
                    try {
                        call.respond(HttpStatusCode.Created, service.create(dto))
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Invalid data")))
                    }
                }
                put("/by/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid ID"))
                    val dto = call.receive<TeacherUpdateDTO>()
                    try {
                        call.respond(HttpStatusCode.OK, service.update(id, dto))
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Invalid data")))
                    }
                }
                delete("/by/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid ID"))
                    val ok = service.delete(id)
                    if (ok) call.respond(HttpStatusCode.OK, mapOf("message" to "Teacher deleted"))
                    else call.respond(HttpStatusCode.NotFound, mapOf("error" to "Teacher not found"))
                }
            }

            withRoles("teacher", "admin") {
                get("/me") {
                    val principal = call.principal<JWTPrincipal>()
                        ?: return@get call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Not authenticated"))
                    val userId = principal.payload.getClaim("id").asInt()
                    val role = principal.payload.getClaim("role").asString()
                    if (role != "teacher") {
                        return@get call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Not a teacher"))
                    }
                    val teacher = service.getByPersonId(userId)
                        ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Teacher profile not found"))
                    call.respond(HttpStatusCode.OK, teacher)
                }

                put("/me") {
                    val principal = call.principal<JWTPrincipal>()
                        ?: return@put call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Not authenticated"))
                    val userId = principal.payload.getClaim("id").asInt()
                    val role = principal.payload.getClaim("role").asString()
                    if (role != "teacher") {
                        return@put call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Not a teacher"))
                    }
                    val incoming = call.receive<TeacherUpdateDTO>()
                    val sanitized = TeacherUpdateDTO(
                        firstName = null,
                        lastName = null,
                        email = null,
                        password = incoming.password, // only password allowed
                        teacherId = null
                    )
                    try {
                        call.respond(HttpStatusCode.OK, service.update(userId, sanitized))
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Invalid data")))
                    }
                }
            }
        }
    }
}