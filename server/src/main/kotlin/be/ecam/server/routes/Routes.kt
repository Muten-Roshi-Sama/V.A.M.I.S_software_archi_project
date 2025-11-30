package be.ecam.server.routes

import be.ecam.common.Greeting
import be.ecam.common.api.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File
import java.time.*

fun Application.configureRoutes() {
    routing {
        get("/") { call.respondText("Ktor: ${Greeting().greet()}") }
        get("/health") { call.respond(mapOf("status" to "OK", "timestamp" to LocalDate.now().toString())) }

        route("/api") {
            get("/hello") { call.respond(HelloResponse("Hello from Ktor server")) }
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
            route("/users") {
                get { call.respondText("List of all users") }
                get("/{id}") { call.respondText("Details for user ${call.parameters["id"]}") }
            }
        }

        route("/auth") { }

        route("/crud") {
            route("/admins") {
                get {
                    val admins = be.ecam.server.services.AdminService().getAll()
                    call.respond(admins)
                }
            }

            // Expose /crud/courses (serve the courses.json file)
            route("/courses") {
                get {
                    val possiblePaths = listOf(
                        "server/src/main/resources/data/courses.json",
                        "src/main/resources/data/courses.json",
                        "data/courses.json"
                    )
                    val file = possiblePaths.map(::File).firstOrNull { it.exists() }
                        ?: return@get call.respond(HttpStatusCode.NotFound, "courses.json untraceable")
                    call.respondText(file.readText(), ContentType.Application.Json)
                }
            }


            //When the frontend calls this URL → the server returns the contents of the JSON file :
            route("/students") {
                get("/all/grades") {
                    val possiblePaths = listOf(
                        "server/src/main/resources/data/students.json",
                        "src/main/resources/data/students.json",
                        "data/students.json"
                    )
                    val file = possiblePaths.map(::File).firstOrNull { it.exists() }
                        ?: return@get call.respond(HttpStatusCode.NotFound, "students.json untraceable")

                    call.respondText(file.readText(), ContentType.Application.Json)
                }
            }

            route("/teachers") { }
        }

        route("/db") { }
    }
}