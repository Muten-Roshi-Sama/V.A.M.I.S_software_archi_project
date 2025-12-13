package be.ecam.companion.data

import be.ecam.common.api.AdminDTO
import be.ecam.common.api.HelloResponse
import be.ecam.common.api.ScheduleItem
import be.ecam.common.api.ProgramWithDetails
import be.ecam.common.api.StudentBulletin
import be.ecam.common.api.Teacher
import kotlinx.serialization.Serializable

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

@Serializable
data class Course(
    val code: String,
    val name: String,
    val year: Int
)
