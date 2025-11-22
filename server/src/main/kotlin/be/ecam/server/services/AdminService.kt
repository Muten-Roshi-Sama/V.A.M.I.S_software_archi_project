package be.ecam.server.services



//DAO
import be.ecam.server.models.Person
import be.ecam.server.models.Admin
import be.ecam.server.models.AdminTable

//DTO
import be.ecam.common.api.AdminDTO

//SeedManager
import be.ecam.server.db.SeedResult
import be.ecam.server.db.seedFromResourceIfMissing

// util
import be.ecam.server.util.requireValidEmail
import be.ecam.server.util.requireValidPassword


//Exposed
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

// ----------------------------

data class AdminCreateDTO(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val createdAt: String
)
class AdminService {

    // Get all admins as DTOs (for API)
    fun getAll(): List<AdminDTO> = transaction {
        Admin.all().map { it.toDto() }
    }

    // Create admin from DTO
    fun create(createDto: AdminCreateDTO): AdminDTO = transaction {

        // REQUIRED Fields
        requireValidEmail(createDto.email)
        requireValidPassword(createDto.password, )
//        val password_field = createDto.password ?: throw IllegalArgumentException("Password is required to create an admin.")
//        val email_field = createDto.email ?: throw IllegalArgumentException("Email is required to create an admin.")


        // TODO: check if email doesnt already exits


        val person = Person.new {
            firstName = createDto.firstName
            lastName = createDto.lastName
            email = createDto.email
            password = createDto.password
            createdAt = createDto.createdAt
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
            password = dto.password ?: "", // fallback if password is null in seed
            createdAt = dto.createdAt ?: LocalDateTime.now().toString()
        )
        // Use existing create(createDto) which returns AdminDTO
        create(createDto)
    }

    // idempotent seeder entrypoint (called from DatabaseFactory)
    fun seedFromResource(resourcePath: String = "data/admin.json"): SeedResult {
        return seedFromResourceIfMissing<AdminDTO>(
            name = "admins",
            resourcePath = resourcePath,
            // exists: skip seeding when an admin with same email exists
            exists = { dto: AdminDTO -> existsByEmail(dto.email) },
            // create: convert AdminDTO -> AdminCreateDTO and persist
            create = { dto: AdminDTO -> createAdminFromDto(dto) },
            // legacyMapper: handle legacy JSON objects like {"username":"john.doe","password":"p","email":"x"}
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
                val password = getString("password") // may be null - createAdminFromDto will enforce presence
                val createdAt = getString("createdAt") ?: ""

                AdminDTO(
                    id = null,
                    firstName = firstName,
                    lastName  = lastName,
                    email     = email,
                    password  = password,   // may be null here; createAdminFromDto will check
                    createdAt = createdAt
                )
            }
        )
    }


}





