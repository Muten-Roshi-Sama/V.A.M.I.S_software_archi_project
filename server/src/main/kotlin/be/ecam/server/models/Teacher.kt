package be.ecam.server.models

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table

object TeacherTable : IntIdTable("teachers") {
    val teacherId = integer("teacher_id").uniqueIndex()
    val email = varchar("email", 100).uniqueIndex()
    val firstName = varchar("first_name", 100)
    val lastName = varchar("last_name", 100)
    val password = varchar("password", 255).nullable()
    val createdAt = varchar("created_at", 100)
}

object ModulesTable : Table("modules") {
    val id = integer("id").autoIncrement()
    val activityName = varchar("activity_name", 255)
    val activityCode = varchar("activity_code", 50).uniqueIndex()
    val ects = integer("ects")
    val description = varchar("description", 500).nullable()
    val coordinator = varchar("coordinator", 100)  // teacher'
    val courseCode = varchar("course_code", 20).nullable()
    override val primaryKey = PrimaryKey(id)
}