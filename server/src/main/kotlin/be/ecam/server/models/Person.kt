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
//
import java.time.Instant

interface PersonInfo {
    val personId: Int?
    val firstName: String?
    val lastName: String?
    val email: String
    val createdAt: String
}


// Defines the DB table schema
object PersonTable : IntIdTable(name = "persons") {
    val firstName = varchar("first_name", 120).nullable()
    val lastName = varchar("last_name", 120).nullable()
    val email = varchar("email", 255).uniqueIndex()
    val password = varchar("password", 255)
    val createdAt = varchar("created_at", 64).clientDefault { Instant.now().toString() }}

// Implementation of object PersonTable
class Person(id: EntityID<Int>) : IntEntity(id), PersonInfo {
    companion object : IntEntityClass<Person>(PersonTable)
    //
    override val personId: Int? get() = this.id.value
    override var firstName by PersonTable.firstName
    override var lastName by PersonTable.lastName
    override var email by PersonTable.email

    var password by PersonTable.password
    override var createdAt by PersonTable.createdAt


    // ======== Methods ============
    // TODO: implement hashing
//    internal fun setPasswordHash(hash: String) { password = hash }
    internal fun verifyPasswordPlain(plain: String): Boolean {
        // replace with real hash verification (BCrypt.checkpw)
        return password == plain
    }

//    val fullName: String get() = "$firstName $lastName"
}






