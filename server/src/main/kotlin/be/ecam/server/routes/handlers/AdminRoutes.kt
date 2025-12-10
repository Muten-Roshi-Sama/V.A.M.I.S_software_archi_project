package be.ecam.server.routes.handlers

import be.ecam.server.services.AdminService
import be.ecam.server.services.AdminCreateDTO
import be.ecam.server.services.AdminUpdateDTO
import be.ecam.common.api.AdminDTO

// Ktor
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.*

// Routes
import be.ecam.server.routes.InterfaceRoutes

// JWT route helper
import be.ecam.server.auth.withRoles

class AdminRoutes(private val adminService: AdminService) : InterfaceRoutes {

    override fun registerRoutes(parent: Route) {
        parent.apply {
            // PROTECTED : all admin endpoints
            withRoles("admin") {
                // LIST all
                get {
                    val all = adminService.getAll()
                    call.respond(HttpStatusCode.OK, all)
                    // TODO: implement pagination and filter search directly inside Person
                    // val q = call.request.queryParameters["q"]
                    // val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 0
                    // val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 50

                    // // Prefer service-level search/pagination; fallback is okay for small data/tests
                    // val list: List<AdminDTO> = try {
                    //     adminService.search(q, page, size)
                    // } catch (e: NoSuchMethodError) {
                    //     val all = adminService.getAll()
                    //     val filtered = if (!q.isNullOrBlank()) {
                    //         all.filter { dto ->
                    //             listOfNotNull(dto.email?.lowercase(), dto.firstName?.lowercase(), dto.lastName?.lowercase())
                    //                 .any { it.contains(q.lowercase()) }
                    //         }
                    //     } else all
                    //     val from = page * size
                    //     if (from >= filtered.size) emptyList() else filtered.subList(from, (from + size).coerceAtMost(filtered.size))
                    // }
                    // call.respond(HttpStatusCode.OK, list)
                }

                // Get by Id
                get("/by/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid id"))
                    val admin = adminService.getById(id) ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "not found"))
                    call.respond(HttpStatusCode.OK, admin)
                }

                // GET /crud/admins/count
                get("/count") {
                    val c = adminService.count()
                    call.respond(HttpStatusCode.OK, mapOf("count" to c))
                }

                // GET /crud/admins/me - Get authenticated admin's own profile
                get("/me") {
                    val principal = call.principal<io.ktor.server.auth.jwt.JWTPrincipal>()
                        ?: return@get call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Not authenticated"))
                    
                    val userId = principal.payload.getClaim("id").asInt()
                    val role = principal.payload.getClaim("role").asString()
                    
                    if (role != "admin") {
                        return@get call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Not an admin"))
                    }
                    
                    val admin = adminService.getById(userId)
                        ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Admin profile not found"))
                    
                    call.respond(HttpStatusCode.OK, admin)
                }

                // CREATE
                post {
                    try {
                        val dto = call.receive<AdminCreateDTO>()
                        val created = adminService.create(dto)
                        call.response.headers.append(HttpHeaders.Location, "/crud/admins/${created.id}")
                        call.respond(HttpStatusCode.Created, created)
                    } catch (ex: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to (ex.message ?: "invalid input")))
                    }
                }

                // UPDATE
                put("/by/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid id"))

                    try {
                        val dto = call.receive<AdminUpdateDTO>()
                        val updated = adminService.update(id, dto)
                        call.respond(HttpStatusCode.OK, updated)
                    } catch (ex: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to (ex.message ?: "invalid input")))
                    } catch (ex: Exception) {
                        // Generic fallback (log if you have logger)
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (ex.message ?: "internal error")))
                    }
                }

                // DELETE
                delete("/by/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull() ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid id"))
                    val deleted = adminService.delete(id)
                    if (!deleted) return@delete call.respond(HttpStatusCode.NotFound, mapOf("error" to "not found"))
                    call.respond(HttpStatusCode.NoContent)
                }
            } // end withRoles
        }
    }
}