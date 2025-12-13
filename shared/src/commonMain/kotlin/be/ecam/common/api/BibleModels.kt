package be.ecam.common.api

import kotlinx.serialization.Serializable

@Serializable
data class CourseDetail(
    val courseCode: String,
    val courseName: String,
    val totalHours: Int
)

@Serializable
data class ModuleDetail(
    val activityName: String,
    val activityCode: String,
    val ects: Int,
    val description: String,
    val coordinator: String,
    val courseCode: String
)

@Serializable
data class ProgramWithDetails(
    val year: String,
    val optionCode: String?,
    val optionName: String?,
    val totalEcts: Int,
    val courses: List<CourseDetail>,
    val modules: List<ModuleDetail>
)