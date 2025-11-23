package be.ecam.server.routes.handlers


// Ktor
import io.ktor.server.application.ApplicationCall

// Java

/* !: Goal : single generic /crud/{table} route that delegates to table-specific services,

    - CrudHandler.kt : suspend methods for CRUD actions
    - ex. AdminHandler.kt : implements CRUD methods, instantiates AdminService
    - ./CrudRoutes.kt : central routing service, extracts relevant table and delegate to correspondant service.
    - Routes.kt : application routing
 */


interface CrudHandler {
    suspend fun list(call: ApplicationCall)        // GET /crud/{table}
    suspend fun search(call: ApplicationCall)      // GET /crud/{table}?q=...
    suspend fun getById(call: ApplicationCall)     // GET /crud/{table}/by/{id}
    suspend fun count(call: ApplicationCall)       // GET /crud/{table}/count
    suspend fun create(call: ApplicationCall)      // POST /crud/{table}
    suspend fun update(call: ApplicationCall)      // PUT /crud/{table}/by/{id}
    suspend fun delete(call: ApplicationCall)      // DELETE /crud/{table}/by/{id}
}





