package be.ecam.server.models

//Table
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

//DAO
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.IntEntityClass

object StudentTable : IntIdTable("students") {
    val email = varchar("email", 100).uniqueIndex()
    val firstName = varchar("first_name", 100)
    val lastName = varchar("last_name", 100)
    val matricule = varchar("matricule", 5).uniqueIndex()
    val year = varchar("year", 10) // BA1, BA2, BA3, MA1, MA2
    val option = varchar("option", 50).nullable() // null pour BA1/BA2
}

object EvaluationTable : Table("evaluations") {
    val id = integer("id").autoIncrement()
    val student = reference("student_id", StudentTable)
    val activityName = varchar("activity_name", 255)
    val session = varchar("session", 50) // "Janvier 2025", "Juin 2024", ...
    val score = integer("score")
    val maxScore = integer("max_score")
    override val primaryKey = PrimaryKey(id)
}
