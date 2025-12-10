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
import be.ecam.server.routes.auth.AuthRoutes
import be.ecam.server.routes.handlers.AdminRoutes
import be.ecam.server.routes.handlers.StudentRoutes
// import be.ecam.server.routes.handlers.TeacherRoutes


// Auth helpers
import be.ecam.server.auth.userAuthenticator
import be.ecam.server.auth.createToken

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

/** for Test
 *
 */
fun Application.configureRoutes(
    registry: CrudRegistry,
    authRoutes: AuthRoutes? = null
    ) {
    routing {
        // Root health check
        get("/") { call.respondText("Ktor Status: OK") }

        // Health routes
        healthRoutes(
            isDevFeatureEnabled = true,
            devResetHandler = {
                // Optional: trigger DB reset in dev
                be.ecam.server.db.DatabaseFactory.resetDb()
            }
        )

        // Auth routes (login, /me)
        authRoutes?.register(this)

        // CRUD resources
        registry.registerAllUnder(this, "/crud")
    }
}

/**
 * for Production:
 * create services, build registry, wire routes.
 * */
fun Application.configureRoutes() {

    // 1. Create services
    val personService = PersonService()
    val adminService = AdminService()
    val teacherService = TeacherService()
    val studentService = StudentService()


    // 2. Build authenticator and token factory
    val authenticator = userAuthenticator(
        personService = personService,
        adminService = adminService,
        teacherService = teacherService,
        studentService = studentService
    )
    val tokenFactory: (Int, String) -> String = { userId, role ->
        createToken(
            secret = "dev-secret",
            issuer = "ecam",
            audience = "ecam-audience",
            userId = userId,
            role = role
        )
    }

    // 3. Create AuthRoutes
    val authRoutes = AuthRoutes(authenticator, tokenFactory)

    // 4. Create resource handlers
    val adminRoutes = AdminRoutes(adminService)
    val studentRoutes = StudentRoutes(studentService)
    // val teacherRoutes = TeacherRoutes(teacherService)

    // 5. Build registry
    val registry = CrudRegistry(
        mapOf(
            "admins" to adminRoutes,
            "students" to studentRoutes
            // "teachers" to teacherRoutes
        )
    )

    // 6. Wire everything
    configureRoutes(registry, authRoutes)

}





