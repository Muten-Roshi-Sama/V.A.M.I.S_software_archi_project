package be.ecam.server.auth

import io.ktor.server.routing.*
import io.ktor.server.application.*

/**
 * Wrap a route block in JWT authentication and enforce allowed roles.
 * Admins are allowed by default if you want a superuser bypass (adjust as needed).
 */
fun Route.withRoles(vararg allowedRoles: String, build: Route.() -> Unit) {
    authenticate("auth-jwt") {
        route("") {
            intercept(io.ktor.server.application.ApplicationCallPipeline.Call) {
                val role = call.userRole() ?: throw UnauthorizedException()
                if (role != "admin" && allowedRoles.isNotEmpty() && role !in allowedRoles) {
                    throw ForbiddenException()
                }
            }
            build()
        }
    }
}