package be.ecam.server

// Database
import be.ecam.server.db.DatabaseFactory

// Routes imports
import be.ecam.server.routes.configureRoutes
import be.ecam.server.routes.configureStaticRoutes

// Ktor
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation



// ====================== Main ==========================


fun main(args: Array<String>) {
    // Start Ktor with configuration from application.conf (HTTP)
    EngineMain.main(args)  // Starts the Ktor web server, also loads Application.module() down here
}

fun Application.module() {
    install(ContentNegotiation) { json() }   // Uses Ktor's Serialization when sending/receiving JSON's

    // ------- Database ---------
    DatabaseFactory.connect()
    DatabaseFactory.initDb()


    // ----------- ROUTES ----------
    configureStaticRoutes() // serves WASM + favicon
    configureRoutes()       // API routes

}



