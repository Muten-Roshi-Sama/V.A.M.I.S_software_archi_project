package be.ecam.server.routes

import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

import be.ecam.server.db.DatabaseFactory
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.selectAll



// Login credentials
object TestLogin {
    val adminEmail: String = System.getProperty("TEST_ADMIN_EMAIL") ?: "TESTADMIN@admin.com"
    val adminPassword: String = System.getProperty("TEST_ADMIN_PASSWORD") ?: "pass123"
    val studentEmail: String = System.getProperty("TEST_STUDENT_EMAIL") ?: "TESTSTUDENT@student.com"
    val studentPassword: String = System.getProperty("TEST_STUDENT_PASSWORD") ?: "pass123"
    val teacherEmail: String = System.getProperty("TEST_TEACHER_EMAIL") ?: "TESTTEACHER@teacher.com"
    val teacherPassword: String = System.getProperty("TEST_TEACHER_PASSWORD") ?: "pass123"
}

// Central Database Setup/Teardown for Tests
object TestDatabaseSetup {
    private var isInitialized = false

    fun initialize() {
        if (isInitialized) return

        println("🔧 TestDatabaseSetup: Initializing test database...")
        
        // 1. Connect to test DB
        DatabaseFactory.connect()
        println("✅ TestDatabaseSetup: Database connected")

        // 2. Clean and recreate schema
        try {
            DatabaseFactory.cleanDb()
            println("✅ TestDatabaseSetup: Database cleaned")
        } catch (e: Exception) {
            println("⚠️ TestDatabaseSetup: Clean failed (may be first run): ${e.message}")
        }

        // 3. Create tables
        DatabaseFactory.createAllTables()
        println("✅ TestDatabaseSetup: Tables created")

        // 4. Seed data
        DatabaseFactory.initDb()
        println("✅ TestDatabaseSetup: Data seeded")

        // 5. Verify seeds exist
        verifySeedsLoaded()
        
        isInitialized = true
        println("🎯 TestDatabaseSetup: Complete\n")
    }

    private fun verifySeedsLoaded() {
        transaction {
            try {
                val adminCount = be.ecam.server.models.AdminTable.selectAll().count()
                val studentCount = be.ecam.server.models.StudentTable.selectAll().count()
                val teacherCount = be.ecam.server.models.TeacherTable.selectAll().count()
                
                println("📊 TestDatabaseSetup: Seed verification:")
                println("   - Admins: $adminCount")
                println("   - Students: $studentCount")
                println("   - Teachers: $teacherCount")
                
                if (adminCount == 0L || studentCount == 0L || teacherCount == 0L) {
                    error("TestDatabaseSetup: Seeds missing! Check data/*.json files and working directory.")
                }
            } catch (e: Exception) {
                println("⚠️ TestDatabaseSetup: Could not verify seeds: ${e.message}")
                throw e
            }
        }
    }
}

// Helpers
private fun extractToken(json: String): String {
    return json.substringAfter("\"accessToken\":\"").substringBefore('"')
}

fun extractId(json: String): Int {
    return json.substringAfter("\"id\":").substringBefore(',').trim().toInt()
}



// Central Login 
suspend fun loginAdmin(client: HttpClient): String {
    TestDatabaseSetup.initialize()
    
    transaction {
        val admins = be.ecam.server.models.Admin.all().map { it.person.email to it.person.password }
        println("DEBUG admins in DB: $admins")
    }


    val res = client.post("/auth/login") {
        contentType(ContentType.Application.Json)
        setBody("""{"email":"${TestLogin.adminEmail}","password":"${TestLogin.adminPassword}"}""")
    }
    println("🔐 loginAdmin response status: ${res.status}")
    println("🔐 loginAdmin response body: ${res.bodyAsText()}")

    if (!res.status.isSuccess()) {
        error("admin login failed: ${res.status} body=${res.bodyAsText()}")
    }
    return extractToken(res.bodyAsText())
}

suspend fun loginStudent(client: HttpClient): String {
    TestDatabaseSetup.initialize()
    
    // transaction {
    //     val students = be.ecam.server.models.Student.all().map { it.person.email to it.person.password }
    //     println("DEBUG students in DB: $students")
    // }

    val res = client.post("/auth/login") {
        contentType(ContentType.Application.Json)
        setBody("""{"email":"${TestLogin.studentEmail}","password":"${TestLogin.studentPassword}"}""")
    }
    if (!res.status.isSuccess()) {
        error("student login failed: ${res.status} body=${res.bodyAsText()}")
    }
    return extractToken(res.bodyAsText())
}

suspend fun loginTeacher(client: HttpClient): String {
    TestDatabaseSetup.initialize()  // ← ADD THIS
    
    val res = client.post("/auth/login") {
        contentType(ContentType.Application.Json)
        setBody("""{"email":"${TestLogin.teacherEmail}","password":"${TestLogin.teacherPassword}"}""")
    }
    if (!res.status.isSuccess()) {
        error("teacher login failed: ${res.status} body=${res.bodyAsText()}")
    }
    return extractToken(res.bodyAsText())
}

