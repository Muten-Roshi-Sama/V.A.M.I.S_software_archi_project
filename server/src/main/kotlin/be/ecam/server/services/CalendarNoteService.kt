package be.ecam.server.services

import be.ecam.common.api.CalendarNoteCreateRequest
import be.ecam.common.api.CalendarNoteDTO
import be.ecam.server.models.CalendarNote
import be.ecam.server.models.CalendarNoteTable
import be.ecam.server.models.Student
import be.ecam.server.models.StudentTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.transactions.transaction

class CalendarNoteService {
    fun listForStudent(
        studentId: Int,
        startDate: String? = null,
        endDate: String? = null,
    ): List<CalendarNoteDTO> = transaction {
        val studentEntityId = EntityID(studentId, StudentTable)

        val base: Op<Boolean> = CalendarNoteTable.student eq studentEntityId

        val withStart = startDate?.let { base and (CalendarNoteTable.date greaterEq it) } ?: base
        val withEnd = endDate?.let { withStart and (CalendarNoteTable.date lessEq it) } ?: withStart

        CalendarNote.find(withEnd)
            .orderBy(CalendarNoteTable.date to SortOrder.ASC)
            .map { it.toDto() }
    }

    /**
     * Creates or updates the note for (student, date, courseCode).
     */
    fun upsertForStudent(studentId: Int, req: CalendarNoteCreateRequest): CalendarNoteDTO = transaction {
        val student = Student.findById(studentId) ?: throw IllegalArgumentException("Student not found")

        val existing = CalendarNote.find {
            (CalendarNoteTable.student eq student.id) and
                (CalendarNoteTable.date eq req.date) and
                (CalendarNoteTable.courseCode eq req.courseCode)
        }.firstOrNull()

        val note = if (existing != null) {
            existing.apply {
                courseName = req.courseName
                this.note = req.note
            }
        } else {
            CalendarNote.new {
                this.student = student
                date = req.date
                courseCode = req.courseCode
                courseName = req.courseName
                note = req.note
                createdAt = CalendarNote.nowIso()
            }
        }

        note.toDto()
    }
}
