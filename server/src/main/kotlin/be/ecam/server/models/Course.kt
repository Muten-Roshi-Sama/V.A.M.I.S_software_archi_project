package be.ecam.server.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table

// --- Table Exposed pour la base de données ---
object CourseTable : IntIdTable("courses") {
    val code = varchar("code", 20).uniqueIndex()
    val name = varchar("name", 255)
    val year = integer("year")
}


