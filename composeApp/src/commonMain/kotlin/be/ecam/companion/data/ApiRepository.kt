package be.ecam.companion.data

import be.ecam.common.api.AdminDTO
import be.ecam.common.api.HelloResponse
import be.ecam.common.api.ScheduleItem

interface ApiRepository {


    // -------- CRUD ----------
    suspend fun fetchAdmins(): List<AdminDTO>

    // ========== version prof ===========
    suspend fun fetchHello(): HelloResponse
    // Directly return the raw map instead of wrapping in ScheduleResponse for simplicity
    suspend fun fetchSchedule(): Map<String, List<ScheduleItem>>
}
