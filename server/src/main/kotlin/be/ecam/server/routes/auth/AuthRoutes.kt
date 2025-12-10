
package be.ecam.server.routes.auth

import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import kotlinx.serialization.Serializable
//
import be.ecam.common.api.UserInfo

// DTOs for login
@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class LoginResponse(val accessToken: String, val tokenType: String = "Bearer")

/**
 * AuthRoutes:
 * - validate credentials by :
 *                      1. person.existByEmail()
 *                      2. check if password is correct
 *                      3. retrieve person role
 *
 *
 * `authenticator(email,password)` should return Pair(userId, role) (or null if invalid).
 * - `tokenFactory(userId, role)` returns a signed JWT string.
 *
 * This keeps route logic independent of authentication implementation.
 */
class AuthRoutes(
    private val authenticator: suspend (String, String) -> Pair<Int, String>?,
    private val tokenFactory: (Int, String) -> String
) {

    fun register(parent: Route) {
        parent.route("/auth") {
            // POST /auth/login
            post("/login") {
                val req = call.receive<LoginRequest>()
                val auth = authenticator(req.email.trim(), req.password)
                if (auth == null) {
                    return@post call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "invalid credentials"))
                }
                val (userId, role) = auth
                val token = tokenFactory(userId, role)
                call.respond(HttpStatusCode.OK, LoginResponse(accessToken = token))
            }

            authenticate("auth-jwt") {
                get("/me") {
                    val principal = call.authentication.principal<io.ktor.server.auth.jwt.JWTPrincipal>()
                    if (principal == null) {
                        return@get call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "unauthenticated"))
                    }
                    val id = principal.payload.getClaim("id").asInt()
                    val role = principal.payload.getClaim("role").asString()
                    call.respond(HttpStatusCode.OK, be.ecam.common.api.UserInfo(id = id, role = role))
                }
            }
        }
    }
}















//fun Route.registerApiRoutes() {
//
//    // Root route for API
//    get("/") {
//        call.respondText("Ktor: ${Greeting().greet()}")
//    }
//
//    // /api/hello endpoint
//    get("/hello") {
//        call.respond(HelloResponse(message = "Hello from Ktor server"))
//    }
//
//    // /api/schedule endpoint
//    get("/schedule") {
//        val schedule = mutableMapOf<String, List<ScheduleItem>>()
//
//        // A couple of fixed examples around current timeframe
//        schedule["2025-09-30"] = listOf(ScheduleItem("Team sync"), ScheduleItem("Release planning"))
//        schedule["2025-10-01"] = listOf(ScheduleItem("Code review"))
//
//        // Add examples for each remaining month of 2025
//        for (month in 1..12) {
//            val first = LocalDate.of(2025, month, 1)
//            val mid = first.withDayOfMonth(minOf(15, first.lengthOfMonth()))
//            val last = first.withDayOfMonth(first.lengthOfMonth())
//
//            schedule.putIfAbsent(first.toString(), listOf(ScheduleItem("Kickoff ${first.month.name.lowercase().replaceFirstChar { it.titlecase() }}")))
//            schedule.putIfAbsent(mid.toString(), listOf(ScheduleItem("Mid-month check"), ScheduleItem("Demo prep")))
//            schedule.putIfAbsent(last.toString(), listOf(ScheduleItem("Retrospective")))
//        }
//
//        // Weekly examples on Mondays in Q4 2025
//        var d = LocalDate.of(2025, 10, 1)
//        while (!d.isAfter(LocalDate.of(2025, 12, 31))) {
//            if (d.dayOfWeek == DayOfWeek.MONDAY) {
//                schedule.putIfAbsent(d.toString(), listOf(ScheduleItem("Weekly planning")))
//            }
//            d = d.plusDays(1)
//        }
//
//        call.respond(schedule)
//    }
//}
