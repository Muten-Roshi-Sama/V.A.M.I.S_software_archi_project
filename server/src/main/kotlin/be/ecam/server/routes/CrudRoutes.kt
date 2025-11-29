package be.ecam.server.routes


// Crud
import be.ecam.server.routes.handlers.CrudHandler
//import be.ecam.server.routes.handlers.CrudRegistry

// Ktor
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route


class CrudRegistry(private val handlers: Map<String, CrudHandler>) {
    fun getHandler(tableName: String?): CrudHandler? = tableName?.lowercase()?.let { handlers[it] }
}

fun Route.crudRoutes(registry: CrudRegistry) {
    route("/crud") {
        route("/{table}") {
            // LIST
            get { val handler = registry.getHandler(call.parameters["table"]); handler?.list(call) }
            // CREATE
            post { val handler = registry.getHandler(call.parameters["table"]); handler?.create(call) }
            // ... put/get/delete delegates similarly
        }
    }
}

//fun Route.crudRoutes(registry: CrudRegistry) {
//    route("/crud") {
//        route("/{table}") {
//
//            // SEARCH
//            get {
//                val table = call.parameters["table"]?.lowercase()
//                val handler = table?.let { registry.getHandler(it) }
//                if (handler == null) {
//                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "unknown table"))
//                    return@get
//                }
//                // if query param q present -> search, else list
//                val q = call.request.queryParameters["q"]
//                if (!q.isNullOrBlank()) handler.search(call) else handler.list(call)
//            }
//
//            // BY_ID
//            get("/by/{id}") {
//                val table = call.parameters["table"]?.lowercase()
//                val handler = table?.let { registry.getHandler(it) }
//                if (handler == null) {
//                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "unknown table"))
//                    return@get
//                }
//                handler.getById(call)
//            }
//
//            // COUNT
//            get("/count") {
//                val table = call.parameters["table"]?.lowercase()
//                val handler = table?.let { registry.getHandler(it) }
//                if (handler == null) {
//                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "unknown table"))
//                    return@get
//                }
//                handler.count(call)
//            }
//
//            // CREATE
//            post {
//                val table = call.parameters["table"]?.lowercase()
//                val handler = table?.let { registry.getHandler(it) }
//                if (handler == null) {
//                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "unknown table"))
//                    return@post
//                }
//                handler.create(call)
//            }
//
//            // UPDATE
//            put("/by/{id}") {
//                val table = call.parameters["table"]?.lowercase()
//                val handler = table?.let { registry.getHandler(it) }
//                if (handler == null) {
//                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "unknown table"))
//                    return@put
//                }
//                handler.update(call)
//            }
//
//            // DELETE
//            delete("/by/{id}") {
//                val table = call.parameters["table"]?.lowercase()
//                val handler = table?.let { registry.getHandler(it) }
//                if (handler == null) {
//                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "unknown table"))
//                    return@delete
//                }
//                handler.delete(call)
//            }
//        }
//    }
//}
