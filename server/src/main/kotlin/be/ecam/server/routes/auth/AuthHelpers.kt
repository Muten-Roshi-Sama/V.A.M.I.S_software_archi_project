package be.ecam.server.auth

import io.ktor.server.application.*
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.response.*
import io.ktor.http.*
//import io.ktor.server.plugins.statuspages.*

//User Services
import be.ecam.server.services.AdminService
import be.ecam.server.services.PersonService
import be.ecam.server.services.RoleService
import be.ecam.server.services.StudentService
import be.ecam.server.services.TeacherService

class UnauthorizedException : RuntimeException()
class ForbiddenException : RuntimeException()


/**
 * Default authenticator:
 * - 1. checks PersonService.existsByEmail()
 * - 2. checks password (TO.DO: hash password)
 * - 3. find role by checking AdminService.existsByEmail, then TeacherService/StudentService if available
 * - fallback: email domain (...@student.com)
 *
 * Returns Pair(userId, role) or null if invalid.
 */
fun userAuthenticator(
    personService: PersonService,
    adminService: AdminService?,
    teacherService: TeacherService? = null,
    studentService: StudentService? = null
): suspend (String, String) -> Pair<Int, String>? = { email, password ->
    // quick existence check
    if (!personService.existsByEmail(email)) {
        null
    } else {
        // fetch the person to verify password
        val person = personService.findByEmail(email)
        if (person == null) {
            null
        } else {
            val passwordMatches = person.verifyPasswordPlain(password) // uses Person.verifyPasswordPlain
            if (!passwordMatches) {
                null
            } else {
                // Determine role by table lookup (preferred)
                val roleFromTables: String? = when {
                    adminService?.existsByEmail(email) == true -> "admin"
                    studentService?.existsByEmail(email) == true -> "student"
                    teacherService?.existsByEmail(email) == true -> "teacher"
                    
                    else -> null
                }
                val role = roleFromTables ?: run {
                    // Fallback heuristic: domain mapping
                    val domain = email.substringAfter("@", "")
                    when {
                        domain.contains("student") -> "student"
                        domain.contains("teacher") -> "teacher"
                        domain.contains("admin") -> "admin"
                        else -> "user" // generic fallback role
                    }
                }
                Pair(person.id.value, role)
            }
        }
    }
}



//fun Application.installAuthStatusPages() {
//    install(StatusPages) {
//        exception<UnauthorizedException> { call, _ -> call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "unauthorized")) }
//        exception<ForbiddenException> { call, _ -> call.respond(HttpStatusCode.Forbidden, mapOf("error" to "forbidden")) }
//    }
//}

//fun ApplicationCall.userRole(): String? =
//    this.authentication.principal<JWTPrincipal>()?.payload?.getClaim("role")?.asString()