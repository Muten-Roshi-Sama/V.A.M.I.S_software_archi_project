package be.ecam.companion.session

import androidx.compose.runtime.mutableStateMapOf
import kotlinx.datetime.LocalDate

data class StudentCalendarDraft(
    val assignedCourseCode: String? = null,
    val date: LocalDate? = null,
    val notes: String = "",
)

data class StudentCalendarEvent(
    val courseCode: String,
    val courseName: String,
    val notes: String,
)

data class StudentCalendarSession(
    var draft: StudentCalendarDraft = StudentCalendarDraft(),
    val eventsByDate: MutableMap<LocalDate, MutableList<StudentCalendarEvent>> = mutableStateMapOf(),
)

/**
 * In-memory per-student store (session scope = app runtime).
 * Keeps Calendar draft + notes/events separated by studentId.
 */
object StudentCalendarSessionStore {

    private val sessions = mutableStateMapOf<Int, StudentCalendarSession>()

    fun sessionFor(studentId: Int): StudentCalendarSession {
        return sessions.getOrPut(studentId) { StudentCalendarSession() }
    }

    fun getDraft(studentId: Int): StudentCalendarDraft = sessionFor(studentId).draft

    fun updateDraft(
        studentId: Int,
        assignedCourseCode: String? = null,
        date: LocalDate? = null,
        notes: String? = null,
    ) {
        val current = sessionFor(studentId).draft
        sessionFor(studentId).draft = current.copy(
            assignedCourseCode = assignedCourseCode ?: current.assignedCourseCode,
            date = date ?: current.date,
            notes = notes ?: current.notes,
        )
    }

    fun clearDraft(studentId: Int) {
        sessionFor(studentId).draft = StudentCalendarDraft()
    }

    fun addEvent(studentId: Int, date: LocalDate, event: StudentCalendarEvent) {
        val session = sessionFor(studentId)
        val list = session.eventsByDate[date] ?: mutableListOf()
        list.add(event)
        session.eventsByDate[date] = list
    }

    fun eventsByDate(studentId: Int): Map<LocalDate, List<StudentCalendarEvent>> {
        return sessionFor(studentId).eventsByDate
    }
}
