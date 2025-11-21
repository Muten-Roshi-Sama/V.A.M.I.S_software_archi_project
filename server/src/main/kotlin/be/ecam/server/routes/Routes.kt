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



// Path Logic :
    // Check tests/test_collections_api.py for implementations and examples
    // -----------Api-----------------------------------
    // Init db            (POST)   :   /api/init_db
    // Clean db           (POST)   :   /api/clean_db
    // ----------CRUDs---------------------------------
    // List all	          (GET)    :   /crud/{collection}
    // Get by id	      (GET)    :   /crud/tracks/by/{id_or_key}
    // Count              (GET)    :   /crud/count
    // Filter/search	  (POST)   :   /crud/tracks?genre=Jazz&q=love     (same function as List all...)
    // Create	          (POST)   :   /crud/tracks
    // Update	          (POST)   :   /crud/tracks/{track_id}
    // Delete	          (POST)   :   /crud/tracks/{track_id}





fun Application.configureRoutes() {
    routing {

        // ---------- STATIC / ROOT : Health Check ----------
        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }
        get("/health") {
            call.respond(mapOf("status" to "OK", "timestamp" to LocalDate.now().toString()))
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
                    schedule.putIfAbsent(
                        first.toString(),
                        listOf(
                            ScheduleItem(
                                "Kickoff ${
                                    first.month.name.lowercase().replaceFirstChar { it.titlecase() }
                                }"
                            )
                        )
                    )
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


        // ---- AUTHENTICATION & AUTHORIZATION -----
        route("/auth") {
            // Login
//            post("/login") {
//                // TODO: Implement login logic
//                call.respondText("Login endpoint - TODO", status = HttpStatusCode.NotImplemented)
//            }
//
//            // Logout
//            post("/logout") {
//                // TODO: Implement logout logic
//                call.respondText("Logout endpoint - TODO", status = HttpStatusCode.NotImplemented)
//            }
//
//            // Verify token
//            get("/verify") {
//                // TODO: Implement token verification
//                call.respondText("Verify endpoint - TODO", status = HttpStatusCode.NotImplemented)
//            }
//
//            // Refresh token
//            post("/refresh") {
//                // TODO: Implement token refresh
//                call.respondText("Refresh endpoint - TODO", status = HttpStatusCode.NotImplemented)
//            }
        }


        // --------- CRUD ---------
        route("/crud") {
            // -------------------- ADMINS --------------------
            route("/admins") {
                // List all admins
                get {
                    val admins = be.ecam.server.services.AdminService().getAll()
                    call.respond(admins)
                }

                // Get admin by ID
                get("/{id}") {
                    //                    val id = call.parameters["id"]?.toIntOrNull()
                    //                    if (id == null) {
                    //                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid ID"))
                    //                        return@get
                    //                    }
                    //                    val admin = AdminService().getById(id)
                    //                    if (admin != null) {
                    //                        call.respond(admin)
                    //                    } else {
                    //                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Admin not found"))
                    //                    }
                }

                // Create new admin
                post {
                    //                    val dto = call.receive<AdminDTO>()
                    //                    val created = AdminService().create(dto)
                    //                    call.respond(HttpStatusCode.Created, created)
                }

                // Update admin
                put("/{id}") {
                    //                    // TODO: Implement update logic
                    //                    call.respondText("Update admin - TODO", status = HttpStatusCode.NotImplemented)
                }

                // Delete admin
                delete("/{id}") {
                    //                    val id = call.parameters["id"]?.toIntOrNull()
                    //                    if (id == null) {
                    //                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid ID"))
                    //                        return@delete
                    //                    }
                    //                    val deleted = AdminService().delete(id)
                    //                    if (deleted) {
                    //                        call.respond(HttpStatusCode.OK, mapOf("message" to "Admin deleted"))
                    //                    } else {
                    //                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Admin not found"))
                    //                    }
                }

                // Count admins
                get("/count") {
                    //                    val count = AdminService().getAll().size
                    //                    call.respond(mapOf("count" to count))
                }
            }



            route("/students") {

            }
            route("/teachers") {

            }


        }


        // -------- DATABASE MANAGEMENT (Admin Only) -----
        route("/db") {
            // Initialize database with seed data
//            post("/init") {
//                call.respondText("Initialize DB - TODO", status = HttpStatusCode.NotImplemented)
//            }
//
//            // Clean/reset database
//            post("/clean") {
//                call.respondText("Clean DB - TODO", status = HttpStatusCode.NotImplemented)
//            }
//
//            // Database statistics
//            get("/stats") {
//                call.respondText("DB Stats - TODO", status = HttpStatusCode.NotImplemented)
//            }
        }
    }
}