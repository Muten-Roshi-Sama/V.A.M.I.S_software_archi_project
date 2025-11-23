package be.ecam.server.routes

import be.ecam.common.Greeting
import be.ecam.common.api.HelloResponse
import be.ecam.common.api.ScheduleItem
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.DayOfWeek
import java.time.LocalDate

fun Route.registerApiRoutes() {

    // Root route for API
    get("/") {
        call.respondText("Ktor: ${Greeting().greet()}")
    }

    // /api/hello endpoint
    get("/hello") {
        call.respond(HelloResponse(message = "Hello from Ktor server"))
    }

    // /api/schedule endpoint
    get("/schedule") {
        val schedule = mutableMapOf<String, List<ScheduleItem>>()

        // A couple of fixed examples around current timeframe
        schedule["2025-09-30"] = listOf(ScheduleItem("Team sync"), ScheduleItem("Release planning"))
        schedule["2025-10-01"] = listOf(ScheduleItem("Code review"))

        // Add examples for each remaining month of 2025
        for (month in 1..12) {
            val first = LocalDate.of(2025, month, 1)
            val mid = first.withDayOfMonth(minOf(15, first.lengthOfMonth()))
            val last = first.withDayOfMonth(first.lengthOfMonth())

            schedule.putIfAbsent(first.toString(), listOf(ScheduleItem("Kickoff ${first.month.name.lowercase().replaceFirstChar { it.titlecase() }}")))
            schedule.putIfAbsent(mid.toString(), listOf(ScheduleItem("Mid-month check"), ScheduleItem("Demo prep")))
            schedule.putIfAbsent(last.toString(), listOf(ScheduleItem("Retrospective")))
        }

        // Weekly examples on Mondays in Q4 2025
        var d = LocalDate.of(2025, 10, 1)
        while (!d.isAfter(LocalDate.of(2025, 12, 31))) {
            if (d.dayOfWeek == DayOfWeek.MONDAY) {
                schedule.putIfAbsent(d.toString(), listOf(ScheduleItem("Weekly planning")))
            }
            d = d.plusDays(1)
        }

        call.respond(schedule)
    }
}
