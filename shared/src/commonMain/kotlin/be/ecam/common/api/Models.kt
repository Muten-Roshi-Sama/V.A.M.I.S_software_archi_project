package be.ecam.common.api

import kotlinx.serialization.Serializable




// ========== Auth DTOs ==========

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class LoginResponse(val accessToken: String, val tokenType: String = "Bearer")

@Serializable
data class UserInfo(val id: Int, val role: String)

// ========== Response wrappers ==========
@Serializable
data class CountResponse(val count: Long)






// ============== Tables DTO ===========
@Serializable
data class AdminDTO(
    val id: Int? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String,
    val password: String? = null,    // NEVER send pswd to frontend, this is ONLY to retrieve password from frontend,
    val createdAt: String? = null           // let the clientDefault fill it.
)

@Serializable
data class StudentDTO(
    val id: Int? = null,
    val studentId: Int? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String,
    val password: String? = null,    // NEVER send pswd to frontend, this is ONLY to retrieve password from frontend,
    val createdAt: String? = null           // let the clientDefault fill it.
)

@Serializable
data class TeacherDTO(
    val id: Int? = null,
    val teacherId: Int? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String,
    val password: String? = null,    // NEVER send pswd to frontend, this is ONLY to retrieve password from frontend,
    val createdAt: String? = null           // let the clientDefault fill it.
)






// ========================================
@Serializable
data class HelloResponse(val message: String)

@Serializable
data class ScheduleItem(val title: String)

