package adapters

import entities.Season
import entities.SeasonHalf
import entities.Store
import io.javalin.Javalin
import io.mockk.verify
import org.eclipse.jetty.http.HttpStatus
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import java.time.LocalDate
import java.time.Year
import java.util.concurrent.TimeUnit

class StoresAPIGatewayTest {
    private val gateway = StoresAPIGateway(apiUrl = "http://localhost:1234", apiKey = "key")
    private lateinit var mockApi: Javalin

    @Nested
    inner class StoresEndpoint {
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
                    when (it.queryParam("page")) {
                        "0" -> it.json(
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
                                mapOf(
                                    "id" to 7,
                                    "name" to null,
                                ),
                            )
                        )
                        else -> it.json(emptyList<String>())
                    }

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

            assertEquals(expectedStores, stores)
            assertEquals(apiKeyHeader, "key")
        }

        @Test
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        fun `should query all pages until api returns empty list`() {
            val queryParams = mutableListOf<String>()
            mockApi = Javalin.create().start(1234)
                .get("/v1/stores/") {
                    it.queryParam("page")?.let { param ->
                        if (param.toInt() < 5) {
                            queryParams.add(param)

                            it.json(
                                listOf(
                                    mapOf(
                                        "id" to param.toInt(),
                                        "name" to "Store $param",
                                    )
                                )
                            )
                        } else {
                            it.json(emptyList<String>())
                        }
                    }
                }

            gateway.getStores()

            assertTrue(queryParams.containsAll(listOf("1", "2", "3", "4")))
        }

        @Test
        @Timeout(value = 7, unit = TimeUnit.SECONDS)
        fun `should not fetch the page for that the api doesn't return 200 5 times in a row `() {
            var counter = 0
            mockApi = Javalin.create().start(1234)
                .get("/v1/stores/") {
                    when (it.queryParam("page")) {
                        "0" -> it.json(listOf(mapOf("id" to 0, "name" to "Store 0")))
                        "1" -> it.json(listOf(mapOf("id" to 1, "name" to "Store 1")))
                        "2" -> it.json(listOf(mapOf("id" to 2, "name" to "Store 2")))
                        "3" -> {
                            it.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
                            counter++
                        }
                        "4" -> it.json(listOf(mapOf("id" to 4, "name" to "Store 4")))
                        else -> it.json(emptyList<String>())
                    }
                }

            val stores = gateway.getStores()
            assertEquals(5, counter)
            assertEquals(
                listOf(
                    Store(id = 0, name = "Store 0"),
                    Store(id = 1, name = "Store 1"),
                    Store(id = 2, name = "Store 2"),
                    Store(id = 4, name = "Store 4"),
                ), stores
            )
        }
    }

    @Nested
    inner class SeasonStoresEndpoint() {
        @AfterEach
        @Suppress("unused")
        fun `tear down`() {
            mockApi.stop()
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

        @Test
        fun `should throw when api stores and seasons endpoint does not return 200`() {
            mockApi = Javalin.create().start(1234)
                .get("/other/stores_and_seasons") {
                    it.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
                }

            assertThrows<Exception> {
                gateway.getStoresAndSeasons()
            }
        }


        @Test
        @Timeout(value = 7, unit = TimeUnit.SECONDS)
        fun `should attempt 5 times before failing`() {
            var counter = 0
            mockApi = Javalin.create().start(1234)
                .get("/other/stores_and_seasons") {
                    counter++
                    it.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
                }

            assertThrows<Exception> {
                gateway.getStoresAndSeasons()
            }
            assertEquals(5, counter)
        }
    }

    @Nested
    inner class CSVEndpoint {
        @AfterEach
        @Suppress("unused")
        fun `tear down`() {
            mockApi.stop()
        }

        @Test
        fun `should retrieve the CSV information and return it`() {
            mockApi = Javalin.create().start(1234)
                .get("/extra_data.csv") {
                    it.result(
                        """
                        Store id,Special field 1, Special field 2
                        1,, special field 1_2
                        2, special field 2_1,
                        3,special field 3_1, special field 3_2
                    """.trimIndent()
                    )
                    it.contentType("text/plain;charset=UTF-8")
                }

            val csvInfo = gateway.getCSV()

            assertEquals(
                listOf(
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
                ),
                csvInfo
            )
        }

        @Test
        fun `should attempt 5 times before failing`() {
            var counter = 0
            mockApi = Javalin.create().start(1234)
                .get("/extra_data.csv") {
                    counter++
                    it.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
                }
            assertThrows<Exception> {
                gateway.getCSV()
            }
            assertEquals(5, counter)
        }
    }
}