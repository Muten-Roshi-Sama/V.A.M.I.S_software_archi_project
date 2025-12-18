package be.ecam.server.services

import be.ecam.server.models.*
import be.ecam.common.api.ScheduleDTO
import be.ecam.common.api.ScheduleCreateDTO
import be.ecam.common.api.ScheduleUpdateDTO
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import kotlinx.serialization.Serializable
import be.ecam.server.db.SeedResult
import be.ecam.server.db.seedFromResourceIfMissing

@Serializable
data class ScheduleSeedDTO(
    val activity_name: String,
    val start_time: String,
    val end_time: String,
    val description: String? = null,
    val study_year: String,
    val teacher_name: String
)

class ScheduleService {

    fun getAll(): List<ScheduleDTO> = transaction {
        Schedule.all().map { it.toDto() }
    }

    fun getById(id: Int): ScheduleDTO? = transaction {
        Schedule.findById(id)?.toDto()
    }

    fun getByDateRange(startDate: String, endDate: String): List<ScheduleDTO> = transaction {
        Schedule.all()
            .filter { schedule ->
                schedule.startTime >= startDate && schedule.startTime <= endDate
            }
            .map { it.toDto() }
    }

    fun getByActivityName(activityName: String): List<ScheduleDTO> = transaction {
        Schedule.find { ScheduleTable.activityName like "%$activityName%" }
            .map { it.toDto() }
    }

    fun getByStudyYear(studyYear: String): List<ScheduleDTO> = transaction {
        Schedule.find { ScheduleTable.studyYear eq studyYear }
            .map { it.toDto() }
    }

    fun getByTeacher(teacherName: String): List<ScheduleDTO> = transaction {
        Schedule.find { ScheduleTable.teacherName like "%$teacherName%" }
            .map { it.toDto() }
    }

    fun create(dto: ScheduleCreateDTO): ScheduleDTO = transaction {
        val schedule = Schedule.new {
            activityName = dto.activityName
            startTime = dto.startTime
            endTime = dto.endTime
            description = dto.description
            studyYear = dto.studyYear
            teacherName = dto.teacherName
            assigneeName = dto.assigneeName
            notes = dto.notes
        }
        schedule.toDto()
    }

    fun update(id: Int, dto: ScheduleUpdateDTO): ScheduleDTO = transaction {
        val schedule = Schedule.findById(id) 
            ?: throw IllegalArgumentException("Schedule not found")

        dto.activityName?.let { schedule.activityName = it }
        dto.startTime?.let { schedule.startTime = it }
        dto.endTime?.let { schedule.endTime = it }
        dto.description?.let { schedule.description = it }
        dto.studyYear?.let { schedule.studyYear = it }
        dto.teacherName?.let { schedule.teacherName = it }
        dto.assigneeName?.let { schedule.assigneeName = it }
        dto.notes?.let { schedule.notes = it }

        schedule.toDto()
    }

    fun delete(id: Int): Boolean = transaction {
        val schedule = Schedule.findById(id) ?: return@transaction false
        schedule.delete()
        true
    }

    fun count(): Long = transaction {
        Schedule.all().count()
    }

    fun existsById(id: Int): Boolean = transaction {
        Schedule.findById(id) != null
    }

    fun createScheduleFromDto(dto: ScheduleDTO) {
        val createDto = ScheduleCreateDTO(
            activityName = dto.activityName,
            startTime = dto.startTime,
            endTime = dto.endTime,
            description = dto.description,
            studyYear = dto.studyYear,
            teacherName = dto.teacherName,
            assigneeName = dto.assigneeName,
            notes = dto.notes
        )
        create(createDto)
    }

    fun seedFromResource(resourcePath: String = "data/calendar.json"): SeedResult {
        return seedFromResourceIfMissing<ScheduleDTO>(
            name = "schedules",
            resourcePath = resourcePath,
            exists = { dto: ScheduleDTO -> dto.id?.let { existsById(it) } ?: false },
            create = { dto: ScheduleDTO -> createScheduleFromDto(dto) },
            legacyMapper = { map ->
                fun getString(vararg keys: String): String? {
                    for (k in keys) {
                        val v = map[k] ?: map[k.lowercase()]
                        if (v is String) return v
                        if (v != null) return v.toString()
                    }
                    return null
                }

                val activityName = getString("activityName", "activity_name") ?: ""
                val startTime = getString("startTime", "start_time") ?: ""
                val endTime = getString("endTime", "end_time") ?: ""
                val description = getString("description")
                val studyYear = getString("studyYear", "study_year") ?: ""
                val teacherName = getString("teacherName", "teacher_name") ?: ""
                val assigneeName = getString("assigneeName", "assignee_name", "assignedTo", "assigned_to")
                val notes = getString("notes")

                ScheduleDTO(
                    id = null,
                    activityName = activityName,
                    startTime = startTime,
                    endTime = endTime,
                    description = description,
                    studyYear = studyYear,
                    teacherName = teacherName,
                    assigneeName = assigneeName,
                    notes = notes
                )
            }
        )
    }
}
