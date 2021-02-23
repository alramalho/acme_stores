package adapters

import entities.Season
import entities.SeasonHalf
import entities.Store
import io.javalin.Javalin
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import java.time.LocalDate
import java.time.Year

class StoresAPIGatewayTest {
    private val gateway = StoresAPIGateway(apiUrl = "http://localhost:1234", apiKey = "key")
    private lateinit var mockApi: Javalin

    @AfterEach
    @Suppress("unused")
    fun `tear down`() {
        mockApi.stop()
    }

    @Test
    fun `should retrieve the stores from the API and ignore the unparsable`() {
        var apiKeyHeader: String? = null
        mockApi = Javalin.create().start(1234)
            .get("/v1/stores/") {
                it.json(
                    listOf(
                        mapOf(
                            "id" to 1,
                            "code" to "dummy-code-1",
                            "name" to "Store 1",
                            "description" to "descr-1",
                            "openingDate" to "2021-02-07",
                            "storeType" to "STORE FRONT",
                        ),
                        mapOf(
                            "id" to 2,
                            "code" to null,
                            "name" to "Store 2",
                            "description" to null,
                            "openingDate" to null,
                            "storeType" to null,
                        ),
                        mapOf(
                            "id" to 3,
                            "name" to "Store 3",
                        ),
                        mapOf(
                            "id" to 4,
                            "trol" to "trol",
                        ),
                        mapOf(
                            "id" to 5,
                        ),
                        mapOf(
                            "name" to "Store 6",
                        ),
                    )
                )
                apiKeyHeader = it.header("apiKey")
            }
        val expectedStores = listOf(
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
            ),
            Store(
                id = 3,
                name = "Store 3",
            )
        )

        val stores = gateway.getStores()

        assertEquals(stores, expectedStores)
        assertEquals(apiKeyHeader, "key")
    }

    @Test
    fun `should retrieve the stores and seasons form the API and ignore the unparsable`() {
        var apiKeyHeader: String? = null
        mockApi = Javalin.create().start(1234)
            .get("/other/stores_and_seasons") {
                it.json(
                    listOf(
                        mapOf(
                            "storeId" to 1,
                            "season" to "H1 21",
                        ),
                        mapOf(
                            "storeId" to 2,
                            "season" to "H2 22",
                        ),
                        mapOf(
                            "storeId" to 3,
                        ),
                        mapOf(
                            "season" to "H2 22",
                        ),
                        mapOf(
                            "season" to "lololol",
                        ),
                        mapOf(
                            "storeId" to 7,
                            "season" to null,
                        ),
                        mapOf(
                            "storeId" to null,
                            "season" to "H1 23",
                        ),
                    )
                )
                apiKeyHeader = it.header("apiKey")
            }
        val expectedStoresAndSeasons = listOf(
            Pair(1.toLong(), Season(SeasonHalf.H1, Year.of(2021))),
            Pair(2.toLong(), Season(SeasonHalf.H2, Year.of(2022)))
        )

        val storesAndSeasons = gateway.getStoresAndSeasons()

        assertEquals(storesAndSeasons, expectedStoresAndSeasons)
        assertEquals(apiKeyHeader, "key")
    }
}