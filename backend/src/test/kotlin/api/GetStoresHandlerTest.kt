package api

import database.Repository
import entities.Season
import entities.SeasonHalf
import entities.Store
import io.javalin.Javalin
import io.mockk.every
import io.mockk.mockk
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
import java.time.Year

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class GetStoresHandlerTest {
    private lateinit var mockServer: Javalin

    private val repo = mockk<Repository>(relaxed = true)

    @BeforeAll
    @Suppress("unused")
    fun setup() {
        mockServer = Javalin.create()
            .get("/", GetStoresHandler(repo))
            .start(1234)
    }

    @AfterAll
    @Suppress("unused")
    fun `tear down`() {
        mockServer.stop()
    }


    @Test
    fun `should return the full stores data`() {
        val repoStores = listOf(
            Store(1, name = "Store 1"),
            Store(2, name = "Store 2", openingDate = LocalDate.of(2021, 1, 1)),
            Store(3, name = "Store 3"),
            Store(4, name = "Store 4"),
        )
        val repoStoreSeasons = listOf(
            Pair(1.toLong(), Season(SeasonHalf.H1, Year.of(2021))),
            Pair(1.toLong(), Season(SeasonHalf.H2, Year.of(2021))),
            Pair(2.toLong(), Season(SeasonHalf.H2, Year.of(2021)))
        )
        every { repo.getStores() } returns repoStores
        every { repo.getStoreSeasons() } returns repoStoreSeasons

        val response = HttpClient.newHttpClient().send(
            HttpRequest.newBuilder().GET().uri(URI("http://localhost:1234")).build(),
            HttpResponse.BodyHandlers.ofString()
        )

        assertEquals( "[{\"id\":1,\"name\":\"Store 1\",\"seasons\":[{\"half\":\"H1\",\"year\":\"2021\"},{\"half\":\"H2\",\"year\":\"2021\"}]},{\"id\":2,\"name\":\"Store 2\",\"openingDate\":\"2021-01-01\",\"seasons\":[{\"half\":\"H2\",\"year\":\"2021\"}]},{\"id\":3,\"name\":\"Store 3\",\"seasons\":[]},{\"id\":4,\"name\":\"Store 4\",\"seasons\":[]}]", response.body())
    }

    @Test
    fun `should return 500 when repo throws`() {
        every { repo.getStores() } throws SQLException("Hoje não dá")

        val response = HttpClient.newHttpClient().send(
            HttpRequest.newBuilder().GET().uri(URI("http://localhost:1234")).build(),
            HttpResponse.BodyHandlers.ofString()
        )

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR_500, response.statusCode())
    }
}