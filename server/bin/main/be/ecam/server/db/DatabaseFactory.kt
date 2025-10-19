package be.ecam.server.db

import org.jetbrains.exposed.sql.Database    // Imports Exposed’s Database object which manages the JDBC connection for Exposed (the JetBrains SQL library).
import be.ecam.server.models.AdminTable
import be.ecam.server.services.AdminService
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File



object DatabaseFactory {
    fun init() {
        Database.connect("jdbc:sqlite:data/school.db", driver = "org.sqlite.JDBC")
        println("Connected to SQLite database.")

        transaction {
//            SchemaUtils.createMissingTablesAndColumns(AdminTable)
            SchemaUtils.create(AdminTable)
        }

        initMockData()


    }

    fun initDb() {
//        transaction {
//            SchemaUtils.create(Students)
//        }
    }

    private fun initMockData() {
        val file = File("server/src/main/resources/data/admin.json")
        if (!file.exists()) {
            println("⚠️ No mock data file found at ${file.path}")
            return
        }

        val jsonString = file.readText()
        val admins = Json.decodeFromString<List<AdminDTO>>(jsonString)
        val service = AdminService()

        val existing = service.getAll()
        if (existing.isEmpty()) {
            admins.forEach {
                service.insert(
                    be.ecam.server.models.Admin(
                        username = it.username,
                        password = it.password,
                        email = it.email
                    )
                )
            }
            println("✅ Inserted ${admins.size} mock admins.")
        } else {
            println("ℹ️ Admins table already populated.")
        }
    }

    @kotlinx.serialization.Serializable
    private data class AdminDTO(
        val username: String,
        val password: String,
        val email: String
    )

}
