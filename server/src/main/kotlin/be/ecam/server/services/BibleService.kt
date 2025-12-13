package be.ecam.server.services

import be.ecam.server.models.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import be.ecam.common.api.*

@Serializable
data class OptionSeedDTO(
    val option_code: String,
    val name: String
)

@Serializable
data class CourseSeedDTO(
    val course_code: String,
    val course_name: String,
    val total_hours: Int
)

@Serializable
data class AnnualStudyPlanSeedDTO(
    val year: String,
    val course_list: List<String>,
    val total_ects: Int,
    val option_code: String? = null
)

class BibleService {
    private val json = Json { ignoreUnknownKeys = true }

    private inline fun <reified T> readJson(filename: String): List<T>? {
        val possiblePaths = listOf(
            "server/src/main/resources/data/$filename",
            "src/main/resources/data/$filename",
            "data/$filename"
        )
        val file = possiblePaths.map(::File).firstOrNull { it.exists() } ?: run {
            println("Warning: $filename not found")
            return null
        }
        return json.decodeFromString(file.readText())
    }

    fun seedOptions() {
        val options = readJson<OptionSeedDTO>("options.json") ?: return
        transaction {
            if (OptionTable.selectAll().count() > 0L) {
                println("Info: Options already seeded, skipping")
                return@transaction
            }
            options.forEach { opt ->
                OptionTable.insert {
                    it[optionCode] = opt.option_code
                    it[name] = opt.name
                }
            }
            println("Success: ${options.size} options seeded")
        }
    }

    fun seedCourses() {
        val courses = readJson<CourseSeedDTO>("courses.json") ?: return
        transaction {
            if (CourseTable.selectAll().count() > 0L) {
                println("Info: Courses already seeded, skipping")
                return@transaction
            }
            courses.forEach { course ->
                CourseTable.insert {
                    it[courseCode] = course.course_code
                    it[courseName] = course.course_name
                    it[totalHours] = course.total_hours
                }
            }
            println("Success: ${courses.size} courses seeded")
        }
    }

    fun seedAnnualStudyPlans() {
        val plans = readJson<AnnualStudyPlanSeedDTO>("annual_study_plans.json") ?: return
        transaction {
            if (AnnualStudyPlanTable.selectAll().count() > 0L) {
                println("Info: Study plans already seeded, skipping")
                return@transaction
            }
            plans.forEach { plan ->
                val planId = AnnualStudyPlanTable.insertAndGetId {
                    it[year] = plan.year
                    it[optionCode] = plan.option_code
                    it[totalEcts] = plan.total_ects
                }
                plan.course_list.forEach { courseCode ->
                    PlanCourseTable.insert {
                        it[PlanCourseTable.planId] = planId
                        it[PlanCourseTable.courseCode] = courseCode
                    }
                }
            }
            println("Success: ${plans.size} study plans seeded")
        }
    }

    fun getAllPrograms(): List<ProgramWithDetails> {
        return transaction {
            AnnualStudyPlanTable.selectAll().map { planRow ->
                val planId = planRow[AnnualStudyPlanTable.id].value
                val year = planRow[AnnualStudyPlanTable.year]
                val optionCode = planRow[AnnualStudyPlanTable.optionCode]
                val totalEcts = planRow[AnnualStudyPlanTable.totalEcts]

                val optionName = optionCode?.let {
                    OptionTable.selectAll().where { OptionTable.optionCode eq it }
                        .firstOrNull()?.get(OptionTable.name)
                }

                val courseCodes = PlanCourseTable.selectAll()
                    .where { PlanCourseTable.planId eq planId }
                    .map { it[PlanCourseTable.courseCode] }

                val courses = CourseTable.selectAll()
                    .where { CourseTable.courseCode inList courseCodes }
                    .map { courseRow ->
                        CourseDetail(
                            courseCode = courseRow[CourseTable.courseCode],
                            courseName = courseRow[CourseTable.courseName],
                            totalHours = courseRow[CourseTable.totalHours]
                        )
                    }

                val modules = ModulesTable.selectAll()
                    .where { ModulesTable.courseCode inList courseCodes }
                    .map { moduleRow ->
                        ModuleDetail(
                            activityName = moduleRow[ModulesTable.activityName],
                            activityCode = moduleRow[ModulesTable.activityCode],
                            ects = moduleRow[ModulesTable.ects],
                            description = moduleRow[ModulesTable.description] ?: "",
                            coordinator = moduleRow[ModulesTable.coordinator],
                            courseCode = moduleRow[ModulesTable.courseCode] ?: ""
                        )
                    }

                ProgramWithDetails(
                    year = year,
                    optionCode = optionCode,
                    optionName = optionName,
                    totalEcts = totalEcts,
                    courses = courses,
                    modules = modules
                )
            }
        }
    }
}