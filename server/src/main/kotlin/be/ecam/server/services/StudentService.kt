package be.ecam.server.services

//DAO
import be.ecam.server.models.Person
import be.ecam.server.models.Student
import be.ecam.server.models.StudentTable

//DTO
import be.ecam.common.api.StudentDTO

//SeedManager
import be.ecam.server.db.SeedResult
import be.ecam.server.db.seedFromResourceIfMissing

// util
import be.ecam.server.util.requireValidEmail
import be.ecam.server.util.requireValidPassword
import kotlinx.serialization.Serializable

//Exposed
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.exceptions.ExposedSQLException

// ----------------------------

@Serializable
data class StudentCreateDTO(
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String,
    val password: String,
    //
    val studentId: String? = null,
    val studyYear: String? = null,
    val optionCode: String? = null
)

@Serializable
data class StudentUpdateDTO(
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val password: String? = null,
    //
    val studentId: String? = null,
    val studyYear: String? = null,
    val optionCode: String? = null
)

class StudentService(private val personService: PersonService = PersonService()) {

    /* Create a RoleService instance configured for Student */
    private val ops = RoleService<StudentDTO, Student>(
        personService = personService,
        findAll = { Student.all() },
        findById = { id -> Student.findById(id) },
        deleteById = { id -> Student.findById(id)?.delete() != null },
        createForPerson = { person -> Student.createForPerson(person) },
        toDto = { student -> student.toDto() },
        dtoToPersonCreate = { dto ->
            PersonCreateDTO(
                firstName = dto.firstName,
                lastName = dto.lastName,
                email = dto.email,
                password = dto.password ?: throw IllegalArgumentException("password required for creation"),
                createdAt = dto.createdAt
            )
        },
        dtoEmail = { dto -> dto.email }
    )
    fun getAll(): List<StudentDTO> = ops.getAll()
    fun getById(studentId: Int): StudentDTO? = ops.getById(studentId)
    fun delete(studentId: Int): Boolean = ops.delete(studentId)
    fun count(): Long = ops.count()
    fun existsByEmail(email: String): Boolean = ops.existsByEmail(email)

    /* Create student from DTO */
    fun create(createDto: StudentCreateDTO): StudentDTO = transaction {
        val emailField = createDto.email
        val passwordField = createDto.password

        requireValidEmail(emailField)
        requireValidPassword(passwordField, minLength = 6)

        if (personService.existsByEmail(emailField)) {
            throw IllegalArgumentException("Email '$emailField' is already registered")
        }

        try {
            val person = personService.create(
                PersonCreateDTO(
                    firstName = createDto.firstName,
                    lastName = createDto.lastName,
                    email = emailField,
                    password = passwordField
                )
            )

            val student = Student.createForPerson(person)

            //student specific fields
            student.studentId = createDto.studentId
            student.studyYear = createDto.studyYear
            student.optionCode = createDto.optionCode

            student.toDto()
        } catch (ex: ExposedSQLException) {
            val msg = ex.message ?: ""
            if (msg.contains("UNIQUE", true) || msg.contains("constraint", true)) {
                throw IllegalArgumentException("Email '$emailField' is already registered (unique constraint).")
            }
            throw ex
        }
    }

    fun update(studentId: Int, dto: StudentUpdateDTO): StudentDTO = transaction {
        val student = Student.findById(studentId) ?: throw IllegalArgumentException("not found")
        val person = student.person

        dto.email?.let { newEmail ->
            if (newEmail != person.email) {
                requireValidEmail(newEmail)
                val existing = personService.findByEmail(newEmail)
                if (existing != null && existing.id.value != person.id.value) {
                    throw IllegalArgumentException("Email '$newEmail' is already registered")
                }
                person.email = newEmail
            }
        }

        dto.firstName?.let { person.firstName = it }
        dto.lastName?.let { person.lastName = it }

        dto.password?.let { pw ->
            requireValidPassword(pw, minLength = 6)
            person.password = pw // TODO: hash
        }

        // student specific fields
        dto.studentId?.let { student.studentId = it }
        dto.studyYear?.let { student.studyYear = it }
        dto.optionCode?.let { student.optionCode = it }

        student.toDto()
    }

    // convert incoming StudentDTO (frontend) to StudentCreateDTO then call create()
    fun createStudentFromDto(dto: StudentDTO) {
        val createDto = StudentCreateDTO(
            firstName = dto.firstName,
            lastName = dto.lastName,
            email = dto.email,
            password = dto.password ?: ""
        )
        create(createDto)
    }

    // ======== Seeding ===========

    fun seedFromResource(resourcePath: String = "data/students.json"): SeedResult {
        return seedFromResourceIfMissing<StudentDTO>(
            name = "students",
            resourcePath = resourcePath,
            exists = { dto: StudentDTO -> existsByEmail(dto.email) },
            create = { dto: StudentDTO -> createStudentFromDto(dto) },

            legacyMapper = { map ->
                fun getString(vararg keys: String): String? {
                    for (k in keys) {
                        val v = map[k] ?: map[k.lowercase()]
                        if (v is String) return v
                        if (v != null) return v.toString()
                    }
                    return null
                }

                val firstName = getString("firstName") ?: ""
                val lastName = getString("lastName") ?: ""
                val email = getString("email") ?: ""
                val password = getString("password")
                val createdAt = getString("createdAt") ?: ""
                //
                val studentId = getString("studentId", "student_id")
                val studyYear = getString("studyYear", "study_year")
                val optionCode = getString("optionCode", "option_code")

                StudentDTO(
                    id = null,
                    studentId = null,
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    password = password,
                    createdAt = createdAt
                )
            }
        )
    }
}