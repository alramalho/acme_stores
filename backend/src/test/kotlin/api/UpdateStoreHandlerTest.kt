package api

import database.Repository
import entities.Store
import io.javalin.Javalin
import io.javalin.plugin.json.JavalinJson
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.eclipse.jetty.http.HttpStatus
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.sql.SQLException
import java.time.LocalDate

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class UpdateStoreHandlerTest {
    private lateinit var mockServer: Javalin

    private val repo = mockk<Repository>(relaxed = true)

    @BeforeAll
    @Suppress("unused")
    fun setup() {
        mockServer = Javalin.create()
            .put("/:id", UpdateStoreHandler(repo))
            .start(1234)
    }

    @AfterAll
    @Suppress("unused")
    fun `tear down`() {
        mockServer.stop()
    }

    @Test
    fun `should update a store in the repo`() {
        HttpClient.newHttpClient().send(
            HttpRequest.newBuilder().PUT(HttpRequest.BodyPublishers.ofString("""{"newName":"al capone"}"""))
                .uri(URI("http://localhost:1234/1")).build(),
            HttpResponse.BodyHandlers.ofString()
        )
        verify { repo.updateStoreName(1, "al capone") }
    }


    @Test
    fun `should return 500 when repo throws`() {
        every { repo.updateStoreName(1, "al capone") } throws SQLException("Hoje não dá")

        val response = HttpClient.newHttpClient().send(
            HttpRequest.newBuilder().PUT(HttpRequest.BodyPublishers.ofString("""{"newValue":"al capone"}"""))
                .uri(URI("http://localhost:1234/1")).build(),
            HttpResponse.BodyHandlers.ofString()
        )

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR_500, response.statusCode())
    }
}