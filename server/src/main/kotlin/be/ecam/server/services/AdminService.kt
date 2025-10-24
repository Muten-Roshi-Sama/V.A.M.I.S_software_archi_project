package be.ecam.server.services


import be.ecam.common.api.AdminDTO

import be.ecam.server.models.AdminTable
import be.ecam.server.models.Admin

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction




class AdminService {

    // Get all admins as DTOs (for API)
    fun getAll(): List<AdminDTO> = transaction {
        Admin.all().map { entity ->
            AdminDTO(
                id = entity.id.value,
                username = entity.username,
                email = entity.email
            )
        }
    }

    // Create admin from DTO
    fun create(dto: AdminDTO): AdminDTO = transaction {
        val admin = Admin.new {
            username = dto.username
            email = dto.email
            password = "default123" // or hash a provided password
        }
        AdminDTO(
            id = admin.id.value,
            username = admin.username,
            email = admin.email
        )
    }

    // Get by ID
    fun getById(id: Int): AdminDTO? = transaction {
        Admin.findById(id)?.let { entity ->
            AdminDTO(
                id = entity.id.value,
                username = entity.username,
                email = entity.email
            )
        }
    }

    // Delete
    fun delete(id: Int): Boolean = transaction {
        Admin.findById(id)?.delete() != null
    }
}