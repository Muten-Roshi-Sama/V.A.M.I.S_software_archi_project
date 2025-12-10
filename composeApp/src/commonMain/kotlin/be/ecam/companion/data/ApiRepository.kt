package be.ecam.companion.data

// DTO's from /shared
import be.ecam.common.api.*


import kotlinx.serialization.Serializable



//Creation of a contract (anything that wants to retrieve data must comply with this contract) :
interface ApiRepository {
    // -------- Auth ----------
    suspend fun login(email: String, password: String): LoginResponse
    suspend fun logout()
    suspend fun getMe(): UserInfo
    fun isAuthenticated(): Boolean

    // -------- Admin CRUD ----------
    suspend fun fetchAdmins(): List<AdminDTO>
    suspend fun fetchAdminById(id: Int): AdminDTO
    suspend fun fetchAdminCount(): Long
    suspend fun createAdmin(admin: AdminDTO): AdminDTO
    suspend fun updateAdmin(id: Int, admin: AdminDTO): AdminDTO
    suspend fun deleteAdmin(id: Int): Boolean
    // Others
    suspend fun fetchMyAdminProfile(): AdminDTO

    // -------- Student CRUD ----------
    suspend fun fetchStudents(): List<StudentDTO>
    suspend fun fetchStudentById(id: Int): StudentDTO
    suspend fun fetchStudentCount(): Long
    suspend fun createStudent(student: StudentDTO): StudentDTO
    suspend fun updateStudent(id: Int, student: StudentDTO): StudentDTO
    suspend fun deleteStudent(id: Int): Boolean
    // Others
    suspend fun fetchMyStudentProfile(): StudentDTO

    // -------- Teacher CRUD ----------







    // -------- Legacy endpoints ----------
    suspend fun fetchHello(): HelloResponse
//    suspend fun fetchSchedule(): Map<String, List<ScheduleItem>>
//    suspend fun fetchAllStudentBulletins(): List<StudentBulletin>
}

//@Serializable
////Evaluation data structure
//data class Evaluation(
//    val activityName: String,
//    val session: String,
//    val score: Int,
//    val maxScore: Int
//)
//
//@Serializable
////A student with all his grades
//data class StudentBulletin(
//    val studentEmail: String,
//    val firstName: String,
//    val lastName: String,
//    val matricule: String,
//    val year: String,
//    val option: String?,//may be null
//    val evaluations: List<Evaluation> //from "Evaluation" class
//)