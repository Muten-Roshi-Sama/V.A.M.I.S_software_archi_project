package be.ecam.common.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Evaluation(
    val activityName: String,
    val session: String = "Janvier 2025",
    val score: Int,
    val maxScore: Int
)

@Serializable
data class StudentBulletin(
    @SerialName("studentEmail")
    val studentEmail: String,
    @SerialName("firstName")
    val firstName: String,
    @SerialName("lastName")
    val lastName: String,
    @SerialName("matricule")
    val matricule: String,
    @SerialName("year")
    val year: String,
    @SerialName("option")
    val option: String?,
    @SerialName("evaluations")
    val evaluations: List<Evaluation>
)