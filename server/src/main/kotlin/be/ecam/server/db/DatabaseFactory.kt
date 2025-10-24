package be.ecam.server.db

import org.jetbrains.exposed.sql.Database    // Imports Exposed’s Database object which manages the JDBC connection for Exposed (the JetBrains SQL library).

// DAO Schema
import be.ecam.server.models.AdminTable

// DAO Class
import be.ecam.server.models.Admin


import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File



object DatabaseFactory {
    fun connect() {

        val dbFolder = File("data")
        if (!dbFolder.exists()){
            dbFolder.mkdirs()
            println("Created data directory")
        }

        // Get DB file path
        val dbPath = File(dbFolder, "sqlite.db").absolutePath


        Database.connect("jdbc:sqlite:$dbPath", driver = "org.sqlite.JDBC")
        println("Connected to SQLite database, path is : $dbPath")

//        transaction {
////            SchemaUtils.createMissingTablesAndColumns(AdminTable)
//            SchemaUtils.create(AdminTable)
//
//        }

    }

    fun initDb() {
        initMockData()

    }


// ===========================================================================
    private fun initMockData() {


        transaction {
            SchemaUtils.create(AdminTable)
            println("✅ AdminTable created.")

            if (Admin.all().empty()) {
                Admin.new {
                    username = "admin"
                    password = "1234"
                    email = "admin@example.com"
                }
                println("✅ Mock admin created.")
            } else {
                println("ℹ️ Admin table already contains data.")
            }


            // ✅ DEBUG: Print all admins in DB
            println("=== Current Admins in DB ===")
            for (admin in Admin.all()) {
                println("ID=${admin.id.value} | username=${admin.username} | email=${admin.email}")
            }
            println("============================")


        }


//        val file = File("server/src/main/resources/data/admin.json")
//        if (!file.exists()) {
//            println("⚠️ No mock data file found at ${file.path}")
//            return
//        }
//
//
//
//
//        val jsonString = file.readText()
//        val admins = Json.decodeFromString<List<AdminDTO>>(jsonString)
//        val service = AdminService()



//        val existing = service.getAll()
//        if (existing.isEmpty()) {
//            admins.forEach {
//                service.insert(
//                    be.ecam.server.models.Admin(
//                        username = it.username,
//                        password = it.password,
//                        email = it.email
//                    )
//                )
//            }
//            println("✅ Inserted ${admins.size} mock admins.")
//        } else {
//            println("ℹ️ Admins table already populated.")
//        }
    }

    @kotlinx.serialization.Serializable
    private data class AdminDTO(
        val username: String,
        val password: String,
        val email: String
    )

}
