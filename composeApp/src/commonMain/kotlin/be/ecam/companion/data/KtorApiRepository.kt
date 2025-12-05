package be.ecam.companion.data

import be.ecam.common.api.AdminDTO
import be.ecam.common.api.HelloResponse
import be.ecam.common.api.ScheduleItem
import be.ecam.common.api.StudentBulletin
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import be.ecam.common.api.Teacher
import be.ecam.common.api.ProgramWithDetails
import io.ktor.http.*

class KtorApiRepository(
    private val client: HttpClient,
    private val baseUrlProvider: () -> String,
) : ApiRepository {

    private fun baseUrl() = baseUrlProvider()

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
    override suspend fun fetchAllTeachers(): List<Teacher> {
        return client.get("${baseUrl()}/crud/teachers").body()
    }

    override suspend fun fetchTeacher(email: String): Teacher {
        val encoded = java.net.URLEncoder.encode(email, "UTF-8")
        return client.get("${baseUrl()}/crud/teachers/$encoded").body()
    }
    override suspend fun fetchBible(): List<ProgramWithDetails> {
        return client.get("${baseUrl()}/crud/bible").body()
    }
}
