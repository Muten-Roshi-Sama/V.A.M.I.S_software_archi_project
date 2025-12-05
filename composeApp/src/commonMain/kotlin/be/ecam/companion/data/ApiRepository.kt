package be.ecam.companion.data

import be.ecam.common.api.AdminDTO
import be.ecam.common.api.HelloResponse
import be.ecam.common.api.ScheduleItem
import be.ecam.common.api.StudentBulletin
import be.ecam.common.api.ProgramWithDetails

interface ApiRepository {
    suspend fun fetchAdmins(): List<AdminDTO>
    suspend fun fetchHello(): HelloResponse
    suspend fun fetchSchedule(): Map<String, List<ScheduleItem>>
    suspend fun fetchAllStudentBulletins(): List<StudentBulletin>
    suspend fun fetchAllTeachers(): List<be.ecam.common.api.Teacher>
    suspend fun fetchTeacher(email: String): be.ecam.common.api.Teacher
    suspend fun fetchBible(): List<ProgramWithDetails>
}