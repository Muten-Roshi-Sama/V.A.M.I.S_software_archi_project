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
import kotlinx.serialization.Serializable


//Exposed
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.exceptions.ExposedSQLException
import java.time.LocalDateTime

// ----------------------------

@Serializable
data class AdminCreateDTO(
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String,
    val password: String
//    val createdAt: String
)
@Serializable
data class AdminUpdateDTO(
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val password: String? = null
)


class AdminService(private val personService: PersonService = PersonService()) {

    /* Create a RoleService instance configured for Admin
    */
    private val ops = RoleService<AdminDTO, Admin>(
        personService = personService,
        findAll = { Admin.all() },
        findById = { id -> Admin.findById(id) },
        deleteById = { id -> Admin.findById(id)?.delete() != null },
        createForPerson = { person -> Admin.createForPerson(person) },
        toDto = { admin -> admin.toDto() },
        dtoToPersonCreate = { dto ->
            // AdminDTO -> PersonCreateDTO mapping (ensure password present)
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
    fun getAll(): List<AdminDTO> = ops.getAll()
    fun getById(adminId: Int): AdminDTO? = ops.getById(adminId)
    fun delete(adminId: Int): Boolean = ops.delete(adminId)
    fun count(): Long = ops.count()
    // fun existsByEmail(email: String): Boolean = ops.existsByEmail(email)

    

    /* Create admin from DTO, Inherited from PersonService.kt
     */
    fun create(createDto: AdminCreateDTO): AdminDTO = transaction {
        val emailField = createDto.email
        val passwordField = createDto.password

        // Validate FIELDS (TODO: PersonService.create already validates.....
        requireValidEmail(emailField)
        requireValidPassword(passwordField, minLength = 6)

        // Delegate existence check to PersonService (authoritative)
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
        // createdAt = createDto.createdAt ?: LocalDateTime.now().toString()
                )
            )

            val admin = Admin.createForPerson(person)
            admin.toDto()
        } catch (ex: ExposedSQLException) {
            val msg = ex.message ?: ""
            if (msg.contains("UNIQUE", true) || msg.contains("constraint", true)) {
                throw IllegalArgumentException("Email '$emailField' is already registered (unique constraint).")
            }
            throw ex
        }
    }

    fun update(adminId: Int, dto: AdminUpdateDTO): AdminDTO = transaction {
        val admin = Admin.findById(adminId) ?: throw IllegalArgumentException("not found")
        val person = admin.person

        // Email: delegate uniqueness check via PersonService (uses findByEmail)
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

        // Names
        dto.firstName?.let { person.firstName = it }
        dto.lastName?.let { person.lastName = it }

        // Password
        dto.password?.let { pw ->
            requireValidPassword(pw, minLength = 6)
            person.password = pw // TODO: hash
        }

        admin.toDto()
    }

    fun existsByEmail(email: String): Boolean = transaction {
        // Check if person exists AND has an Admin role record
        val person = personService.findByEmail(email) ?: return@transaction false
        Admin.find { AdminTable.person eq person.id }.firstOrNull() != null
    }

    // convert incoming AdminDTO (frontend) to AdminCreateDTO then call create()
    fun createAdminFromDto(dto: AdminDTO) {
        // Map AdminDTO -> AdminCreateDTO (create(...) expects AdminCreateDTO)
        val createDto = AdminCreateDTO(
            firstName = dto.firstName,
            lastName = dto.lastName,
            email = dto.email,
            password = dto.password ?: "" // fallback if password is null in seed
            // createdAt = dto.createdAt ?: LocalDateTime.now().toString()
        )
        // Use existing create(createDto) which returns AdminDTO
        create(createDto)
    }

    // ======== Seeding ===========

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





