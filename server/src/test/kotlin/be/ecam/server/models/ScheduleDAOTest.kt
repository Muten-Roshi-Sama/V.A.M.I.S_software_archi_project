package be.ecam.server.models

import be.ecam.server.testutils.TestDatabase
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ScheduleDAOTest : TestDatabase() {
    override val tables: Array<Table> = arrayOf(ScheduleTable)

    @BeforeTest
    override fun setupDatabase() {
        super.setupDatabase()
    }

    @Test
    fun createSchedule() {
        transaction {
            val schedule = Schedule.new {
                activityName = "INFO3 - Cours"
                startTime = "2024-01-10T09:00:00Z"
                endTime = "2024-01-10T11:00:00Z"
                description = "Introduction au cours"
                studyYear = "BA3"
                teacherName = "Dr. Smith"
            }

            assertNotNull(schedule.id)
            assertEquals("INFO3 - Cours", schedule.activityName)
            assertEquals("BA3", schedule.studyYear)
            assertEquals("Dr. Smith", schedule.teacherName)
        }
    }

    @Test
    fun convertsToDto() {
        transaction {
            val schedule = Schedule.new {
                activityName = "LAB ELEC"
                startTime = "2024-01-15T13:00:00Z"
                endTime = "2024-01-15T16:00:00Z"
                description = null
                studyYear = "MA1"
                teacherName = "Mme. Dupont"
            }

            val dto = schedule.toDto()

            assertEquals(schedule.id.value, dto.id)
            assertEquals("LAB ELEC", dto.activityName)
            assertEquals("2024-01-15T13:00:00Z", dto.startTime)
            assertEquals("2024-01-15T16:00:00Z", dto.endTime)
            assertNull(dto.description)
            assertEquals("MA1", dto.studyYear)
            assertEquals("Mme. Dupont", dto.teacherName)
        }
    }

    @Test
    fun findScheduleById() {
        val id = transaction {
            Schedule.new {
                activityName = "PROJET"
                startTime = "2024-02-01T08:00:00Z"
                endTime = "2024-02-01T12:00:00Z"
                description = "Sprint planning"
                studyYear = "MA2"
                teacherName = "Coach"
            }.id.value
        }

        transaction {
            val found = Schedule.findById(id)
            assertNotNull(found)
            assertEquals("PROJET", found!!.activityName)
        }
    }

    @Test
    fun allSchedules_returnsAllRecords() {
        transaction {
            repeat(3) { i ->
                Schedule.new {
                    activityName = "ACT$i"
                    startTime = "2024-01-0${i + 1}T10:00:00Z"
                    endTime = "2024-01-0${i + 1}T12:00:00Z"
                    description = null
                    studyYear = if (i % 2 == 0) "BA2" else "BA3"
                    teacherName = "Teacher $i"
                }
            }
            val all = Schedule.all().toList()
            assertEquals(3, all.size)
        }
    }

    @Test
    fun deleteSchedule() {
        val id = transaction {
            Schedule.new {
                activityName = "Temporaire"
                startTime = "2024-03-01T09:00:00Z"
                endTime = "2024-03-01T10:00:00Z"
                description = null
                studyYear = "BA1"
                teacherName = "Temp"
            }.id.value
        }

        transaction {
            val s = Schedule.findById(id)
            assertNotNull(s)
            s!!.delete()
            val after = Schedule.findById(id)
            assertNull(after)
        }
    }
}