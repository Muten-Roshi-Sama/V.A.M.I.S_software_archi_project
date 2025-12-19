package be.ecam.companion.data

import be.ecam.common.api.AdminDTO
import be.ecam.common.api.HelloResponse
import be.ecam.common.api.ScheduleItem
import be.ecam.common.api.ProgramWithDetails
import be.ecam.common.api.StudentBulletin
import be.ecam.common.api.Teacher
import kotlinx.serialization.ExperimentalSerializationApi

import kotlinx.serialization.SerialName
import kotlinx.serialization.json.JsonNames
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
    suspend fun fetchHello(): HelloResponse
    //suspend fun fetchSchedule(): Map<String, List<ScheduleItem>>
    suspend fun fetchAllStudentBulletins(): List<StudentBulletin>
    //suspend fun fetchAllCourses(): List<Course>
    suspend fun fetchAllTeachers(): List<Teacher>
    //suspend fun fetchTeacher(email: String): Teacher
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
    suspend fun fetchTeachers(): List<TeacherDTO>
    suspend fun fetchTeacherById(id: Int): TeacherDTO
    suspend fun fetchTeacherCount(): Long
    suspend fun createTeacher(teacher: TeacherDTO): TeacherDTO
    suspend fun updateTeacher(id: Int, teacher: TeacherDTO): TeacherDTO
    suspend fun deleteTeacher(id: Int): Boolean
    suspend fun fetchMyTeacherProfile(): TeacherDTO


    // --------- Course CRUD ----------
    // TO BE IMPLEMENTED LATER




    


    // -------- Bible (Study Plans) ----------
    suspend fun fetchBible(): List<ProgramWithDetails>

    // -------- Student Bulletins & Grades ----------
    suspend fun fetchMyGrades(): StudentBulletin


    // -------- Schedules ----------
    suspend fun fetchAllSchedules(): List<ScheduleDTO>
    suspend fun fetchSchedulesByDateRange(startDate: String, endDate: String): List<ScheduleDTO>
    suspend fun fetchSchedulesByActivity(activityName: String): List<ScheduleDTO>
    suspend fun fetchSchedulesByYear(studyYear: String): List<ScheduleDTO>
    suspend fun fetchSchedulesByTeacher(teacherName: String): List<ScheduleDTO>

    // -------- Calendar Notes (per authenticated student) ----------
    suspend fun fetchMyCalendarNotes(startDate: String? = null, endDate: String? = null): List<CalendarNoteDTO>
    suspend fun upsertMyCalendarNote(request: CalendarNoteCreateRequest): CalendarNoteDTO


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