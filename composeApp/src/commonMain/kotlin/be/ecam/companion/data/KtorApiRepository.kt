package be.ecam.companion.data

// import DTO's
import be.ecam.common.api.*

// Ktor
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.isSuccess
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json



class KtorApiRepository(
    private val client: HttpClient,
    private val baseUrlProvider: () -> String,
) : ApiRepository {
    private fun baseUrl() = baseUrlProvider()

    @Serializable
    private data class ApiErrorResponse(
        val error: String? = null,
        val message: String? = null,
    )

    private val errorJson = Json { ignoreUnknownKeys = true }

    private class ApiHttpException(
        val status: Int,
        val description: String,
        val serverMessage: String,
    ) : RuntimeException("HTTP $status $description: $serverMessage")

    private suspend fun responseMessage(response: HttpResponse): String {
        val text = response.bodyAsText()
        val decoded = runCatching { errorJson.decodeFromString(ApiErrorResponse.serializer(), text) }.getOrNull()
        return decoded?.error ?: decoded?.message ?: text
    }

    private suspend inline fun <reified T> getOrThrow(
        url: String,
        crossinline block: HttpRequestBuilder.() -> Unit = {},
    ): T {
        val response: HttpResponse = client.get(url, block)
        if (!response.status.isSuccess()) {
            throw ApiHttpException(
                status = response.status.value,
                description = response.status.description,
                serverMessage = responseMessage(response),
            )
        }
        return response.body()
    }

    private suspend inline fun <reified T> postOrThrow(
        url: String,
        crossinline block: HttpRequestBuilder.() -> Unit,
    ): T {
        val response: HttpResponse = client.post(url, block)
        if (!response.status.isSuccess()) {
            throw ApiHttpException(
                status = response.status.value,
                description = response.status.description,
                serverMessage = responseMessage(response),
            )
        }
        return response.body()
    }

    // Token storage (in-memory, replace with platform-specific secure storage later)
    private var accessToken: String? = null

    // ========================
    //           Auth
    // ========================
    override suspend fun login(email: String, password: String): LoginResponse {
        return try {
            val response: LoginResponse = postOrThrow("${baseUrl()}/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(email, password))
            }
            accessToken = response.accessToken
            response
        } catch (e: ApiHttpException) {
            if (e.status == 401 || e.status == 403) throw IllegalArgumentException("Invalid credentials")
            throw e
        }
    }
    override suspend fun logout() {
        accessToken = null
    }
    override suspend fun getMe(): UserInfo {
        return getOrThrow("${baseUrl()}/auth/me") {
            bearerAuth(accessToken ?: throw IllegalStateException("Not authenticated"))
        }
    }
    override fun isAuthenticated(): Boolean = accessToken != null



    // ============================
    //           Admin CRUD
    // ============================
    override suspend fun fetchAdmins(): List<AdminDTO> {
        return getOrThrow("${baseUrl()}/crud/admins") {
            bearerAuth(accessToken ?: throw IllegalStateException("Not authenticated"))
        }
    }
    override suspend fun fetchAdminById(id: Int): AdminDTO {
        return getOrThrow("${baseUrl()}/crud/admins/by/$id") {
            bearerAuth(accessToken ?: throw IllegalStateException("Not authenticated"))
        }
    }
    override suspend fun fetchAdminCount(): Long {
        val response: CountResponse = getOrThrow("${baseUrl()}/crud/admins/count") {
            bearerAuth(accessToken ?: throw IllegalStateException("Not authenticated"))
        }
        return response.count
    }
    override suspend fun createAdmin(admin: AdminDTO): AdminDTO {
        return postOrThrow("${baseUrl()}/crud/admins") {
            bearerAuth(accessToken ?: throw IllegalStateException("Not authenticated"))
            contentType(ContentType.Application.Json)
            setBody(admin)
        }
    }
    override suspend fun updateAdmin(id: Int, admin: AdminDTO): AdminDTO {
        val response: HttpResponse = client.put("${baseUrl()}/crud/admins/by/$id") {
            bearerAuth(accessToken ?: throw IllegalStateException("Not authenticated"))
            contentType(ContentType.Application.Json)
            setBody(admin)
        }
        if (!response.status.isSuccess()) {
            throw ApiHttpException(
                status = response.status.value,
                description = response.status.description,
                serverMessage = responseMessage(response),
            )
        }
        return response.body()
    }
    override suspend fun deleteAdmin(id: Int): Boolean {
        return try {
            client.delete("${baseUrl()}/crud/admins/by/$id") {
                bearerAuth(accessToken ?: throw IllegalStateException("Not authenticated"))
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    // Others
    override suspend fun fetchMyAdminProfile(): AdminDTO {
        return getOrThrow("${baseUrl()}/crud/admins/me") {
            bearerAuth(accessToken ?: throw IllegalStateException("Not authenticated"))
        }
    }

    // ============================
    //           Student CRUD
    // ============================
    override suspend fun fetchStudents(): List<StudentDTO> {
        return getOrThrow("${baseUrl()}/crud/students") {
            bearerAuth(accessToken ?: throw IllegalStateException("Not authenticated"))
        }
    }
    override suspend fun fetchStudentById(id: Int): StudentDTO {
        return getOrThrow("${baseUrl()}/crud/students/by/$id") {
            bearerAuth(accessToken ?: throw IllegalStateException("Not authenticated"))
        }
    }
    override suspend fun fetchStudentCount(): Long {
        val response: CountResponse = getOrThrow("${baseUrl()}/crud/students/count") {
            bearerAuth(accessToken ?: throw IllegalStateException("Not authenticated"))
        }
        return response.count
    }
    override suspend fun createStudent(student: StudentDTO): StudentDTO {
        return postOrThrow("${baseUrl()}/crud/students") {
            bearerAuth(accessToken ?: throw IllegalStateException("Not authenticated"))
            contentType(ContentType.Application.Json)
            setBody(student)
        }
    }
    override suspend fun updateStudent(id: Int, student: StudentDTO): StudentDTO {
        val response: HttpResponse = client.put("${baseUrl()}/crud/students/by/$id") {
            bearerAuth(accessToken ?: throw IllegalStateException("Not authenticated"))
            contentType(ContentType.Application.Json)
            setBody(student)
        }
        if (!response.status.isSuccess()) {
            throw ApiHttpException(
                status = response.status.value,
                description = response.status.description,
                serverMessage = responseMessage(response),
            )
        }
        return response.body()
    }
    override suspend fun deleteStudent(id: Int): Boolean {
        return try {
            client.delete("${baseUrl()}/crud/students/by/$id") {
                bearerAuth(accessToken ?: throw IllegalStateException("Not authenticated"))
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    // Others
    override suspend fun fetchMyStudentProfile(): StudentDTO {
        return getOrThrow("${baseUrl()}/crud/students/me") {
            bearerAuth(accessToken ?: throw IllegalStateException("Not authenticated"))
        }
    }

    // ============================
    //           Teacher
    // ============================
    override suspend fun fetchAllTeachers(): List<Teacher> {
        return getOrThrow("${baseUrl()}/crud/teachers") {
            bearerAuth(accessToken ?: throw IllegalStateException("Not authenticated"))
        }
    }

    // ============================
    //           Bible (Study Plans)
    // ============================
    override suspend fun fetchBible(): List<ProgramWithDetails> {
        return getOrThrow("${baseUrl()}/crud/bible") {
            bearerAuth(accessToken ?: throw IllegalStateException("Not authenticated"))
        }
    }

    // ============================
    //           Student Bulletins & Grades
    // ============================
    override suspend fun fetchMyGrades(): StudentBulletin {
        return getOrThrow("${baseUrl()}/crud/students/grades/me") {
            bearerAuth(accessToken ?: throw IllegalStateException("Not authenticated"))
        }
    }

    override suspend fun fetchAllStudentBulletins(): List<StudentBulletin> {
        return getOrThrow("${baseUrl()}/crud/students/all/grades") {
            bearerAuth(accessToken ?: throw IllegalStateException("Not authenticated"))
        }
    }

    // -------- Schedules ----------

    override suspend fun fetchAllSchedules(): List<ScheduleDTO> {
        return getOrThrow("${baseUrl()}/crud/schedules") {
            bearerAuth(accessToken ?: throw IllegalStateException("Not authenticated"))
        }
    }

    override suspend fun fetchSchedulesByDateRange(startDate: String, endDate: String): List<ScheduleDTO> {
        return getOrThrow("${baseUrl()}/crud/schedules/range?startDate=$startDate&endDate=$endDate") {
            bearerAuth(accessToken ?: throw IllegalStateException("Not authenticated"))
        }
    }

    override suspend fun fetchSchedulesByActivity(activityName: String): List<ScheduleDTO> {
        return getOrThrow("${baseUrl()}/crud/schedules/search?activity=$activityName") {
            bearerAuth(accessToken ?: throw IllegalStateException("Not authenticated"))
        }
    }

    override suspend fun fetchSchedulesByYear(studyYear: String): List<ScheduleDTO> {
        return getOrThrow("${baseUrl()}/crud/schedules/year/$studyYear") {
            bearerAuth(accessToken ?: throw IllegalStateException("Not authenticated"))
        }
    }

    override suspend fun fetchSchedulesByTeacher(teacherName: String): List<ScheduleDTO> {
        return getOrThrow("${baseUrl()}/crud/schedules/teacher/$teacherName") {
            bearerAuth(accessToken ?: throw IllegalStateException("Not authenticated"))
        }
    }

    // -------- Legacy endpoints ----------

    override suspend fun fetchHello(): HelloResponse {
        return getOrThrow("${baseUrl()}/api/hello")
    }
}















