package be.ecam.server.routes

import io.ktor.server.routing.Route
import io.ktor.server.routing.route


interface InterfaceRoutes {
    fun registerRoutes(parent: Route)
}

class CrudRegistry(private val handlers: Map<String, InterfaceRoutes>) {
    fun registerAllUnder(parent: Route, basePath: String = "") {
        val path = if (basePath.isNotEmpty()) basePath else ""
        parent.route(path) {
            // or just skip the route() call and register directly on parent
            handlers.forEach { (name, routes) ->
                route("/${name.lowercase()}") {
                    routes.registerRoutes(this)
                }
            }
        }
    }
}