package be.ecam.server.models

import org.jetbrains.exposed.sql.Table


data class Admin(
    val id: Int? = null,
    val username: String,
    val password: String,
    val email: String
)

object AdminTable : Table("admins") {
    val id = integer("id").autoIncrement()
    val username = varchar("username", 50)
    val password = varchar("password", 255) // will be hashed later
    val email = varchar("email", 100)
    override val primaryKey = PrimaryKey(id)
}



