package be.ecam.server.models

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table

object OptionTable : IntIdTable("options") {
    val optionCode = varchar("option_code", 10).uniqueIndex()
    val name = varchar("name", 100)
}

object CourseTable : IntIdTable("courses") {
    val courseCode = varchar("course_code", 20).uniqueIndex()
    val courseName = varchar("course_name", 255)
    val totalHours = integer("total_hours")
}

object AnnualStudyPlanTable : IntIdTable("annual_study_plans") {
    val year = varchar("year", 10)
    val optionCode = varchar("option_code", 10).nullable()
    val totalEcts = integer("total_ects")
}

object PlanCourseTable : Table("plan_courses") {
    val id = integer("id").autoIncrement()
    val planId = reference("plan_id", AnnualStudyPlanTable.id)
    val courseCode = varchar("course_code", 20)
    override val primaryKey = PrimaryKey(id)
}