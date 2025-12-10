package be.ecam.server.routes.handlers

import be.ecam.common.api.CountResponse
import be.ecam.common.api.StudentDTO
import be.ecam.server.routes.InterfaceRoutes
import be.ecam.server.services.StudentCreateDTO
import be.ecam.server.services.StudentService
import be.ecam.server.services.StudentUpdateDTO
import be.ecam.server.auth.withRoles
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class StudentRoutes(private val service: StudentService) : InterfaceRoutes {
    override fun registerRoutes(parent: Route) {
        parent.apply {
            
            // ========== ADMIN-ONLY ENDPOINTS ==========
            withRoles("admin") {
                // GET /crud/students - List all students (admin only)
                get {
                    val students = service.getAll()
                    call.respond(HttpStatusCode.OK, students)
                }

                // GET /crud/students/count - Count all students (admin only)
                get("/count") {
                    val count = service.count()
                    call.respond(HttpStatusCode.OK, CountResponse(count))
                }

                // GET /crud/students/by/{id} - Get any student by ID (admin only)
                get("/by/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid ID"))
                    
                    val student = service.getById(id)
                        ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Student not found"))
                    
                    call.respond(HttpStatusCode.OK, student)
                }

                // POST /crud/students - Create new student (admin only)
                post {
                    val createDto = call.receive<StudentCreateDTO>()
                    try {
                        val created = service.create(createDto)
                        call.respond(HttpStatusCode.Created, created)
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Invalid data")))
                    }
                }

                // PUT /crud/students/by/{id} - Update any student (admin only)
                put("/by/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid ID"))
                    
                    val updateDto = call.receive<StudentUpdateDTO>()
                    try {
                        val updated = service.update(id, updateDto)
                        call.respond(HttpStatusCode.OK, updated)
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Invalid data")))
                    }
                }

                // DELETE /crud/students/by/{id} - Delete any student (admin only)
                delete("/by/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid ID"))
                    
                    val success = service.delete(id)
                    if (success) {
                        call.respond(HttpStatusCode.OK, mapOf("message" to "Student deleted"))
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Student not found"))
                    }
                }
            } // end admin-only

            // ========== STUDENT SELF-SERVICE ENDPOINTS ==========
            withRoles("student", "admin") {
                
                // GET /crud/student/me - Get own profile
                get("/me") {
                    val principal = call.principal<JWTPrincipal>()
                        ?: return@get call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Not authenticated"))

                    val userId = principal.payload.getClaim("id").asInt()
                    val role = principal.payload.getClaim("role").asString()

                    if (role != "student") {
                        return@get call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Not a student"))
                    }

                    val student = service.getByPersonId(userId)
                        ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Student profile not found"))

                    call.respond(HttpStatusCode.OK, student)  // StudentDTO includes studentId
                }

                // PUT /crud/student/me - Update own profile (limited fields)
                put("/me") {
                    val principal = call.principal<JWTPrincipal>()
                        ?: return@put call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Not authenticated"))
                    
                    val userId = principal.payload.getClaim("id").asInt()
                    val role = principal.payload.getClaim("role").asString()
                    
                    if (role == "student") {
                        val updateDto = call.receive<StudentUpdateDTO>()
                        
                        // Students can only update their own email/password, NOT studentId/year/option
                        val sanitizedDto = StudentUpdateDTO(
                            firstName = updateDto.firstName,
                            lastName = updateDto.lastName,
                            email = updateDto.email,
                            password = updateDto.password,
                            studentId = null,      // Prevent students from changing these
                            studyYear = null,
                            optionCode = null
                        )
                        
                        try {
                            val updated = service.update(userId, sanitizedDto)
                            call.respond(HttpStatusCode.OK, updated)
                        } catch (e: IllegalArgumentException) {
                            call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Invalid data")))
                        }
                    } else {
                        call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Access denied"))
                    }
                }
            } // end student self-service
        }
    }
}