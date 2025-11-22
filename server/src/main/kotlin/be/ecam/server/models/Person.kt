package be.ecam.server.models

//Table
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

//DAO
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

//DTO
import be.ecam.common.api.AdminDTO


object PersonTable : IntIdTable(name = "persons") {
    val firstName = varchar("first_name", 120)
    val lastName = varchar("last_name", 120)
    val email = varchar("email", 255).uniqueIndex()
    val password = varchar("password", 255)
    val createdAt = varchar("createdAt", 255)
}

class Person(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Person>(PersonTable)

    var firstName by PersonTable.firstName
    var lastName by PersonTable.lastName
    var email by PersonTable.email
    var password by PersonTable.password
    var createdAt by PersonTable.createdAt

    // ===========================================

    val fullName: String
        get() = "$firstName $lastName"

    fun setPassword(plain: String, cost: Int = 12) {
        password = plain
    }

    fun verifyPassword(plain: String): Boolean {
        return password == plain
    }

    //fun toPersonDto(): PersonDTO = PersonDTO(
    //    id = this.id.value,
    //    firstName = this.firstName,
    //    lastName = this.lastName,
    //    email = this.email
    //)

}






