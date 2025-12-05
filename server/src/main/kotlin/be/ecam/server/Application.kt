package be.ecam.server

// Database
import be.ecam.server.db.DatabaseFactory

// Routes imports
import be.ecam.server.routes.configureRoutes
import be.ecam.server.routes.configureStaticRoutes

// JSWT Auth
import be.ecam.server.auth.installJwtAuth

// Ktor
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.application.install
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation



// ====================== Main ==========================


fun main(args: Array<String>) {
    // Start Ktor with configuration from application.conf (HTTP)
    EngineMain.main(args)  // Starts the Ktor web server, also loads Application.module() down here
}

fun Application.module() {
    // 1. configure Server
    installCommonPlugins()  // ContentNegotiation, StatusPages, CallLogging, etc.
//    install(ContentNegotiation) { json() }   // Uses Ktor's Serialization when sending/receiving JSON's

    // 2. Configure Auth BEFORE routes
    installJwtAuth(
        secret = environment.config.propertyOrNull("jwt.secret")?.getString() ?: "dev-secret",
        issuer = environment.config.propertyOrNull("jwt.issuer")?.getString() ?: "ecam",
        audience = environment.config.propertyOrNull("jwt.audience")?.getString() ?: "ecam-audience"
    )

    // 3. Setup DB
    DatabaseFactory.connect()
//    DatabaseFactory.resetDb()


    // 4. Register Routes
    configureStaticRoutes() // serves WASM + favicon
    configureRoutes()       // API routes


}

// ===========================================================================




