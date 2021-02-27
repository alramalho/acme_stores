package api.usecases

import adapters.StoresGateway
import database.Repository
import entities.Season
import entities.SeasonHalf
import entities.Store
import io.javalin.Javalin
import io.mockk.*
import org.junit.jupiter.api.*
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.LocalDate
import java.time.Year

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ImportDataTest {
    private val repo = mockk<Repository>(relaxed = true)
    private val gateway = mockk<StoresGateway>(relaxed = true)
    private val importUseCase = ImportData(gateway, repo)

    @AfterEach
    fun `after each`() = clearAllMocks()

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
        every { repo.getStores() } returns listOf()
        justRun { repo.importStores(any()) }
        justRun { repo.updateStores(any()) }

        importUseCase.invoke()

        verify(exactly = 1) {
            gateway.getStores()
            repo.importStores(returnedStoresList)
        }
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
        every { repo.getStores() } returns listOf(Store(id = 1, name = "Store 1"))

        importUseCase.invoke()

        verify(exactly = 1) {
            gateway.getStores()
            repo.updateStores(
                listOf(
                    Store(
                        id = 1,
                        code = "dummy-code-1",
                        description = "descr-1",
                        name = "Store 1",
                        openingDate = LocalDate.of(2021, 2, 7),
                        storeType = "STORE FRONT"
                    )
                )
            )
        }
    }

    @Test
    fun `should only import the seasons & stores-seasons relationships for stores and seasons present in the repo`() {
        every { repo.getStores() } returns listOf(Store(id = 1, name = "Store 1"))
        every { repo.getSeasons() } returns listOf(Season(SeasonHalf.H1, Year.of(2021)))
        val gatewayStoresAndSeasons = listOf(
            Pair(1.toLong(), Season(SeasonHalf.H1, Year.of(2021))),
            Pair(1.toLong(), Season(SeasonHalf.H2, Year.of(2021))),
            Pair(2.toLong(), Season(SeasonHalf.H1, Year.of(2022))),
            Pair(2.toLong(), Season(SeasonHalf.H2, Year.of(2022)))
        )
        every { gateway.getStoresAndSeasons() } returns gatewayStoresAndSeasons

        importUseCase.invoke()

        verify(exactly = 1) {
            gateway.getStoresAndSeasons()
            repo.importStoreSeasons(
                listOf(
                    Pair(1.toLong(), Season(SeasonHalf.H1, Year.of(2021)))
                )
            )
        }
    }

    @Test
    fun `should only import the returned gateway seasons for seasons not already present in the repo`() {
        every { repo.getSeasons() } returns listOf(Season(SeasonHalf.H1, Year.of(2021)))
        val gatewayStoresAndSeasons = listOf(
            Pair(1.toLong(), Season(SeasonHalf.H1, Year.of(2021))),
            Pair(1.toLong(), Season(SeasonHalf.H2, Year.of(2021))),
            Pair(2.toLong(), Season(SeasonHalf.H1, Year.of(2022))),
            Pair(2.toLong(), Season(SeasonHalf.H2, Year.of(2022)))
        )
        every { gateway.getStoresAndSeasons() } returns gatewayStoresAndSeasons

        importUseCase.invoke()

        verify(exactly = 1) {
            gateway.getStoresAndSeasons()
            repo.importSeasons(
                listOf(
                    Season(SeasonHalf.H2, Year.of(2021)),
                    Season(SeasonHalf.H1, Year.of(2022)),
                    Season(SeasonHalf.H2, Year.of(2022))
                )
            )
        }
    }

    @Test
    fun `should not import seasons or stores-seasons and propagate exception`() {
        every { gateway.getStoresAndSeasons() } throws Exception()
        every { repo.getStores() } returns listOf()

        assertThrows<Exception> {
            importUseCase.invoke()
        }

        verify(exactly = 0) { repo.importSeasons(any()) }
        verify(exactly = 0) { repo.importStoreSeasons(any()) }
    }

    @Test
    fun `should update the existent stores with the CSV information`() {
        every { gateway.getStores() } returns listOf(
            Store(id=1, name="Store 1"),
            Store(id=2, name="Store 2")
        )
        every { repo.getStores() } returns listOf(
            Store(id=1, name="Store 1"),
            Store(id=2, name="Store 2")
        )
        every { gateway.getCSV() } returns listOf(
            mapOf(
                "Store id" to "1",
                "Special field 1" to "",
                "Special field 2" to " special field 1_2",
            ),
            mapOf(
                "Store id" to "2",
                "Special field 1" to " special field 2_1",
                "Special field 2" to "",
            ),
            mapOf(
                "Store id" to "3",
                "Special field 1" to "special field 3_1",
                "Special field 2" to " special field 3_2",
            ),
        )

        importUseCase.invoke()

        verify(exactly = 1) {
            repo.updateStores(
                listOf(
                    Store(
                        id = 1,
                        name = "Store 1",
                        specialField1 = "",
                        specialField2 = " special field 1_2"
                    ),
                    Store(
                        id = 2,
                        name = "Store 2",
                        specialField1 = " special field 2_1",
                        specialField2 = ""
                    )
                )
            )
        }
    }
}