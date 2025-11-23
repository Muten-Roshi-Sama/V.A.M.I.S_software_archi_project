package be.ecam.server.routes

import be.ecam.server.routes.handlers.CrudRegistry
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route


fun Route.crudRoutes(registry: CrudRegistry) {
    route("/crud") {
        route("/{table}") {
            // list & search
            get {
                val table = call.parameters["table"]?.lowercase()
                val handler = table?.let { registry.getHandler(it) }
                if (handler == null) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "unknown table"))
                    return@get
                }
                // if query param q present -> search, else list
                val q = call.request.queryParameters["q"]
                if (!q.isNullOrBlank()) handler.search(call) else handler.list(call)
            }

            get("/by/{id}") {
                val table = call.parameters["table"]?.lowercase()
                val handler = table?.let { registry.getHandler(it) }
                if (handler == null) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "unknown table"))
                    return@get
                }
                handler.getById(call)
            }

            get("/count") {
                val table = call.parameters["table"]?.lowercase()
                val handler = table?.let { registry.getHandler(it) }
                if (handler == null) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "unknown table"))
                    return@get
                }
                handler.count(call)
            }

            post {
                val table = call.parameters["table"]?.lowercase()
                val handler = table?.let { registry.getHandler(it) }
                if (handler == null) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "unknown table"))
                    return@post
                }
                handler.create(call)
            }

            put("/by/{id}") {
                val table = call.parameters["table"]?.lowercase()
                val handler = table?.let { registry.getHandler(it) }
                if (handler == null) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "unknown table"))
                    return@put
                }
                handler.update(call)
            }

            delete("/by/{id}") {
                val table = call.parameters["table"]?.lowercase()
                val handler = table?.let { registry.getHandler(it) }
                if (handler == null) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "unknown table"))
                    return@delete
                }
                handler.delete(call)
            }
        }
    }
}
