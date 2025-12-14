package be.ecam.server.models

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ReferenceOption
import be.ecam.common.api.ScheduleDTO

object ScheduleTable : IntIdTable(name = "schedules") {
    val activityName = varchar("activity_name", 255)
    val startTime = varchar("start_time", 30)
    val endTime = varchar("end_time", 30)
    val description = varchar("description", 500).nullable()
    val studyYear = varchar("study_year", 10)
    val teacherName = varchar("teacher_name", 100)
}

class Schedule(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Schedule>(ScheduleTable)

    var activityName by ScheduleTable.activityName
    var startTime by ScheduleTable.startTime
    var endTime by ScheduleTable.endTime
    var description by ScheduleTable.description
    var studyYear by ScheduleTable.studyYear
    var teacherName by ScheduleTable.teacherName

    fun toDto(): ScheduleDTO = ScheduleDTO(
        id = this.id.value,
        activityName = this.activityName,
        startTime = this.startTime,
        endTime = this.endTime,
        description = this.description,
        studyYear = this.studyYear,
        teacherName = this.teacherName
    )
}
