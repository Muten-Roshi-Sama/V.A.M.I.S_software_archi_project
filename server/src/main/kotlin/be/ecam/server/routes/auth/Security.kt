package be.ecam.server.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.installJwtAuth(secret: String, issuer: String, audience: String) {
    val algorithm = Algorithm.HMAC256(secret)
    install(Authentication) {
        jwt("auth-jwt") {
            realm = "ecam"
            verifier(
                JWT.require(algorithm)
                    .withIssuer(issuer)
                    .withAudience(audience)
                    .build()
            )
            validate { credential ->
                val idClaim = credential.payload.getClaim("id")
                if (!idClaim.isNull) JWTPrincipal(credential.payload) else null
            }
        }
    }
}

/** Create a signed JWT (use only for tests or login endpoints). */
fun createToken(secret: String, issuer: String, audience: String, userId: Int, role: String, expiresMs: Long = 60 * 60 * 1000L): String {
    val algorithm = Algorithm.HMAC256(secret)
    return JWT.create()
        .withIssuer(issuer)
        .withAudience(audience)
        .withClaim("id", userId)
        .withClaim("role", role)
        .withExpiresAt(java.util.Date(System.currentTimeMillis() + expiresMs))
        .sign(algorithm)
}