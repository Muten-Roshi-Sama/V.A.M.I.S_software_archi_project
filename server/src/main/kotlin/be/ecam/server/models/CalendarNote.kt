package be.ecam.server.models

import be.ecam.common.api.CalendarNoteDTO
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import java.time.Instant

object CalendarNoteTable : IntIdTable(name = "calendar_notes") {
    val student = reference("student_id", StudentTable, onDelete = ReferenceOption.CASCADE)

    /** ISO-8601 date: YYYY-MM-DD */
    val date = varchar("date", 10)

    val courseCode = varchar("course_code", 32)
    val courseName = varchar("course_name", 255).nullable()

    val note = text("note")

    /** ISO-8601 timestamp */
    val createdAt = varchar("created_at", 35)

    init {
        // At most one note per (student, date, course).
        uniqueIndex(student, date, courseCode)
    }
}

class CalendarNote(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<CalendarNote>(CalendarNoteTable) {
        fun nowIso(): String = Instant.now().toString()
    }

    var student by Student referencedOn CalendarNoteTable.student
    var date by CalendarNoteTable.date
    var courseCode by CalendarNoteTable.courseCode
    var courseName by CalendarNoteTable.courseName
    var note by CalendarNoteTable.note
    var createdAt by CalendarNoteTable.createdAt

    fun toDto(): CalendarNoteDTO = CalendarNoteDTO(
        id = this.id.value,
        date = this.date,
        courseCode = this.courseCode,
        courseName = this.courseName,
        note = this.note,
        createdAt = this.createdAt,
    )
}
