package be.ecam.companion.data
//
//import be.ecam.common.api.AdminDTO
//import io.ktor.client.HttpClient
//import io.ktor.client.engine.mock.*
//import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
//import io.ktor.http.*
//import io.ktor.serialization.kotlinx.json.json
//import kotlinx.coroutines.test.runTest
//import kotlinx.serialization.json.Json
//import kotlin.test.Test
//import kotlin.test.assertEquals
//import kotlin.test.assertTrue
//
//class AdminCrudTest {
//
//    private fun createMockClient(handler: MockRequestHandleScope.(HttpRequestData) -> HttpResponseData): HttpClient {
//        return HttpClient(MockEngine) {
//            install(ContentNegotiation) {
//                json(Json {
//                    ignoreUnknownKeys = true
//                    prettyPrint = true
//                })
//            }
//            engine {
//                addHandler(handler)
//            }
//        }
//    }
//
//    @Test
//    fun testLoginAndFetchAdmins() = runTest {
//        val mockClient = createMockClient { request ->
//            when (request.url.encodedPath) {
//                "/auth/login" -> respond(
//                    content = """{"accessToken":"mock-token","tokenType":"Bearer"}""",
//                    status = HttpStatusCode.OK,
//                    headers = headersOf(HttpHeaders.ContentType, "application/json")
//                )
//                "/crud/admins" -> {
//                    val authHeader = request.headers[HttpHeaders.Authorization]
//                    if (authHeader == "Bearer mock-token") {
//                        respond(
//                            content = """[
//                                {"id":1,"firstName":"Admin","lastName":"One","email":"admin1@admin.com","createdAt":"2025-12-05T13:36:36Z"},
//                                {"id":2,"firstName":"Admin","lastName":"Two","email":"admin2@admin.com","createdAt":"2025-12-05T13:36:37Z"}
//                            ]""",
//                            status = HttpStatusCode.OK,
//                            headers = headersOf(HttpHeaders.ContentType, "application/json")
//                        )
//                    } else {
//                        respond(
//                            content = """{"error":"unauthorized"}""",
//                            status = HttpStatusCode.Unauthorized,
//                            headers = headersOf(HttpHeaders.ContentType, "application/json")
//                        )
//                    }
//                }
//                else -> respond(
//                    content = """{"error":"not found"}""",
//                    status = HttpStatusCode.NotFound,
//                    headers = headersOf(HttpHeaders.ContentType, "application/json")
//                )
//            }
//        }
//
//        val repo = KtorApiRepository(mockClient) { "http://localhost:8080" }
//
//        // Test login
//        val loginResponse = repo.login("admin1@admin.com", "pass123")
//        assertEquals("mock-token", loginResponse.accessToken)
//        assertTrue(repo.isAuthenticated())
//
//        // Test fetch admins
//        val admins = repo.fetchAdmins()
//        assertEquals(2, admins.size)
//        assertEquals("admin1@admin.com", admins[0].email)
//    }
//
//    @Test
//    fun testCreateAdmin() = runTest {
//        val mockClient = createMockClient { request ->
//            when {
//                request.url.encodedPath == "/auth/login" -> respond(
//                    content = """{"accessToken":"mock-token","tokenType":"Bearer"}""",
//                    status = HttpStatusCode.OK,
//                    headers = headersOf(HttpHeaders.ContentType, "application/json")
//                )
//                request.url.encodedPath == "/crud/admins" && request.method == HttpMethod.Post -> respond(
//                    content = """{"id":3,"firstName":"Test","lastName":"Admin","email":"test@admin.com","createdAt":"2025-12-05T13:48:59Z"}""",
//                    status = HttpStatusCode.Created,
//                    headers = headersOf(HttpHeaders.ContentType, "application/json")
//                )
//                else -> respond(
//                    content = """{"error":"not found"}""",
//                    status = HttpStatusCode.NotFound,
//                    headers = headersOf(HttpHeaders.ContentType, "application/json")
//                )
//            }
//        }
//
//        val repo = KtorApiRepository(mockClient) { "http://localhost:8080" }
//
//        repo.login("admin1@admin.com", "pass123")
//
//        val newAdmin = AdminDTO(
//            firstName = "Test",
//            lastName = "Admin",
//            email = "test@admin.com",
//            password = "testpass"
//        )
//
//        val created = repo.createAdmin(newAdmin)
//        assertEquals(3, created.id)
//        assertEquals("test@admin.com", created.email)
//    }
//}