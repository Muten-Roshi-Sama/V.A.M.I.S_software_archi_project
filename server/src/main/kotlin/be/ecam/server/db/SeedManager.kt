package be.ecam.server.db

/**
 * Very small registry of seed tasks. Tasks are ordered by registration.
 * Each task is just a name + ()->Unit.
 */
object SeedManager {
    private val tasks = linkedMapOf<String, () -> SeedResult>()

    fun clear() { tasks.clear() }

    fun register(name: String, task: () -> SeedResult) {
        tasks[name] = task
    }

    fun seedAll(): List<Pair<String, SeedResult>> {
        val results = mutableListOf<Pair<String, SeedResult>>()
        tasks.forEach { (name, task) ->
            println("➡️ Seeding: $name")
            val result = try {
                task()
            } catch (ex: Exception) {
                // create a failure SeedResult — adapt constructor to your SeedResult type if needed
                println("❌ Seeder threw: $name -> ${ex.message}")
                SeedResult(name = name, inserted = 0, skipped = 0, errors = listOf("Seeder threw exception: ${ex.message}"))
            }
            val summary = "inserted=${result.inserted}, skipped=${result.skipped}, errors=${result.errors.size}"
            if (result.errors.isNotEmpty()) {
                result.errors.forEach { err -> println("   • $name error: $err") }
            }
            results += (name to result)
        }
        return results
    }
}