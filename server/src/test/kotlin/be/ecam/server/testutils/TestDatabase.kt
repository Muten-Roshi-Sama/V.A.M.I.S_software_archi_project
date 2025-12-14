package be.ecam.server.testutils

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.sqlite.SQLiteDataSource
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import java.sql.Connection
import java.util.UUID

/**
 * Base test class that sets up an in-memory (file:...;cache=shared) SQLite DB per test class.
 * Subclasses should override [tables] to list the Exposed tables to create/drop.
 *
 * Usage:
 *  - class PersonServiceTest : TestDatabase() { ... }
 */
open class TestDatabase {

    protected lateinit var dataSource: SQLiteDataSource
    protected lateinit var keeperConnection: Connection

    protected open val tables: Array<Table>
        get() = arrayOf() // override in subclasses

    // Use a unique DB name per class to avoid interference
    private fun uniqueDbName(): String = "tests_${this::class.simpleName ?: UUID.randomUUID().toString().replace("-", "")}"

    @BeforeTest
    open fun setupDatabase() {
        val jdbcUrl = "jdbc:sqlite:file:${uniqueDbName()}?mode=memory&cache=shared&foreign_keys=true"
        dataSource = SQLiteDataSource().apply { url = jdbcUrl }

        // Keep one persistent connection open so the in-memory database survives across other connections
        keeperConnection = dataSource.connection

        // Give Exposed the DataSource so it can open/close connections safely
        Database.connect(dataSource)

        // Set stable isolation for SQLite
        TransactionManager.manager.defaultIsolationLevel = java.sql.Connection.TRANSACTION_SERIALIZABLE

        // Create schema (safe to call even if tables array is empty)
        if (tables.isNotEmpty()) {
            transaction {
                SchemaUtils.create(*tables)
            }
        }
    }

    @AfterTest
    fun teardownDatabase() {
        if (tables.isNotEmpty()) {
            transaction {
                SchemaUtils.drop(*tables)
            }
        }
        // Close the keeper to release resources
        if (::keeperConnection.isInitialized && !keeperConnection.isClosed) {
            keeperConnection.close()
        }
    }

    protected fun <T> dbTransaction(block: () -> T): T = transaction { block() }
}