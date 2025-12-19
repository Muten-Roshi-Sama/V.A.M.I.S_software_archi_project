package be.ecam.server

// Database
import be.ecam.server.db.DatabaseFactory

// Routes imports
import be.ecam.server.routes.configureRoutes
import be.ecam.server.routes.configureStaticRoutes

// JSWT Auth
import be.ecam.server.auth.installJwtAuth

// Ktor
import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain

// === IMPORTS AJOUTÉS POUR LE CORS ===
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
// ====================================

// ====================== Main ==========================

fun main(args: Array<String>) {
    // Start Ktor with configuration from application.conf (HTTP)
    EngineMain.main(args)
}

fun Application.module() {
    // === BLOC CORS AJOUTÉ ICI ===
    // C'est ça qui va permettre à ton site (8081) de parler au serveur (8080)
    install(CORS) {
        allowMethod(HttpMethod.Options) // INDISPENSABLE pour que le navigateur vérifie la sécu
        allowMethod(HttpMethod.Post)    // Pour le login
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        
        // Autorise les infos d'authentification
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.AccessControlAllowOrigin)
        
        // Autorise ton site Web sur le port 8081
        allowHost("localhost:8081")
        allowHost("127.0.0.1:8081")
    }
    // ============================

    // 1. configure Server
    installCommonPlugins()

    // 2. Configure Auth BEFORE routes
    installJwtAuth(
        secret = environment.config.propertyOrNull("jwt.secret")?.getString() ?: "dev-secret",
        issuer = environment.config.propertyOrNull("jwt.issuer")?.getString() ?: "ecam",
        audience = environment.config.propertyOrNull("jwt.audience")?.getString() ?: "ecam-audience"
    )

    // 3. Setup DB
    DatabaseFactory.connect()
    // DatabaseFactory.resetDb() // Attention, ça efface tout !
    DatabaseFactory.initDb()

    // 4. Register Routes
    configureStaticRoutes()
    configureRoutes()
}