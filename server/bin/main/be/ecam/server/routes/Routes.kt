package be.ecam.server.routes

// SHARED Functions
import be.ecam.common.Greeting
import be.ecam.common.api.*
// Ktor
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
// Java
import java.time.*

fun Application.configureRoutes() {
    routing {

        // ---------- STATIC / ROOT ----------
        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }

        // ---------- API ----------
        route("/api") {

            // --- Hello ---
            get("/hello") {
                call.respond(HelloResponse("Hello from Ktor server"))
            }

            // --- Schedule ---
            get("/schedule") {
                val schedule = mutableMapOf<String, List<ScheduleItem>>()
                schedule["2025-09-30"] = listOf(ScheduleItem("Team sync"), ScheduleItem("Release planning"))
                schedule["2025-10-01"] = listOf(ScheduleItem("Code review"))

                for (month in 1..12) {
                    val first = LocalDate.of(2025, month, 1)
                    schedule.putIfAbsent(first.toString(), listOf(ScheduleItem("Kickoff ${first.month.name.lowercase().replaceFirstChar { it.titlecase() }}")))
                }

                call.respond(schedule)
            }

            // --- Users ---
            route("/users") {
                get {
                    // (later) Return all users
                    call.respondText("List of all users")
                }
                get("/{id}") {
                    // (later) Return single user details
                    call.respondText("Details for user ${call.parameters["id"]}")
                }
            }
        }
    }
}