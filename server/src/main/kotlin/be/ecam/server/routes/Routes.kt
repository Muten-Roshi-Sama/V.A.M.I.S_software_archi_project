package be.ecam.server.routes

// Ktor
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
// Java
import java.time.*

// SHARED Functions
import be.ecam.common.Greeting
import be.ecam.common.api.*

// Services
import be.ecam.server.services.PersonService
import be.ecam.server.services.AdminService
import be.ecam.server.services.StudentService
import be.ecam.server.services.TeacherService

// Handlers
import be.ecam.server.routes.handlers.AdminRoutes

// Routes Interface
import be.ecam.server.routes.InterfaceRoutes
import be.ecam.server.routes.CrudRegistry

//
/** ==================================================
    Routes.kt :
    - auth/Security.kt: configure JWT auth plugin and token creation. Called during application startup.
    - auth/AuthHelpers.kt: helper functions to read claims, status-page mapping for auth exceptions.
    - auth/RoutesExtension.kt (with withRoles): protects routes based on role




 ================================================== **/
//
fun Application.configureRoutes(registry: CrudRegistry) {
    routing {
        get("/") { call.respondText("Ktor Status: OK") }
        registry.registerAllUnder(this, "/crud")
    }
}

/** Production entry: create concrete services & registry then delegate. */
fun Application.configureRoutes() {

    // Create services
    val personService = be.ecam.server.services.PersonService()
    val adminService = AdminService()
    val teacherService = TeacherService()
    val studentService = StudentService()


    // Build authenticator and token factory
    val authenticator = be.ecam.server.auth.userAuthenticator(personService, adminService, teacherService, studentService)
    val tokenFactory: (Int, String) -> String = { userId, role ->
        // Security.kt - create stateless token
        be.ecam.server.auth.createToken(secret = "dev-secret", issuer = "ecam", audience = "ecam-audience", userId = userId, role = role)
    }



//    val registry = CrudRegistry(mapOf("admins" to adminRoutes))
//    configureRoutes(registry)



}






//fun Application.configureRoutes() {
//
//    // 1. Create services
//    val adminService = AdminService()          // single instance
////   val studentService = StudentService()
////    val teacherService = TeacherService()
//    // ...
//
//    // 2. Create Handlers (DI)
//    val adminHandler = AdminHandler(adminService)
////    val studentHandler = StudentHandler(StudentService)
////    val teacherHandler = TeacherHandler(TeacherService)
//    // ...
//
//    // 3. Build Registry
//    val registry = CrudRegistry(
//        mapOf(
//            "admins" to adminHandler,
////            "students" to studentHandler,
////            "teachers" to TeacherHandler(teacherService)
//            // ...
//        )
//    )
//
//    routing {
//
//        // ---------- ROOT -------------
//        get("/") { call.respondText("Ktor Status: OK") }
//
//        // ---------- CRUD -------------
//        crudRoutes(registry)
//
//    }
//
//}

