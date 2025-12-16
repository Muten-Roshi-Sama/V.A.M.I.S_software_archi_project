package be.ecam.server.services

import be.ecam.server.models.*
import be.ecam.common.api.TeacherDTO
import be.ecam.server.db.SeedResult
import be.ecam.server.db.seedFromResourceIfMissing
import be.ecam.server.util.requireValidEmail
import be.ecam.server.util.requireValidPassword
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
data class TeacherCreateDTO(
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String,
    val password: String,
    val teacherId: Int? = null,
    val createdAt: String? = null
)

@Serializable
data class TeacherUpdateDTO(
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val password: String? = null,
    val teacherId: Int? = null
)

class TeacherService(private val personService: PersonService = PersonService()) {
    private val ops = RoleService<TeacherDTO, Teacher>(
        personService = personService,
        findAll = { Teacher.all() },
        findById = { id -> Teacher.findById(id) },
        deleteById = { id -> Teacher.findById(id)?.delete() != null },
        createForPerson = { person -> Teacher.createForPerson(person) },
        toDto = { it.toDto() },
        dtoToPersonCreate = { dto ->
            PersonCreateDTO(
                firstName = dto.firstName,
                lastName = dto.lastName,
                email = dto.email,
                password = dto.password ?: throw IllegalArgumentException("password required for creation"),
                createdAt = dto.createdAt
            )
        },
        dtoEmail = { it.email }
    )

    fun getAll(): List<TeacherDTO> = ops.getAll()
    fun getById(id: Int): TeacherDTO? = ops.getById(id)
    fun delete(id: Int): Boolean = ops.delete(id)
    fun count(): Long = ops.count()
    
    fun getAllTeachers(): List<TeacherDTO> = getAll()
    
    fun getTeacherByEmail(email: String): TeacherDTO? = transaction {
        val person = Person.find { PersonTable.email eq email }.firstOrNull() ?: return@transaction null
        val teacher = Teacher.find { TeacherTable.person eq person.id }.firstOrNull() ?: return@transaction null
        teacher.toDto()
    }
    
    fun findTeachersByName(nameQuery: String): List<TeacherDTO> = transaction {
        val lowerQuery = nameQuery.lowercase()
        Teacher.all().filter { teacher ->
            val firstName = teacher.firstName?.lowercase() ?: ""
            val lastName = teacher.lastName?.lowercase() ?: ""
            firstName.contains(lowerQuery) || lastName.contains(lowerQuery)
        }.map { it.toDto() }
    }

    fun create(dto: TeacherCreateDTO): TeacherDTO = transaction {
        val email = dto.email
        val password = dto.password
        requireValidEmail(email)
        requireValidPassword(password, minLength = 6)

        if (personService.existsByEmail(email)) {
            throw IllegalArgumentException("Email '$email' is already registered")
        }

        try {
            val person = personService.create(
                PersonCreateDTO(
                    firstName = dto.firstName,
                    lastName = dto.lastName,
                    email = email,
                    password = password,
                    createdAt = dto.createdAt
                )
            )
            val teacher = Teacher.createForPerson(person)
            teacher.teacherId = dto.teacherId
            teacher.toDto()
        } catch (ex: ExposedSQLException) {
            val msg = ex.message ?: ""
            if (msg.contains("UNIQUE", true) || msg.contains("constraint", true)) {
                throw IllegalArgumentException("Email '$email' is already registered (unique constraint).")
            }
            throw ex
        }
    }

    fun update(id: Int, dto: TeacherUpdateDTO): TeacherDTO = transaction {
        val teacher = Teacher.findById(id) ?: throw IllegalArgumentException("not found")
        val person = teacher.person

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
            person.password = pw
        }
        dto.teacherId?.let { teacher.teacherId = it }

        teacher.toDto()
    }

    fun existsByEmail(email: String): Boolean = transaction {
        val person = personService.findByEmail(email) ?: return@transaction false
        Teacher.find { TeacherTable.person eq person.id }.firstOrNull() != null
    }

    fun getByPersonId(personId: Int): TeacherDTO? = transaction {
        Teacher.find { TeacherTable.person eq EntityID(personId, PersonTable) }
            .firstOrNull()
            ?.toDto()
    }

    fun createTeacherFromDto(dto: TeacherDTO) {
        val createDto = TeacherCreateDTO(
            firstName = dto.firstName,
            lastName = dto.lastName,
            email = dto.email,
            password = dto.password ?: "",
            teacherId = dto.teacherId,
            createdAt = dto.createdAt
        )
        create(createDto)
    }

    fun seedFromResource(resourcePath: String = "data/teacher.json"): SeedResult {
        return seedFromResourceIfMissing<TeacherDTO>(
            name = "teachers",
            resourcePath = resourcePath,
            exists = { dto -> existsByEmail(dto.email) },
            create = { dto -> createTeacherFromDto(dto) },
            legacyMapper = { map ->
                fun getString(vararg keys: String): String? {
                    for (k in keys) {
                        val v = map[k] ?: map[k.lowercase()]
                        if (v is String) return v
                        if (v != null) return v.toString()
                    }
                    return null
                }
                fun getInt(vararg keys: String): Int? {
                    for (k in keys) {
                        val v = map[k] ?: map[k.lowercase()]
                        if (v is Int) return v
                        if (v is Number) return v.toInt()
                        if (v is String) return v.toIntOrNull()
                    }
                    return null
                }
                TeacherDTO(
                    id = null,
                    firstName = getString("firstName") ?: "",
                    lastName = getString("lastName") ?: "",
                    email = getString("email") ?: "",
                    password = getString("password"),
                    createdAt = getString("createdAt") ?: "",
                    teacherId = getInt("teacherId", "teacher_id")
                )
            }
        )
    }
}