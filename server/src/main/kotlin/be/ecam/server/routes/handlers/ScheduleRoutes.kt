package be.ecam.server.routes.handlers

import be.ecam.common.api.CountResponse
import be.ecam.common.api.ScheduleCreateDTO
import be.ecam.common.api.ScheduleUpdateDTO
import be.ecam.server.services.ScheduleService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.scheduleRoutes() {
    val service = ScheduleService()
    
    get {
        val schedules = service.getAll()
        call.respond(HttpStatusCode.OK, schedules)
    }
    
    get("/count") {
        val count = service.count()
        call.respond(HttpStatusCode.OK, CountResponse(count))
    }
    
    get("/by/{id}") {
        val id = call.parameters["id"]?.toIntOrNull()
            ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid ID")
        
        val schedule = service.getById(id)
            ?: return@get call.respond(HttpStatusCode.NotFound, "Schedule not found")
        
        call.respond(HttpStatusCode.OK, schedule)
    }
    
    post {
        val dto = call.receive<ScheduleCreateDTO>()
        try {
            val created = service.create(dto)
            call.respond(HttpStatusCode.Created, created)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, "Error: ${e.message}")
        }
    }
    
    put("/by/{id}") {
        val id = call.parameters["id"]?.toIntOrNull()
            ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid ID")
        
        val dto = call.receive<ScheduleUpdateDTO>()
        try {
            val updated = service.update(id, dto)
            call.respond(HttpStatusCode.OK, updated)
        } catch (e: IllegalArgumentException) {
            call.respond(HttpStatusCode.BadRequest, "Error: ${e.message}")
        }
    }
    
    delete("/by/{id}") {
        val id = call.parameters["id"]?.toIntOrNull()
            ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid ID")
        
        val success = service.delete(id)
        if (success) {
            call.respond(HttpStatusCode.OK, mapOf("message" to "Schedule deleted"))
        } else {
            call.respond(HttpStatusCode.NotFound, "Schedule not found")
        }
    }
    
    get("/range") {
        val startDate = call.request.queryParameters["startDate"] ?: return@get call.respond(
            HttpStatusCode.BadRequest, 
            "Missing startDate parameter"
        )
        val endDate = call.request.queryParameters["endDate"] ?: return@get call.respond(
            HttpStatusCode.BadRequest, 
            "Missing endDate parameter"
        )
        
        try {
            val schedules = service.getByDateRange(startDate, endDate)
            call.respond(schedules)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
        }
    }
    
    get("/search") {
        val activityName = call.request.queryParameters["activity"] ?: return@get call.respond(
            HttpStatusCode.BadRequest, 
            "Missing activity parameter"
        )
        
        try {
            val schedules = service.getByActivityName(activityName)
            call.respond(schedules)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
        }
    }
    
    get("/year/{year}") {
        val studyYear = call.parameters["year"] ?: return@get call.respond(
            HttpStatusCode.BadRequest,
            "Missing year parameter"
        )
        
        try {
            val schedules = service.getByStudyYear(studyYear)
            call.respond(schedules)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
        }
    }
    
    get("/teacher/{name}") {
        val teacherName = call.parameters["name"] ?: return@get call.respond(
            HttpStatusCode.BadRequest,
            "Missing name parameter"
        )
        
        try {
            val schedules = service.getByTeacher(teacherName)
            call.respond(schedules)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
        }
    }
}
