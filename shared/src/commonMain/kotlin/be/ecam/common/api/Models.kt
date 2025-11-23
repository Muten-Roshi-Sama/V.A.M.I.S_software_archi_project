package be.ecam.common.api

import kotlinx.serialization.Serializable


// ========================================


@Serializable
data class HelloResponse(val message: String)

@Serializable
data class ScheduleItem(val title: String)


@Serializable
data class AdminDTO(
    val id: Int? = null,
//    val username: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String,
    val password: String? = null,    // NEVER send pswd to frontend, this is ONLY to retrieve password from frontend,
    val createdAt: String? = null           // let the clientDefault fill it.
)


