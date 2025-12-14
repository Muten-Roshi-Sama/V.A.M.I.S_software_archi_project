package be.ecam.server.routes


// Ktor
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route



fun Route.healthRoutes(isDevFeatureEnabled: Boolean, devResetHandler: (() -> Unit)?) {
    get("/health") { call.respond(mapOf("status" to "ok")) }
    get("/ready") { call.respond(mapOf("ready" to true)) }
    if (isDevFeatureEnabled && devResetHandler != null) {
        post("/dev/db/reset") {
            devResetHandler.invoke()
            call.respond(mapOf("status" to "db reset (dev)"))
        }
    }
}