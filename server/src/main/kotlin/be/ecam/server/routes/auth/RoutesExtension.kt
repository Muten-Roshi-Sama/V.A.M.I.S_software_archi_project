package be.ecam.server.auth

import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

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
                    throw UnauthorizedException()
                }
                val role = principal.payload.getClaim("role").asString()

                // Admin bypass or check allowed roles
                if (role != "admin" && allowedRoles.isNotEmpty() && role !in allowedRoles) {
                    throw ForbiddenException()
                }
            }
            build()
        }
    }
}