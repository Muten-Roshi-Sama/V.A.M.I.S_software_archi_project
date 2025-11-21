package be.ecam.server.services



//DAO
import be.ecam.server.models.Person
import be.ecam.server.models.Admin
import be.ecam.server.models.AdminTable
import be.ecam.server.db.seedFromResourceIfMissing

//DTO
import be.ecam.common.api.AdminDTO

//Exposed
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

// ----------------------------

data class AdminCreateDTO(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String
)
class AdminService {

    // Get all admins as DTOs (for API)
    fun getAll(): List<AdminDTO> = transaction {
        Admin.all().map { it.toDto() }
    }

    // Create admin from DTO
    fun create(createDto: AdminCreateDTO): AdminDTO = transaction {
        // Optional: validate email uniqueness here
        val person = Person.new {
            firstName = createDto.firstName
            lastName = createDto.lastName
            email = createDto.email
            // per your request: keep simple password storage
            password = createDto.password
        }

        val admin = Admin.createForPerson(person)
        admin.toDto()
    }

    // Get by ID
    fun getById(id: Int): AdminDTO? = transaction {
        Admin.findById(id)?.toDto()
    }

    // Delete admin (deletes Admin row; Person will cascade because of FK ON DELETE CASCADE)
    fun delete(id: Int): Boolean = transaction {
        Admin.findById(id)?.delete() != null
    }

    // ======== Seeding ===========

    // fast count helper
    fun count(): Long = transaction { be.ecam.server.models.Admin.all().count() }

    // fast existence check by email
    fun existsByEmail(email: String): Boolean = transaction {
        be.ecam.server.models.Person.find { be.ecam.server.models.PersonTable.email eq email }.empty().not()
    }

    // create wrapper: convert incoming AdminDTO (seed shape) to AdminCreateDTO then call create()
    fun createAdminFromDto(dto: AdminDTO) {
        // Map AdminDTO -> AdminCreateDTO (create(...) expects AdminCreateDTO)
        val createDto = AdminCreateDTO(
            firstName = dto.firstName,
            lastName = dto.lastName,
            email = dto.email,
            password = dto.password ?: "" // fallback if password is null in seed
        )
        // Use existing create(createDto) which returns AdminDTO
        create(createDto)
    }

    // idempotent seeder entrypoint (called from DatabaseFactory)
    fun seedFromResource(resourcePath: String = "data/admin.json") {
        seedFromResourceIfMissing<AdminDTO>(
            resourcePath = resourcePath,
            // explicitly type the lambda parameter to avoid inference problems
            exists = { dto: AdminDTO -> existsByEmail(dto.email) },
            create = { dto: AdminDTO -> createAdminFromDto(dto) }
        )
    }


}





