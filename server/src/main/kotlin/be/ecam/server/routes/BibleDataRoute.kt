package be.ecam.server.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import be.ecam.server.services.BibleService

fun Route.bibleDataRoute() {
    val service = BibleService()

    route("/bible") {
        get {
            try {
                val programs = service.getAllPrograms()
                call.respond(programs)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Erreur: ${e.message}")
            }
        }
    }
}