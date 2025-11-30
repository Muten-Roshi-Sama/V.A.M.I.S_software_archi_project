package be.ecam.companion.data
//import the classes that will be used :
import be.ecam.common.api.AdminDTO
import be.ecam.common.api.HelloResponse
import be.ecam.common.api.ScheduleItem
import kotlinx.serialization.Serializable

//Creation of a contract (anything that wants to retrieve data must comply with this contract) :
interface ApiRepository {
    //we must be able to retrieve the following items (suspend = this function can wait for the server's response without blocking the interface)
    suspend fun fetchAdmins(): List<AdminDTO>
    suspend fun fetchHello(): HelloResponse
    suspend fun fetchSchedule(): Map<String, List<ScheduleItem>>
    suspend fun fetchAllStudentBulletins(): List<StudentBulletin>
    suspend fun fetchAllCourses(): List<Course>

}

@Serializable
//Evaluation data structure
data class Evaluation(
    val activityName: String,
    val session: String,
    val score: Int,
    val maxScore: Int
)

@Serializable
//A student with all his grades
data class StudentBulletin(
    val studentEmail: String,
    val firstName: String,
    val lastName: String,
    val matricule: String,
    val year: String,
    val option: String?,//may be null
    val evaluations: List<Evaluation> //from "Evaluation" class
)

// --- Data class pour JSON ---
@Serializable
data class Course(
    val code: String,
    val name: String,
    val year: Int
)
