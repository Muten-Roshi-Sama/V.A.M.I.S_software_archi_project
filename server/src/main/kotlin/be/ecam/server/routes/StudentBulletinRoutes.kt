package be.ecam.server.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import be.ecam.common.api.Evaluation
import be.ecam.common.api.StudentBulletin
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.Serializable
import java.io.File

// DTOs pour désérialiser les fichiers JSON
@Serializable
data class StudentDTO(
    val student_id: String,
    val first_name: String,
    val last_name: String,
    val email: String,
    val study_year: String,
    val option_code: String? = null
)

@Serializable
data class GradeDTO(
    val points: Int,
    val student_id: String,
    val activities_id: String,
    val scale: Int
)

@Serializable
data class ModuleDTO(
    val activity_name: String,
    val activity_code: String
)

private val json = Json { ignoreUnknownKeys = true }

private inline fun <reified T> readJsonList(filename: String): List<T> {
    val possiblePaths = listOf(
        "server/src/main/resources/data/$filename",
        "src/main/resources/data/$filename",
        "data/$filename"
    )
    val file = possiblePaths.map(::File).firstOrNull { it.exists() }
        ?: run {
            println(" ERREUR: Fichier $filename introuvable")
            println(" Chemins vérifiés: $possiblePaths")
            throw RuntimeException("Fichier $filename introuvable dans les chemins : $possiblePaths")
        }
    println(" Fichier trouvé: ${file.absolutePath}")
    return json.decodeFromString(file.readText())
}

fun Route.studentBulletinRoutes() {
    println("Enregistrement de studentBulletinRoutes()...")
    route("/students") {
        println("   → Route /students créée")
        get("/all/grades") {
            println("\n🔹 [ROUTE] GET /crud/students/all/grades appelée")
            try {
                println(" Chargement des données...")
                val students = readJsonList<StudentDTO>("students.json")
                println("    ${students.size} étudiants chargés")
                
                val grades = readJsonList<GradeDTO>("grades.json")
                println("    ${grades.size} notes chargées")
                
                val modules = readJsonList<ModuleDTO>("modules.json")
                    .associateBy { it.activity_code }
                println("    ${modules.size} modules chargés")

                val bulletins = students.map { student ->
                    val studentName = "${student.first_name} ${student.last_name}"

                    val evaluations = grades
                        .filter { it.student_id == student.student_id }
                        .mapNotNull { grade ->
                            val activityCode = grade.activities_id
                            val module = modules[activityCode] ?: run {
                                println("    Module non trouvé: $activityCode pour $studentName")
                                return@mapNotNull null
                            }

                            Evaluation(
                                activityName = module.activity_name,
                                session = "Janvier 2025",
                                score = grade.points,
                                maxScore = grade.scale
                            )
                        }
                    
                    println("   → $studentName: ${evaluations.size} évaluations")

                    StudentBulletin(
                        studentEmail = student.email,
                        firstName = student.first_name,
                        lastName = student.last_name,
                        matricule = student.student_id,
                        year = student.study_year,
                        option = student.option_code,
                        evaluations = evaluations
                    )
                }

                println(" ${bulletins.size} bulletins générés avec succès\n")
                call.respondText(json.encodeToString(bulletins), contentType = ContentType.Application.Json)

            } catch (e: Exception) {
                println(" ERREUR: ${e.message}")
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Erreur serveur : ${e.message}")
            }
        }
    }
}