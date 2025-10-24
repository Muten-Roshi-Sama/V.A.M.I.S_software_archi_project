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
    val username: String,
    val email: String
)


