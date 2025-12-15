package be.ecam.server.models

//Table
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

//DAO
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.IntEntityClass

//DTO
import be.ecam.common.api.StudentDTO

object StudentTable : IntIdTable(name = "students") {
    val person = reference("personId", PersonTable, onDelete = ReferenceOption.CASCADE).uniqueIndex()
    
    //  student-specific fields
    val studentId = varchar("studentId", 20).nullable().uniqueIndex()
    val studyYear = varchar("studyYear", 64).nullable()
    val optionCode = varchar("optionCode", 64).nullable()

}

class Student(id: EntityID<Int>) : IntEntity(id), PersonInfo {

    companion object : IntEntityClass<Student>(StudentTable) {
        fun createForPerson(person: Person): Student = new { this.person = person }
    }

    var person by Person referencedOn StudentTable.person
    var studentId by StudentTable.studentId    
    var studyYear by StudentTable.studyYear    
    var optionCode by StudentTable.optionCode 

    // Getters from PersonInfo interface
    override val personId: Int? get() = person.id.value
    override val firstName: String? get() = person.firstName
    override val lastName: String? get() = person.lastName
    override val email: String get() = person.email
    override val createdAt: String get() = person.createdAt

    // REMOVE the duplicate override declarations for studentId, studyYear, optionCode

    fun toDto(): StudentDTO = StudentDTO(
        id = this.id.value,
        studentId = this.studentId,      // Now unambiguous
        firstName = this.firstName,
        lastName = this.lastName,
        email = this.email,
        password = null,
        studyYear = this.studyYear,      // Now unambiguous
        optionCode = this.optionCode,    // Now unambiguous
        createdAt = this.createdAt
    )
}

