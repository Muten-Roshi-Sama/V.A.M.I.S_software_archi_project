package be.ecam.server.services

import be.ecam.server.models.EvaluationTable
import be.ecam.server.models.StudentTable
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

@Serializable
data class StudentSeedDTO(
    val studentEmail: String,
    val firstName: String,
    val lastName: String,
    val matricule: String,
    val year: String,
    val option: String? = null,
    val evaluations: List<EvaluationSeedDTO>
)

@Serializable
data class EvaluationSeedDTO(
    val activityName: String,
    val session: String,
    val score: Int,
    val maxScore: Int
)

class StudentService {
    private val json = Json { ignoreUnknownKeys = true }

    fun seedFromJson() {
        // Cherche le fichier à plusieurs endroits possibles (comme tu fais déjà pour admin.json)
        val possiblePaths = listOf(
            "server/src/main/resources/data/students.json",
            "src/main/resources/data/students.json",
            "data/students.json"
        )
        val file = possiblePaths.map(::File).firstOrNull { it.exists() }
            ?: run {
                println("Warning: students.json not found in any expected path")
                return
            }

        val students = json.decodeFromString<List<StudentSeedDTO>>(file.readText())

        transaction {
            // Vérifie si des étudiants existent déjà
            if (StudentTable.selectAll().count() > 0L) {
                println("Info: Students already seeded, skipping")
                return@transaction
            }

            students.forEach { studentDto ->
                // Insertion étudiant + récupération de l’ID généré
                val studentId = StudentTable.insertAndGetId { row ->
                    row[email] = studentDto.studentEmail
                    row[firstName] = studentDto.firstName
                    row[lastName] = studentDto.lastName
                    row[matricule] = studentDto.matricule
                    row[year] = studentDto.year
                    row[option] = studentDto.option
                }
