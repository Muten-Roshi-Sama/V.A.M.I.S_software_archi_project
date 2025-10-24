package be.ecam.server.models

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table
// =======================================================
object AdminTable : IntIdTable("admins") {
//    Table("admins") {
//    val id = integer("id").autoIncrement()
    val username = varchar("username", 50)
    val password = varchar("password", 255) // will be hashed later
    val email = varchar("email", 100)
//    override val primaryKey = PrimaryKey(id)
}


class Admin(id: EntityID<Int>) : IntEntity(id){
    companion object : IntEntityClass<Admin>(AdminTable)


    var identifier by AdminTable.id
    var username by AdminTable.username
    var password by AdminTable.password
    var email by AdminTable.email
}
