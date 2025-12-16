package be.ecam.server.services

import be.ecam.server.models.Person
import org.jetbrains.exposed.sql.transactions.transaction
import be.ecam.server.db.SeedResult
import be.ecam.server.db.seedFromResourceIfMissing

/**
 * Generic role helper to implement common role operations.
 *
 * TDto: role DTO type (e.g. AdminDTO)
 * TRole: role DAO type (e.g. Admin : IntEntity)
 *
 * Instead of forcing a specific DTO shape, RoleService requires small lambdas:
 * - findAll / findById / deleteById / createForPerson / toDto / dtoToPersonCreate
 *
 * Important: role DAO lambdas are executed inside RoleService transactions.
 */
class RoleService<TDto, TRole>(
    private val personService: PersonService,

    // Role DAO operations (should operate on Exposed DAOs)
    private val findAll: () -> Iterable<TRole>,
    private val findById: (Int) -> TRole?,
    private val deleteById: (Int) -> Boolean,
    private val createForPerson: (Person) -> TRole,
    private val toDto: (TRole) -> TDto,

    // Map a DTO -> PersonCreateDTO for creating the Person
    private val dtoToPersonCreate: (TDto) -> PersonCreateDTO,

    // Extract an identifying email from the DTO (used by seeding)
    private val dtoEmail: (TDto) -> String
) {

    fun getAll(): List<TDto> = transaction {
        findAll().map { toDto(it) }
    }

    fun getById(roleId: Int): TDto? = transaction {
        findById(roleId)?.let { toDto(it) }
    }

    fun delete(roleId: Int): Boolean = transaction {
        deleteById(roleId)
    }

    fun count(): Long = transaction {
        // findAll may be able to provide a more efficient count; this is generic
        findAll().count().toLong()
    }

    fun existsByEmail(email: String): Boolean = personService.existsByEmail(email)

    /**
     * Create from DTO: converts to PersonCreateDTO, creates Person via PersonService,
     * then attaches the role row and returns the role DTO.
     */
    fun createFromDto(dto: TDto): TDto = transaction {
        val personCreate = dtoToPersonCreate(dto)
        val person = personService.create(personCreate)
        val role = createForPerson(person)
        toDto(role)
    }
}