package be.ecam.server.models

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import be.ecam.common.api.TeacherDTO

object TeacherTable : IntIdTable(name = "teachers") {
    val person = reference("personId", PersonTable, onDelete = ReferenceOption.CASCADE).uniqueIndex()
    val teacherId = varchar("teacherId", 20).nullable().uniqueIndex()  // CHANGE from integer() to varchar()
}

class Teacher(id: EntityID<Int>) : IntEntity(id), PersonInfo {
    companion object : IntEntityClass<Teacher>(TeacherTable) {
        fun createForPerson(person: Person): Teacher = new { this.person = person }
    }

    var person by Person referencedOn TeacherTable.person
    var teacherId by TeacherTable.teacherId

    override val personId: Int? get() = person.id.value
    override val firstName: String? get() = person.firstName
    override val lastName: String? get() = person.lastName
    override val email: String get() = person.email
    override val createdAt: String get() = person.createdAt

    fun toDto(): TeacherDTO = TeacherDTO(
        id = this.id.value,
        teacherId = this.teacherId,
        firstName = this.firstName,
        lastName = this.lastName,
        email = this.email,
        password = null,
        createdAt = this.createdAt
    )
}

object ModulesTable : Table("modules") {
    val id = integer("id").autoIncrement()
    val activityName = varchar("activity_name", 255)
    val activityCode = varchar("activity_code", 50).uniqueIndex()
    val ects = integer("ects")
    val description = varchar("description", 500).nullable()
    val coordinator = varchar("coordinator", 100)
    val courseCode = varchar("course_code", 20).nullable()
    override val primaryKey = PrimaryKey(id)
}