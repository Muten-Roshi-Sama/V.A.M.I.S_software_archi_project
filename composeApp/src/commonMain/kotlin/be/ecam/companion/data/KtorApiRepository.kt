package be.ecam.companion.data

// import DTO's
import be.ecam.common.api.*

// Ktor
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType



class KtorApiRepository(
    private val client: HttpClient,
    private val baseUrlProvider: () -> String,
    ) : ApiRepository {
    private fun baseUrl() = baseUrlProvider()

    // Token storage (in-memory, replace with platform-specific secure storage later)
    private var accessToken: String? = null

    // ========================
    //           Auth 
    // ========================
    override suspend fun login(email: String, password: String): LoginResponse {
        try {
            val response: LoginResponse = client.post("${baseUrl()}/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(email, password))
            }.body()
            accessToken = response.accessToken
            return response
        } catch (e: ClientRequestException) {
            // Handle 4xx errors (e.g., 401 Unauthorized)
            throw IllegalArgumentException("Invalid credentials")
        }
    }
    override suspend fun logout() {
        accessToken = null
    }
    override suspend fun getMe(): UserInfo {
        return client.get("${baseUrl()}/auth/me") {
            bearerAuth(accessToken ?: throw IllegalStateException("Not authenticated"))
        }.body()
    }
    override fun isAuthenticated(): Boolean = accessToken != null



    // ============================
    //           Admin CRUD 
    // ============================
    override suspend fun fetchAdmins(): List<AdminDTO> {
        return client.get("${baseUrl()}/crud/admins") {
            bearerAuth(accessToken ?: throw IllegalStateException("Not authenticated"))
        }.body()
    }
    override suspend fun fetchAdminById(id: Int): AdminDTO {
        return client.get("${baseUrl()}/crud/admins/by/$id") {
            bearerAuth(accessToken ?: throw IllegalStateException("Not authenticated"))
        }.body()
    }
    override suspend fun fetchAdminCount(): Long {
        val response: CountResponse = client.get("${baseUrl()}/crud/admins/count") {
            bearerAuth(accessToken ?: throw IllegalStateException("Not authenticated"))
        }.body()
        return response.count
    }
    override suspend fun createAdmin(admin: AdminDTO): AdminDTO {
        return client.post("${baseUrl()}/crud/admins") {
            bearerAuth(accessToken ?: throw IllegalStateException("Not authenticated"))
            contentType(ContentType.Application.Json)
            setBody(admin)
        }.body()
    }
    override suspend fun updateAdmin(id: Int, admin: AdminDTO): AdminDTO {
        return client.put("${baseUrl()}/crud/admins/by/$id") {
            bearerAuth(accessToken ?: throw IllegalStateException("Not authenticated"))
            contentType(ContentType.Application.Json)
            setBody(admin)
        }.body()
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
        return client.get("${baseUrl()}/crud/admins/me") {
            bearerAuth(accessToken ?: throw IllegalStateException("Not authenticated"))
        }.body()
    }

    // ============================
    //           Student CRUD 
    // ============================
    override suspend fun fetchStudents(): List<StudentDTO> {
        return client.get("${baseUrl()}/crud/students") {
            bearerAuth(accessToken ?: throw IllegalStateException("Not authenticated"))
        }.body()
    }
    override suspend fun fetchStudentById(id: Int): StudentDTO {
        return client.get("${baseUrl()}/crud/students/by/$id") {
            bearerAuth(accessToken ?: throw IllegalStateException("Not authenticated"))
        }.body()
    }
    override suspend fun fetchStudentCount(): Long {
        val response: CountResponse = client.get("${baseUrl()}/crud/students/count") {
            bearerAuth(accessToken ?: throw IllegalStateException("Not authenticated"))
        }.body()
        return response.count
    }
    override suspend fun createStudent(student: StudentDTO): StudentDTO {
        return client.post("${baseUrl()}/crud/students") {
            bearerAuth(accessToken ?: throw IllegalStateException("Not authenticated"))
            contentType(ContentType.Application.Json)
            setBody(student)
        }.body()
    }
    override suspend fun updateStudent(id: Int, student: StudentDTO): StudentDTO {
        return client.put("${baseUrl()}/crud/students/by/$id") {
            bearerAuth(accessToken ?: throw IllegalStateException("Not authenticated"))
            contentType(ContentType.Application.Json)
            setBody(student)
        }.body()
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
        return client.get("${baseUrl()}/crud/students/me") {
            bearerAuth(accessToken ?: throw IllegalStateException("Not authenticated"))
        }.body()
    }

    // ============================
    //           Teacher 
    // ============================
    override suspend fun fetchAllTeachers(): List<Teacher> {
        return client.get("${baseUrl()}/crud/teachers") {
            bearerAuth(accessToken ?: throw IllegalStateException("Not authenticated"))
        }.body()
    }

    // ============================
    //           Bible (Study Plans) 
    // ============================
    override suspend fun fetchBible(): List<ProgramWithDetails> {
        return client.get("${baseUrl()}/crud/bible") {
            bearerAuth(accessToken ?: throw IllegalStateException("Not authenticated"))
        }.body()
    }

    // ============================
    //           Student Bulletins & Grades 
    // ============================
    override suspend fun fetchMyGrades(): StudentBulletin {
        return client.get("${baseUrl()}/crud/students/grades/me") {
            bearerAuth(accessToken ?: throw IllegalStateException("Not authenticated"))
        }.body()
    }

    override suspend fun fetchAllStudentBulletins(): List<StudentBulletin> {
        return client.get("${baseUrl()}/crud/students/all/grades") {
            bearerAuth(accessToken ?: throw IllegalStateException("Not authenticated"))
        }.body()
    }

    // -------- Schedules ----------

    override suspend fun fetchAllSchedules(): List<ScheduleDTO> {
        return client.get("${baseUrl()}/crud/schedules") {
            bearerAuth(accessToken ?: throw IllegalStateException("Not authenticated"))
        }.body()
    }

    override suspend fun fetchSchedulesByDateRange(startDate: String, endDate: String): List<ScheduleDTO> {
        return client.get("${baseUrl()}/crud/schedules/range?startDate=$startDate&endDate=$endDate") {
            bearerAuth(accessToken ?: throw IllegalStateException("Not authenticated"))
        }.body()
    }

    override suspend fun fetchSchedulesByActivity(activityName: String): List<ScheduleDTO> {
        return client.get("${baseUrl()}/crud/schedules/search?activity=$activityName") {
            bearerAuth(accessToken ?: throw IllegalStateException("Not authenticated"))
        }.body()
    }

    override suspend fun fetchSchedulesByYear(studyYear: String): List<ScheduleDTO> {
        return client.get("${baseUrl()}/crud/schedules/year/$studyYear") {
            bearerAuth(accessToken ?: throw IllegalStateException("Not authenticated"))
        }.body()
    }

    override suspend fun fetchSchedulesByTeacher(teacherName: String): List<ScheduleDTO> {
        return client.get("${baseUrl()}/crud/schedules/teacher/$teacherName") {
            bearerAuth(accessToken ?: throw IllegalStateException("Not authenticated"))
        }.body()
    }

    // -------- Legacy endpoints ----------

    override suspend fun fetchHello(): HelloResponse {
        return client.get("${baseUrl()}/api/hello").body()
    }
}
















