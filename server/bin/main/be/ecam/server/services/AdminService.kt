package be.ecam.server.services

import be.ecam.server.models.AdminTable
import be.ecam.server.models.Admin
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction



class AdminService {
//    fun insert(admin: Admin): Admin {
////        val id = transaction {
////            Admins.insert {
////                it[username] = admin.username
////                it[password] = admin.password
////                it[email] = admin.email
////            } get Admins.id
////        }
//        return false
//    }

//    fun getAll(): List<Admin> = transaction {
////        Admins.selectAll().map {
////            Admin(
////                id = it[Admins.id],
////                username = it[Admins.username],
////                password = it[Admins.password],
////                email = it[Admins.email]
////            )
//        }
//    }
}
