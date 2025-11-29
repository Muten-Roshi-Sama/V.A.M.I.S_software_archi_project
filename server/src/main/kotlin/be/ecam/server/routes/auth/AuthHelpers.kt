package be.ecam.server.auth

import io.ktor.server.application.*
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.server.plugins.statuspages.*

class UnauthorizedException : RuntimeException()
class ForbiddenException : RuntimeException()

fun Application.installAuthStatusPages() {
    install(StatusPages) {
        exception<UnauthorizedException> { call, _ -> call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "unauthorized")) }
        exception<ForbiddenException> { call, _ -> call.respond(HttpStatusCode.Forbidden, mapOf("error" to "forbidden")) }
    }
}

fun ApplicationCall.userRole(): String? =
    this.authentication.principal<JWTPrincipal>()?.payload?.getClaim("role")?.asString()