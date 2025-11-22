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
    // admin-specific fields can go here
}

class Admin(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Admin>(AdminTable) {
        // factory helper — call from a transaction in your service:
        fun createForPerson(person: Person): Admin = new {
            this.person = person
        }
    }
    var person by Person referencedOn AdminTable.person

    // Getters
    val firstName get() = person.firstName
    val lastName get() = person.lastName
    val email get() = person.email
    val password get() = person.password
    val createdAt get() = person.createdAt

    fun toDto(): AdminDTO = AdminDTO(
        id = this.id.value,
        firstName = this.firstName,
        lastName = this.lastName,
        email = this.email,
        password = null,                  // HIDE the password, DTO is public
        createdAt = this.createdAt
    )


}





//object AdminTable : IntIdTable("admins") {
////    Table("admins") {
////    val id = integer("id").autoIncrement()
//    val username = varchar("username", 50)
//    val password = varchar("password", 255) // will be hashed later
//    val email = varchar("email", 100)
////    override val primaryKey = PrimaryKey(id)
//}


//class Admin(id: EntityID<Int>) : IntEntity(id){
//    companion object : IntEntityClass<Admin>(AdminTable)
//
//
//    var identifier by AdminTable.id
//    var username by AdminTable.username
//    var password by AdminTable.password
//    var email by AdminTable.email
//}
