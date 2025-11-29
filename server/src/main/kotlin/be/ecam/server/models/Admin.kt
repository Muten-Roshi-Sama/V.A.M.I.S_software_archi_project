package be.ecam.server.models

//Table
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

//DAO
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.sql.Table

//DTO
import be.ecam.common.api.AdminDTO

object AdminTable : IntIdTable(name = "admins") {
    val person = reference("person_id", PersonTable, onDelete = ReferenceOption.CASCADE).uniqueIndex()
    //TODO: add admin-specific fields
}

class Admin(id: EntityID<Int>) : IntEntity(id), PersonInfo {

    companion object : IntEntityClass<Admin>(AdminTable) {
        fun createForPerson(person: Person): Admin = new { this.person = person }
    }

    var person by Person referencedOn AdminTable.person

    // Getters
    override val personId: Int? get() = person.id.value
    override val firstName: String? get() = person.firstName
    override val lastName: String? get() = person.lastName
    override val email: String get() = person.email
    override val createdAt: String get() = person.createdAt

    fun toDto(): AdminDTO = AdminDTO(
        id = this.id.value,            // Create Admin Id
        firstName = this.firstName,
        lastName = this.lastName,
        email = this.email,
        password = null,                  // HIDE password in public DTO
        createdAt = this.createdAt
    )


}


