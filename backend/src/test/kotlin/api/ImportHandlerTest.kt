package api

import adapters.StoresGateway
import database.Repository
import entities.Season
import entities.SeasonHalf
import entities.Store
import io.javalin.Javalin
import io.mockk.*
import org.eclipse.jetty.http.HttpStatus
import org.junit.jupiter.api.*
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.LocalDate
import java.time.Year

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ImportHandlerTest {
    private lateinit var mockServer: Javalin
    private val mockRepo = mockk<Repository>(relaxed = true)
    private val gateway = mockk<StoresGateway>(relaxed = true)

    @BeforeAll
    @Suppress("unused")
    fun setup() {
        mockServer = Javalin.create()
            .get("/", ImportHandler(gateway, mockRepo))
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
    fun `should successfully import the gateway stores into the repo`() {
        val returnedStoresList = listOf(
            Store(
                id = 1,
                code = "dummy-code-1",
                description = "descr-1",
                name = "Store 1",
                openingDate = LocalDate.of(2021, 2, 7),
                storeType = "STORE FRONT"
            ),
            Store(
                id = 2,
                code = "dummy-code-2",
                description = "descr-2",
                name = "Store 2",
                openingDate = LocalDate.of(2021, 2, 8),
                storeType = "RETAIL"
            ),
        )
        every { gateway.getStores() } returns returnedStoresList
        every { mockRepo.getStores() } returns listOf()
        justRun { mockRepo.importStores(any()) }
        justRun { mockRepo.updateStores(any()) }

        HttpClient.newHttpClient().send(
            HttpRequest.newBuilder().GET().uri(URI("http://localhost:1234")).build(),
            HttpResponse.BodyHandlers.ofString()
        )

        verify(exactly = 1) {
            gateway.getStores()
            mockRepo.importStores(returnedStoresList)
        }
    }

    @Test
    fun `should return 503 and not import stores if gateway throws`() {
        every { gateway.getStores() } throws Exception()
        every { mockRepo.getStores() } returns listOf()
        justRun { mockRepo.importStores(any()) }
        justRun { mockRepo.updateStores(any()) }

        val response = HttpClient.newHttpClient().send(
            HttpRequest.newBuilder().GET().uri(URI("http://localhost:1234")).build(),
            HttpResponse.BodyHandlers.ofString()
        )

        verify(exactly = 0) { mockRepo.importStores(any()) }
        Assertions.assertEquals(response.statusCode(), HttpStatus.SERVICE_UNAVAILABLE_503)
    }
    @Test
    fun `should update existing stores in the repo`() {
        val storesFromAPI = listOf(
            Store(
                id = 1,
                code = "dummy-code-1",
                description = "descr-1",
                name = "Store 1",
                openingDate = LocalDate.of(2021, 2, 7),
                storeType = "STORE FRONT"
            ),
            Store(
                id = 2,
                name = "Store 2",
            )
        )
        every { gateway.getStores() } returns storesFromAPI
        every { mockRepo.getStores() } returns listOf(Store(id = 1, name = "Store 1"))

        HttpClient.newHttpClient().send(
            HttpRequest.newBuilder().GET().uri(URI("http://localhost:1234")).build(),
            HttpResponse.BodyHandlers.ofString()
        )

        verify(exactly = 1) {
            gateway.getStores()
            mockRepo.updateStores(listOf(Store(
                id = 1,
                code = "dummy-code-1",
                description = "descr-1",
                name = "Store 1",
                openingDate = LocalDate.of(2021, 2, 7),
                storeType = "STORE FRONT"
            )))
        }
    }

    @Test
    fun `should successfully import the new gateway seasons & stores-seasons relationships into the repo`() {
        val returnedSeasonStores = listOf(
            Pair(1.toLong(), Season(SeasonHalf.H1, Year.of(2021))),
            Pair(2.toLong(), Season(SeasonHalf.H2, Year.of(2022)))
        )
        every { gateway.getStoresAndSeasons() } returns returnedSeasonStores
        every { mockRepo.getSeasons() } returns listOf(
            Season(SeasonHalf.H1, Year.of(2021))
        )

        HttpClient.newHttpClient().send(
            HttpRequest.newBuilder().GET().uri(URI("http://localhost:1234")).build(),
            HttpResponse.BodyHandlers.ofString()
        )

        verify(exactly = 1) {
            gateway.getStoresAndSeasons()
            mockRepo.importSeasons(listOf(
                Season(SeasonHalf.H2, Year.of(2022))
            ))
            mockRepo.importStoreSeasons(returnedSeasonStores)
        }
    }

    @Test
    fun `should return 503 and not import seasons or stores-seasons if gateway throws`() {
        every { gateway.getStoresAndSeasons() } throws Exception()
        every { mockRepo.getStores() } returns listOf()

        val response = HttpClient.newHttpClient().send(
            HttpRequest.newBuilder().GET().uri(URI("http://localhost:1234")).build(),
            HttpResponse.BodyHandlers.ofString()
        )

        verify(exactly = 0) { mockRepo.importSeasons(any()) }
        verify(exactly = 0) { mockRepo.importStoreSeasons(any()) }
        Assertions.assertEquals(response.statusCode(), HttpStatus.SERVICE_UNAVAILABLE_503)
    }
}