package be.ecam.companion.data

import be.ecam.common.api.HelloResponse
import be.ecam.common.api.ScheduleItem
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

// import DTO's
import be.ecam.common.api.AdminDTO

//This class implements the ApiRepository contract :
class KtorApiRepository(
    private val client: HttpClient,
    private val baseUrlProvider: () -> String,
    ) : ApiRepository {
    private fun baseUrl() = baseUrlProvider()

    // -------- CRUD ----------

    //retrieves and returns information from the server//
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

    override suspend fun fetchAllStudentBulletins(): List<StudentBulletin> {
        return client.get("${baseUrl()}/crud/students/all/grades").body()
    }
}
