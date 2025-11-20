package be.ecam.server.routes

// Ktor
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.http.content.*
// Java
import java.io.File
import java.nio.file.Paths

/**
 * Handles static WASM files and favicon serving.
 */
fun Application.configureStaticRoutes() {
    routing {

        // --- WASM Static Files ---
        val wasmOut = resolveWasmOutputDir()
        println("Working dir: ${System.getProperty("user.dir")} | Using WASM dir: ${wasmOut?.absolutePath ?: "<not found>"}")

        if (wasmOut != null) {
            staticFiles("/", wasmOut, index = "index.html")
        }

        // --- Favicons ---
        get("/favicon.png") { call.respondFavIcon() }
        get("/favicon.ico") { call.respondFavIcon() }
    }
}

/**
 * Resolves where the WASM build files are located.
 */
private fun resolveWasmOutputDir(): File? {
    fun wasmCandidates(): List<File> {
        val override = System.getProperty("wasm.dir")?.trim().takeUnless { it.isNullOrEmpty() }
            ?: System.getenv("WASM_DIR")?.trim().takeUnless { it.isNullOrEmpty() }

        val paths = mutableListOf<String>()
        if (override != null) paths += override

        // Project-root relative (works if working dir is repo root)
        paths += listOf(
            "composeApp/build/dist/wasmJs/productionExecutable",
            "composeApp/build/dist/wasmJs/developmentExecutable"
        )
        // Server-module relative (works if working dir is repoRoot/server)
        paths += listOf(
            "../composeApp/build/dist/wasmJs/productionExecutable",
            "../composeApp/build/dist/wasmJs/developmentExecutable"
        )

        return paths.map { Paths.get(it).toFile() }
    }

    return wasmCandidates().firstOrNull { it.exists() }
}

/**
 * Responds with favicon.png if found, else 404.
 */
suspend fun ApplicationCall.respondFavIcon() {
    val bytes = this::class.java.classLoader.getResourceAsStream("favicon.png")?.readBytes()
    if (bytes == null) {
        respondText("Not found", status = HttpStatusCode.NotFound)
    } else {
        respondBytes(bytes, contentType = ContentType.Image.PNG)
    }
}


