package be.ecam.server.auth

import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

import io.ktor.http.*
import io.ktor.server.response.*

/**
 * Wrap a route block in JWT authentication and enforce allowed roles.
 * Admins bypass role restrictions by default.
 */
fun Route.withRoles(vararg allowedRoles: String, build: Route.() -> Unit) {
    authenticate("auth-jwt") {
        route("") {
            intercept(ApplicationCallPipeline.Call) {
                // Extract role from validated JWT principal
                val principal = call.principal<JWTPrincipal>()
                if (principal == null) {
                    // No token / invalid token -> 401
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "unauthorized"))
                    finish()
                    return@intercept
                }
                val role = principal.payload.getClaim("role").asString()

                // Admin bypass or check allowed roles
                if (role != "admin" && allowedRoles.isNotEmpty() && role !in allowedRoles) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to "forbidden"))
                    finish()
                    return@intercept
                }
            }
            build()
        }
    }
}


