package be.ecam.server.services

import be.ecam.common.api.AdminDTO
import be.ecam.server.models.Admin
import be.ecam.server.models.AdminTable
import be.ecam.server.models.PersonTable
import be.ecam.server.testutils.TestDatabase
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * Tests generic RoleService behavior using the Admin role as a concrete example.
 *
 * Exercises:
 * - createFromDto (maps DTO -> PersonCreateDTO -> create Person -> create role)
 * - getAll, getById
 * - existsByEmail (delegates to PersonService)
 * - delete, count
 *
 * This uses the TestDatabase base which provides an isolated in-memory SQLite DB
 * and creates/drops the tables declared in `override val tables`.
 */
class RoleServiceTest : TestDatabase() {

    override val tables: Array<Table> = arrayOf(PersonTable, AdminTable)

    private lateinit var personService: PersonService
    private lateinit var roleService: RoleService<AdminDTO, Admin>

    @BeforeTest
    override fun setupDatabase() {
        super.setupDatabase()

        // real PersonService is used so RoleService integrates with the same person lifecycle.
        personService = PersonService()

        // Create the RoleService wiring for Admin -> AdminDTO
        roleService = RoleService(
            personService = personService,
            findAll = { Admin.all().toList() },
            findById = { id: Int -> Admin.findById(id) },
            deleteById = { id: Int ->
                Admin.findById(id)?.let {
                    it.delete()
                    true
                } ?: false
            },
            createForPerson = { person -> Admin.createForPerson(person) },
            toDto = { admin: Admin -> admin.toDto() },
            dtoToPersonCreate = { dto: AdminDTO ->
                PersonCreateDTO(
                    firstName = dto.firstName ?: "",
                    lastName = dto.lastName ?: "",
                    email = dto.email,
                    password = dto.password ?: "fallback-pass",
                    createdAt = dto.createdAt
                )
            },
            dtoEmail = { dto -> dto.email }
        )
    }

    @Test
    fun `createFromDto persists person and role and existsByEmail is true`() {
        val dto = AdminDTO(
            id = null,
            firstName = "Role",
            lastName = "Tester",
            email = "role.tester@example.com",
            password = "TestPass123"
//            createdAt = null
        )

        val created = roleService.createFromDto(dto)

        // returned DTO should have non-null id (if your toDto sets it) or at least the email matches
        assertNotNull(created, "createFromDto should return a DTO")
        assertEquals(dto.email, created.email)

        // person exists (via PersonService)
        assertTrue(personService.existsByEmail(dto.email))

        // role count is 1
        assertEquals(1L, roleService.count())
    }

    @Test
    fun `getAll and getById and delete behave as expected`() {
        // Create two admins via roleService
        val a1 = AdminDTO(null, "A", "One", "a1@example.com", "Password1", null)
        val a2 = AdminDTO(null, "B", "Two", "b2@example.com", "Password2", null)

        val created1 = roleService.createFromDto(a1)
        val created2 = roleService.createFromDto(a2)

        // getAll should return at least the two created admins
        val all = roleService.getAll()
        assertTrue(all.any { it.email == "a1@example.com" })
        assertTrue(all.any { it.email == "b2@example.com" })

        // get a DAO instance safely (returns first admin or null)
        val found = transaction {
            Admin.all().firstOrNull()
        }
        assertNotNull(found)

        // Find the Admin id to delete by looking up the Person by email, then the Admin by person id
        val idToDelete = transaction {
            val admin = Admin.all().first { it.person.email == "a1@example.com" }
            admin.id.value
        }

        val deleted = roleService.delete(idToDelete)
        assertTrue(deleted)

        // After deletion, count decreased
        val c = roleService.count()
        assertEquals(1L, c)
    }

    @Test
    fun `existsByEmail returns false when not present`() {
        assertFalse(roleService.existsByEmail("no.such.user@example.com"))
    }
}