package be.ecam.common.api

import kotlinx.serialization.Serializable

@Serializable
data class Module(
    val activity_name: String,
    val description: String,
    val activity_code: String,
    val ects: Int,
)

@Serializable
data class Teacher(
    val email: String,
    val first_name: String,
    val last_name: String,
    val modules: List<Module>
)