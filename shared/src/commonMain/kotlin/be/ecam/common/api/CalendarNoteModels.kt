package be.ecam.common.api

import kotlinx.serialization.Serializable

@Serializable
data class CalendarNoteCreateRequest(
    /** ISO-8601 date: YYYY-MM-DD */
    val date: String,
    val courseCode: String,
    val courseName: String? = null,
    val note: String,
)

@Serializable
data class CalendarNoteDTO(
    val id: Int,
    /** ISO-8601 date: YYYY-MM-DD */
    val date: String,
    val courseCode: String,
    val courseName: String? = null,
    val note: String,
    /** ISO-8601 timestamp */
    val createdAt: String,
)
