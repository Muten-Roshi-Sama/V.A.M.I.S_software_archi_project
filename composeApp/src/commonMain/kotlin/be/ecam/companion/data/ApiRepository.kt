package be.ecam.companion.data

import be.ecam.common.api.AdminDTO
import be.ecam.common.api.HelloResponse
import be.ecam.common.api.ScheduleItem
import be.ecam.common.api.ProgramWithDetails
import be.ecam.common.api.StudentBulletin
import be.ecam.common.api.Teacher
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.JsonNames

interface ApiRepository {
    suspend fun fetchAdmins(): List<AdminDTO>
    suspend fun fetchHello(): HelloResponse
    suspend fun fetchSchedule(): Map<String, List<ScheduleItem>>
    suspend fun fetchAllStudentBulletins(): List<StudentBulletin>
    suspend fun fetchAllCourses(): List<Course>
    suspend fun fetchAllTeachers(): List<Teacher>
    suspend fun fetchTeacher(email: String): Teacher
    suspend fun fetchBible(): List<ProgramWithDetails>
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Course(
    @SerialName("course_code")
    @JsonNames("course_code", "courseCode", "Code", "code")
    val code: String,

    @SerialName("course_name")
    @JsonNames("course_name", "courseName", "name")
    val name: String,

    @SerialName("total_hours")
    @JsonNames("total_hours", "totalHours", "hours")
    val totalHours: Int? = null,
)
