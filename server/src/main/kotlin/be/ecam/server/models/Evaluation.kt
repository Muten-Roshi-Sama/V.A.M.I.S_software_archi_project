package be.ecam.server.models

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object EvaluationTable : IntIdTable(name = "evaluations") {
    val student = reference("student_id", StudentTable, onDelete = ReferenceOption.CASCADE)
    val activityName = varchar("activity_name", 255)
    val session = varchar("session", 100)
    val score = integer("score")
    val maxScore = integer("max_score")
}
