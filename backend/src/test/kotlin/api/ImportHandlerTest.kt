package api

import api.usecases.ImportData
import io.javalin.Javalin
import io.mockk.*
import org.eclipse.jetty.http.HttpStatus
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ImportHandlerTest {
    private lateinit var mockServer: Javalin
    private val importUseCase = mockk<ImportData>(relaxed = true)

    @BeforeAll
    @Suppress("unused")
    fun setup() {
        mockServer = Javalin.create()
            .get("/", ImportHandler(importUseCase))
            .start(1234)
    }

    @AfterEach
    fun `after each`() = clearAllMocks()

    @AfterAll
    @Suppress("unused")
    fun `tear down`() {
        mockServer.stop()
    }

    @Test
    fun `should return 201 and when use case goes smoothly`() {
        justRun { importUseCase() }

        val response = HttpClient.newHttpClient().send(
            HttpRequest.newBuilder().GET().uri(URI("http://localhost:1234")).build(),
            HttpResponse.BodyHandlers.ofString()
        )

        assertEquals(response.statusCode(), HttpStatus.CREATED_201)
    }

    @Test
    fun `should return 500 and when use case throws`() {
        every { importUseCase() } throws Exception()

        val response = HttpClient.newHttpClient().send(
            HttpRequest.newBuilder().GET().uri(URI("http://localhost:1234")).build(),
            HttpResponse.BodyHandlers.ofString()
        )

        assertEquals(response.statusCode(), HttpStatus.INTERNAL_SERVER_ERROR_500)
    }

}