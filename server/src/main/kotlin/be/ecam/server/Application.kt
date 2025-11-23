package be.ecam.server

// Database
import be.ecam.server.db.DatabaseFactory

// Routes
import be.ecam.server.routes.configureRoutes
import be.ecam.server.routes.configureStaticRoutes

// Ktor
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json

// ====================== Main ==========================

fun main(args: Array<String>) {
    // Start Ktor server using application.conf
    EngineMain.main(args)
}

fun Application.module() {

    // JSON Serialization
    install(ContentNegotiation) { json() }

    // ------- Database ---------
    DatabaseFactory.connect()
    DatabaseFactory.initDb()

    // ----------- ROUTES ----------
    configureStaticRoutes() // static assets, favicon, etc.
    configureRoutes()       // API routes
}
