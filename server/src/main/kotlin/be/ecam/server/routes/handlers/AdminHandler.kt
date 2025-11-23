// server/src/main/kotlin/be/ecam/server/routes/handlers/AdminHandler.kt
package be.ecam.server.routes.handlers

import be.ecam.server.services.AdminService
import be.ecam.server.services.AdminCreateDTO
import be.ecam.common.api.AdminDTO
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.http.*

/**
 * Concrete Admin handler implementing CrudHandler using AdminService.
 * Inject the AdminService in the constructor (passed from `Routes.kt`).
 */
class AdminHandler(
    private val adminService: AdminService
) : CrudHandler {

    override suspend fun list(call: ApplicationCall) {
        val all: List<AdminDTO> = adminService.getAll()
        call.respond(HttpStatusCode.OK, all)
    }

    override suspend fun search(call: ApplicationCall) {
        val q = call.request.queryParameters["q"]?.lowercase()
        val all = adminService.getAll()
        val filtered = if (!q.isNullOrBlank()) {
            all.filter { dto ->
                listOfNotNull(dto.email?.lowercase(), dto.firstName?.lowercase(), dto.lastName?.lowercase())
                    .any { it.contains(q) }
            }
        } else all
        call.respond(HttpStatusCode.OK, filtered)
    }

    override suspend fun getById(call: ApplicationCall) {
        val idStr = call.parameters["id"]
        val id = idStr?.toIntOrNull()
        if (id == null) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid id"))
            return
        }
        val admin = adminService.getById(id)
        if (admin == null) {
            call.respond(HttpStatusCode.NotFound, mapOf("error" to "not found"))
            return
        }
        call.respond(HttpStatusCode.OK, admin)
    }

    override suspend fun count(call: ApplicationCall) {
        val c = adminService.count()
        call.respond(HttpStatusCode.OK, mapOf("count" to c))
    }

    override suspend fun create(call: ApplicationCall) {
        try {
            val dto = call.receive<AdminCreateDTO>()
            val created: AdminDTO = adminService.create(dto)
            call.respond(HttpStatusCode.Created, created)
        } catch (ex: IllegalArgumentException) {
            // validation or business error (e.g., email exists)
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to (ex.message ?: "invalid input")))
        }
    }

    override suspend fun update(call: ApplicationCall) {
        // AdminService currently has no update(...) method in provided code.
        // Option A: implement an AdminService.update(...) and call it here.
        // Option B: indicate Not Implemented until service supports update.
        call.respond(HttpStatusCode.NotImplemented, mapOf("error" to "update not implemented"))
    }

    override suspend fun delete(call: ApplicationCall) {
        val idStr = call.parameters["id"]
        val id = idStr?.toIntOrNull()
        if (id == null) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid id"))
            return
        }
        val deleted = adminService.delete(id)
        if (!deleted) {
            call.respond(HttpStatusCode.NotFound, mapOf("error" to "not found"))
            return
        }
        call.respond(HttpStatusCode.NoContent)
    }
}