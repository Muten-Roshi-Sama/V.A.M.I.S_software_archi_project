package be.ecam.common.api

import kotlinx.serialization.Serializable

@Serializable
data class Evaluation(
    val activityName: String,
    val session: String = "Janvier 2025",
    val score: Int,
    val maxScore: Int
)

@Serializable
data class StudentBulletin(
    val studentEmail: String,
    val firstName: String,
    val lastName: String,
    val matricule: String,
    val year: String,
    val option: String?,
    val evaluations: List<Evaluation>
)