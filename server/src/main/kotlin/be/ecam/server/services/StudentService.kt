package be.ecam.server.services

import be.ecam.server.models.EvaluationTable
import be.ecam.server.models.StudentTable
import be.ecam.common.api.Evaluation     //from /shared/
import be.ecam.common.api.StudentBulletin   //from /shared/
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.time.Year

//DTOs used only to read JSON at startup (seed)
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
data class StudentSeedV2DTO(
    val student_id: String,
    val first_name: String,
    val last_name: String,
    val email: String,
    val password: String? = null,
    val created_at: String? = null,
    val study_year: String,
    val option_code: String? = null,
)

@Serializable
data class EvaluationSeedDTO(
    val activityName: String,
    val session: String,
    val score: Int,
    val maxScore: Int
)

@Serializable
data class GradeSeedDTO(
    val points: Int,
    val comment: String? = null,
    val student_id: String,
    val activities_id: String,
    val scale: Int,
)

class StudentService {
    private val json = Json { ignoreUnknownKeys = true }

    private fun defaultEvaluationSession(): String = Year.now().value.toString()

    //Seed function called once when the server starts up
    //It reads students.json and fills the database
    fun seedFromJson() {
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

        val fileText = file.readText()
        val studentsV1: List<StudentSeedDTO>? = try {
            json.decodeFromString(fileText)
        } catch (_: Exception) {
            null
        }

        val studentsV2: List<StudentSeedV2DTO>? = if (studentsV1 == null) {
            try {
                json.decodeFromString(fileText)
            } catch (e: Exception) {
                throw e
            }
        } else {
            null
        }

        transaction {
            if (StudentTable.selectAll().count() > 0L) {
                println("Info: Students already seeded, skipping")
                return@transaction
            }

            if (studentsV1 != null) {
                studentsV1.forEach { studentDto ->
                    val studentId = StudentTable.insertAndGetId { row ->
                        row[email] = studentDto.studentEmail
                        row[firstName] = studentDto.firstName
                        row[lastName] = studentDto.lastName
                        row[matricule] = studentDto.matricule
                        row[year] = studentDto.year
                        row[option] = studentDto.option
                    }

                    studentDto.evaluations.forEach { eval ->
                        EvaluationTable.insert { row ->
                            row[this.student] = studentId.value
                            row[activityName] = eval.activityName
                            row[session] = eval.session
                            row[score] = eval.score
                            row[maxScore] = eval.maxScore
                        }
                    }
                }
                println("Success: ${studentsV1.size} students + their evaluations seeded from students.json")
                return@transaction
            }

            if (studentsV2 != null) {
                studentsV2.forEach { studentDto ->
                    StudentTable.insert { row ->
                        row[email] = studentDto.email
                        row[firstName] = studentDto.first_name
                        row[lastName] = studentDto.last_name
                        row[matricule] = studentDto.student_id
                        row[year] = studentDto.study_year
                        row[option] = studentDto.option_code
                    }
                }
                println("Success: ${studentsV2.size} students seeded from students.json (no evaluations)")
            }
        }
    }

    fun syncGradesFromJson() {
        val possiblePaths = listOf(
            "server/src/main/resources/data/grades.json",
            "src/main/resources/data/grades.json",
            "data/grades.json"
        )
        val file = possiblePaths.map(::File).firstOrNull { it.exists() }
            ?: run {
                println("Warning: grades.json not found in any expected path")
                return
            }

        val grades = json.decodeFromString<List<GradeSeedDTO>>(file.readText())
        val sessionValue = defaultEvaluationSession()

        transaction {
            var inserted = 0
            var updated = 0
            var missingStudents = 0

            grades.forEach { grade ->
                val matricule = grade.student_id.trim()
                val studentRow = StudentTable
                    .selectAll()
                    .where { StudentTable.matricule eq matricule }
                    .firstOrNull()

                if (studentRow == null) {
                    missingStudents++
                    return@forEach
                }

                val studentId = studentRow[StudentTable.id]
                val existing = EvaluationTable
                    .selectAll()
                    .where {
                        (EvaluationTable.student eq studentId) and
                                (EvaluationTable.activityName eq grade.activities_id) and
                                (EvaluationTable.session eq sessionValue)
                    }
                    .firstOrNull()

                if (existing == null) {
                    EvaluationTable.insert { row ->
                        row[this.student] = studentId
                        row[activityName] = grade.activities_id
                        row[session] = sessionValue
                        row[score] = grade.points
                        row[maxScore] = grade.scale
                    }
                    inserted++
                } else {
                    val evalId = existing[EvaluationTable.id]
                    val changed = EvaluationTable.update({ EvaluationTable.id eq evalId }) { row ->
                        row[score] = grade.points
                        row[maxScore] = grade.scale
                    }
                    if (changed > 0) updated++
                }
            }

            if (inserted > 0) println("Success: $inserted new evaluation(s) inserted from grades.json")
            if (updated > 0) println("Success: $updated evaluation(s) updated from grades.json")
            if (missingStudents > 0) println("Warning: $missingStudents grade(s) skipped (no matching student matricule)")
        }
    }

    //// ========== CRUD : READ from BD ==========
    fun getAllStudents(): List<StudentBulletin> {
        return transaction {
            // SELECT * FROM students
            StudentTable.selectAll().map { studentRow ->
                val studentId = studentRow[StudentTable.id]

                //retrieval of assessments for each student
                val evaluations = EvaluationTable
                    .selectAll()
                    .where { EvaluationTable.student eq studentId }
                    .map { evalRow ->
                        Evaluation(  //using the DTO from /shared/
                            activityName = evalRow[EvaluationTable.activityName],
                            session = evalRow[EvaluationTable.session],
                            score = evalRow[EvaluationTable.score],
                            maxScore = evalRow[EvaluationTable.maxScore]
                        )
                    }

                //Construction of the StudentBulletin object
                StudentBulletin(  //using the DTO from /shared/
                    studentEmail = studentRow[StudentTable.email],
                    firstName = studentRow[StudentTable.firstName],
                    lastName = studentRow[StudentTable.lastName],
                    matricule = studentRow[StudentTable.matricule],
                    year = studentRow[StudentTable.year],
                    option = studentRow[StudentTable.option],
                    evaluations = evaluations
                )
            }
        }
    }

    //Getting one student by his email
    fun getStudentByEmail(emailParam: String): StudentBulletin? {
        return transaction {
            val studentRow = StudentTable
                .selectAll()
                .where { StudentTable.email eq emailParam }
                .firstOrNull() ?: return@transaction null

            val studentId = studentRow[StudentTable.id]

            val evaluations = EvaluationTable
                .selectAll()
                .where { EvaluationTable.student eq studentId }
                .map { evalRow ->
                    Evaluation(
                        activityName = evalRow[EvaluationTable.activityName],
                        session = evalRow[EvaluationTable.session],
                        score = evalRow[EvaluationTable.score],
                        maxScore = evalRow[EvaluationTable.maxScore]
                    )
                }

            StudentBulletin(
                studentEmail = studentRow[StudentTable.email],
                firstName = studentRow[StudentTable.firstName],
                lastName = studentRow[StudentTable.lastName],
                matricule = studentRow[StudentTable.matricule],
                year = studentRow[StudentTable.year],
                option = studentRow[StudentTable.option],
                evaluations = evaluations
            )
        }
    }

    //Getting one student by his matricule
    fun getStudentByMatricule(matriculeParam: String): StudentBulletin? {
        return transaction {
            val studentRow = StudentTable
                .selectAll()
                .where { StudentTable.matricule eq matriculeParam }
                .firstOrNull() ?: return@transaction null

            val studentId = studentRow[StudentTable.id]

            val evaluations = EvaluationTable
                .selectAll()
                .where { EvaluationTable.student eq studentId }
                .map { evalRow ->
                    Evaluation(
                        activityName = evalRow[EvaluationTable.activityName],
                        session = evalRow[EvaluationTable.session],
                        score = evalRow[EvaluationTable.score],
                        maxScore = evalRow[EvaluationTable.maxScore]
                    )
                }

            StudentBulletin(
                studentEmail = studentRow[StudentTable.email],
                firstName = studentRow[StudentTable.firstName],
                lastName = studentRow[StudentTable.lastName],
                matricule = studentRow[StudentTable.matricule],
                year = studentRow[StudentTable.year],
                option = studentRow[StudentTable.option],
                evaluations = evaluations
            )
        }
    }

    //// ========== CREATE (change in the database) ==========

    //Create a new student with their assessments
    fun createStudent(student: StudentSeedDTO) {
        transaction {
            //Check if the student already exists
            val existing = StudentTable
                .selectAll()
                .where { (StudentTable.email eq student.studentEmail) or (StudentTable.matricule eq student.matricule) }
                .count()

            if (existing > 0L) {
                throw IllegalArgumentException("Student with this email or matricule already exists")
            }

            //Innsert into students
            val studentId = StudentTable.insertAndGetId { row ->
                row[email] = student.studentEmail
                row[firstName] = student.firstName
                row[lastName] = student.lastName
                row[matricule] = student.matricule
                row[year] = student.year
                row[option] = student.option
            }

            //Insert into evaluations (for each eval)
            student.evaluations.forEach { eval ->
                EvaluationTable.insert { row ->
                    row[this.student] = studentId.value
                    row[activityName] = eval.activityName
                    row[session] = eval.session
                    row[score] = eval.score
                    row[maxScore] = eval.maxScore
                }
            }
        }
    }

    //Add one assessment to an existing student
    fun addEvaluation(studentEmail: String, eval: EvaluationSeedDTO) {
        transaction {
            val studentRow = StudentTable
                .selectAll()
                .where { StudentTable.email eq studentEmail }
                .firstOrNull() ?: throw IllegalArgumentException("Student not found")

            val studentId = studentRow[StudentTable.id]

            EvaluationTable.insert { row ->
                row[this.student] = studentId
                row[activityName] = eval.activityName
                row[session] = eval.session
                row[score] = eval.score
                row[maxScore] = eval.maxScore
            }
        }
    }

    // ========== UPDATE change in the database ==========

    //Updates a student's information
    fun updateStudent(emailParam: String, newData: StudentSeedDTO) {
        transaction {
            val updated = StudentTable.update({ StudentTable.email eq emailParam }) { row ->
                row[firstName] = newData.firstName
                row[lastName] = newData.lastName
                row[year] = newData.year
                row[option] = newData.option
            }

            if (updated == 0) {
                throw IllegalArgumentException("Student not found")
            }
        }
    }

    //Updates the score of an existing assessment
    fun updateEvaluation(studentEmail: String, activityName: String, session: String, newScore: Int) {
        transaction {
            val studentRow = StudentTable
                .selectAll()
                .where { StudentTable.email eq studentEmail }
                .firstOrNull() ?: throw IllegalArgumentException("Student not found")

            val studentId = studentRow[StudentTable.id]
            val updated = EvaluationTable.update({
                (EvaluationTable.student eq studentId) and
                        (EvaluationTable.activityName eq activityName) and
                        (EvaluationTable.session eq session)
            }) { row ->
                row[score] = newScore
            }

            if (updated == 0) {
                throw IllegalArgumentException("Evaluation not found")
            }
        }
    }

    // ========== DELETE (in the DB) ==========

    //Deletes a student AND all of their assessments
    fun deleteStudent(emailParam: String) {
        transaction {
            val studentRow = StudentTable
                .selectAll()
                .where { StudentTable.email eq emailParam }
                .firstOrNull() ?: throw IllegalArgumentException("Student not found")

            val studentId = studentRow[StudentTable.id]

            EvaluationTable.deleteWhere { EvaluationTable.student eq studentId }
            StudentTable.deleteWhere { StudentTable.email eq emailParam }
        }
    }

    //Delete one specific assessment
    fun deleteEvaluation(studentEmail: String, activityName: String, session: String) {
        transaction {
            val studentRow = StudentTable
                .selectAll()
                .where { StudentTable.email eq studentEmail }
                .firstOrNull() ?: throw IllegalArgumentException("Student not found")

            val studentId = studentRow[StudentTable.id]

            val deleted = EvaluationTable.deleteWhere {
                (EvaluationTable.student eq studentId) and
                        (EvaluationTable.activityName eq activityName) and
                        (EvaluationTable.session eq session)
            }

            if (deleted == 0) {
                throw IllegalArgumentException("Evaluation not found")
            }
        }
    }
}