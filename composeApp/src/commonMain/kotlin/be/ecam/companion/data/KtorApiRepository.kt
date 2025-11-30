package be.ecam.companion.data

import be.ecam.common.api.AdminDTO
import be.ecam.common.api.HelloResponse
import be.ecam.common.api.ScheduleItem
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

//This class implements the ApiRepository contract :
class KtorApiRepository(
    private val client: HttpClient, //the tool for making HTTP requests
    private val baseUrlProvider: () -> String, //function that returns the server URL
) : ApiRepository {

    private fun baseUrl() = baseUrlProvider() //function to retrieve the URL

    //retrieves and returns information from the server//
    override suspend fun fetchAdmins(): List<AdminDTO> {
        return client.get("${baseUrl()}/crud/admins").body()
    }

    override suspend fun fetchHello(): HelloResponse {
        return client.get("${baseUrl()}/api/hello").body()
    }

    override suspend fun fetchSchedule(): Map<String, List<ScheduleItem>> {
        return client.get("${baseUrl()}/api/schedule").body()
    }

    override suspend fun fetchAllStudentBulletins(): List<StudentBulletin> {
        return client.get("${baseUrl()}/crud/students/all/grades").body()
    }
    // ← nouvelle implémentation pour récupérer les cours depuis l’API
    override suspend fun fetchAllCourses(): List<Course> =
        client.get("${baseUrl()}/crud/courses").body()
}
