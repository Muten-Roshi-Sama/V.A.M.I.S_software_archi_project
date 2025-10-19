package be.ecam.server.db

import org.jetbrains.exposed.sql.Database

object DatabaseFactory {
    fun init() {
        Database.connect("jdbc:sqlite:data/school.db", driver = "org.sqlite.JDBC")
        println("Connected to SQLite database.")
    }
}
