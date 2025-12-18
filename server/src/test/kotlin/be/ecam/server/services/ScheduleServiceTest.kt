package be.ecam.server.services

import be.ecam.server.models.ScheduleTable
import be.ecam.server.testutils.TestDatabase
import be.ecam.common.api.ScheduleCreateDTO
import be.ecam.common.api.ScheduleUpdateDTO
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class ScheduleServiceTest : TestDatabase() {
    override val tables: Array<Table> = arrayOf(ScheduleTable)

    private lateinit var service: ScheduleService

    @BeforeTest
    override fun setupDatabase() {
        super.setupDatabase()
        service = ScheduleService()
    }

    @Test
    fun create_and_getAll() {
        val created = service.create(
            ScheduleCreateDTO(
                activityName = "INFO3 - Cours",
                startTime = "2024-01-10T09:00:00Z",
                endTime = "2024-01-10T11:00:00Z",
                description = "Intro",
                studyYear = "BA3",
                teacherName = "Dr. Smith"
            )
        )
        assertEquals("INFO3 - Cours", created.activityName)
        assertEquals(1, service.getAll().size)
    }

    @Test
    fun filters_byActivityYearTeacher_and_dateRange() {
        // seed
        listOf(
            ScheduleCreateDTO("Algo", "2024-02-01T08:00:00Z", "2024-02-01T10:00:00Z", null, "BA1", "Alice"),
            ScheduleCreateDTO("Algo TP", "2024-02-05T08:00:00Z", "2024-02-05T10:00:00Z", null, "BA1", "Alice"),
            ScheduleCreateDTO("Réseaux", "2024-03-01T13:00:00Z", "2024-03-01T15:00:00Z", null, "BA3", "Bob"),
            ScheduleCreateDTO("AI", "2024-03-10T09:00:00Z", "2024-03-10T12:00:00Z", null, "MA1", "Carol")
        ).forEach { service.create(it) }

        // by activity name 
        val byName = service.getByActivityName("Algo")
        assertEquals(2, byName.size)

        // by study year
        val byYear = service.getByStudyYear("BA1")
        assertEquals(2, byYear.size)

        // by teacher
        val byTeacher = service.getByTeacher("Alice")
        assertEquals(2, byTeacher.size)

        // date range
        val inRange = service.getByDateRange("2024-02-01T00:00:00Z", "2024-02-28T23:59:59Z")
        assertEquals(2, inRange.size)
    }

    @Test
    fun update_partial_and_delete_flow() {
        val created = service.create(
            ScheduleCreateDTO(
                activityName = "Projet",
                startTime = "2024-04-01T10:00:00Z",
                endTime = "2024-04-01T12:00:00Z",
                description = null,
                studyYear = "MA2",
                teacherName = "Coach"
            )
        )
        val id = created.id!!

        val updated = service.update(id, ScheduleUpdateDTO(
            endTime = "2024-04-01T13:00:00Z",
            description = "Sprint 1"
        ))
        assertEquals("2024-04-01T13:00:00Z", updated.endTime)
        assertEquals("Sprint 1", updated.description)

        assertTrue(service.existsById(id))
        assertEquals(1, service.count())

        val deleted = service.delete(id)
        assertTrue(deleted)
        assertFalse(service.existsById(id))
        assertEquals(0, service.count())
    }
}
