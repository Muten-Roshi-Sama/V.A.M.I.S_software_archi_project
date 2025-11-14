package be.ecam.companion.data

import be.ecam.common.api.HelloResponse
import be.ecam.common.api.ScheduleItem
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

// import DTO's
import be.ecam.common.api.AdminDTO

class KtorApiRepository(
    private val client: HttpClient,
    private val baseUrlProvider: () -> String,
    ) : ApiRepository {
    private fun baseUrl() = baseUrlProvider()

    // -------- CRUD ----------

    override suspend fun fetchAdmins(): List<AdminDTO> {
        return client.get("${baseUrl()}/crud/admins").body()
    }





    // ========== version prof ===========
    override suspend fun fetchHello(): HelloResponse {
        println("baseUrl" + baseUrl())
        return client.get("${baseUrl()}/api/hello").body()
    }

    override suspend fun fetchSchedule(): Map<String, List<ScheduleItem>> {
        // The server returns a raw map of ISO date -> items
        return client.get("${baseUrl()}/api/schedule").body()
    }
}
