package be.ecam.server.db

/**
 * Very small registry of seed tasks. Tasks are ordered by registration.
 * Each task is just a name + ()->Unit.
 */
object SeedManager {
    private val tasks = mutableListOf<Pair<String, () -> Unit>>()

    fun clear() = tasks.clear()

    fun register(name: String, task: () -> Unit) {
        tasks.add(name to task)
    }

    fun seedAll() {
        tasks.forEach { (name, task) ->
            try {
                println("➡️ Seeding: $name")
                task()
                println("✅ Seeded: $name")
            } catch (ex: Exception) {
                println("❌ Seeding failed for $name: ${ex.message}")
                throw ex
            }
        }
    }
}