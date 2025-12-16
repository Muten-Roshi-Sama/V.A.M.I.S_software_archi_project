package be.ecam.server

import io.ktor.server.application.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.http.*
import kotlinx.serialization.json.Json

fun Application.installCommonPlugins() {
    install(DefaultHeaders)
    install(CallLogging)
    install(ContentNegotiation) {
        json(Json { prettyPrint = false; isLenient = true; ignoreUnknownKeys = true })
    }
    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to (cause.message ?: "bad request")))
        }
        exception<NoSuchElementException> { call, _ ->
            call.respond(HttpStatusCode.NotFound, mapOf("error" to "not found"))
        }
        exception<Throwable> { call, t ->
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (t.message ?: "internal error")))
        }
    }
}