package adapters

import entities.toSeason
import com.fasterxml.jackson.databind.JsonNode
import entities.Store
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest.newBuilder
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import entities.Season
import org.eclipse.jetty.http.HttpStatus
import java.net.http.HttpResponse.BodyHandlers.ofString
import java.time.LocalDate

class StoresAPIGateway(private val apiUrl: String, private val apiKey: String) : StoresGateway {
    companion object {
        private const val MAX_ATTEMPTS = 5
    }

    private val httpClient: HttpClient = HttpClient.newHttpClient()
    private val objectMapper = ObjectMapper()

    override fun getStores(): List<Store> {
        var pageQuerier = 0
        val stores = mutableListOf<Store>()
        var storesFromApi = listOf<Store>()

        while (storesFromApi.isNotEmpty() || pageQuerier == 0) {
            val request = newBuilder().GET()
                .header("apiKey", apiKey)
                .uri(URI("$apiUrl/v1/stores/?page=$pageQuerier"))

            for (attempt in 1..MAX_ATTEMPTS) {
                storesFromApi = (httpClient.send(request.build(), ofString()).run {
                    if (this.statusCode() != HttpStatus.OK_200 && attempt == MAX_ATTEMPTS) {
                        print("Only imported until page $pageQuerier. Stores API responded with 500 5 times in a row.")
                        return stores
                    }
                    objectMapper.readTree(this.body()).toStores()
                })
            }
            stores.addAll(storesFromApi)

            pageQuerier += 1
        }
        return stores
    }

    override fun getStoresAndSeasons(): List<Pair<Long, Season>> {
        val request = newBuilder().GET()
            .uri(URI("$apiUrl/other/stores_and_seasons"))
            .header("apiKey", apiKey)

        var returnedException: Exception = Exception("Unexpected error at stores and seasons endpoint")
        for (attempt in 1..MAX_ATTEMPTS) {
            try {
                return httpClient.send(request.build(), ofString()).run {
                    check(this.statusCode() == HttpStatus.OK_200) { throw Exception() }
                    objectMapper.readTree(this.body()).toStoresAndSeasons()
                }
            } catch (e: Exception) {
                returnedException = e
                continue
            }
        }
        throw returnedException
    }

    override fun getCSV(): List<Map<String, String>> {
        val request = newBuilder().GET()
            .uri(URI("$apiUrl/extra_data.csv"))
            .header("apiKey", apiKey)

        var returnedException: Exception = Exception("Unexpected error at get CSV endpoint")
        for (attempt in 1..MAX_ATTEMPTS) {
            try {
                return httpClient.send(request.build(), ofString()).run {
                    check(this.statusCode() == HttpStatus.OK_200) { throw Exception() }
                    csvReader().readAllWithHeader(this.body().replaceFirst(" Special field 2", "Special field 2"))
                }
            } catch (e: Exception) {
                returnedException = e
                continue
            }
        }
        throw returnedException
    }

    private fun JsonNode.toStores(): List<Store> {
        val list = mutableListOf<Store>()
        for (store in this) {
            try {
                list.add(
                    Store(
                        id = store.get("id").asLong(),
                        code = store.get("code")?.toTextOrNull(),
                        description = store.get("description")?.toTextOrNull(),
                        name = store.get("name").asText(),
                        openingDate = store.get("openingDate")?.toDateOrNull(),
                        storeType = store.get("storeType")?.toTextOrNull(),
                    )
                )
            } catch (e: Exception) {
                continue
            }
        }
        return list
    }

    private fun JsonNode.toDateOrNull(): LocalDate? {
        require(!this.isNull) { return null }
        return try {
            LocalDate.parse(this.asText())
        } catch (e: NullPointerException) {
            null
        }
    }

    private fun JsonNode.toTextOrNull(): String? {
        require(!this.isNull) { return null }
        return try {
            this.asText()
        } catch (e: NullPointerException) {
            null
        }
    }

    private fun JsonNode.toStoresAndSeasons(): List<Pair<Long, Season>> {
        val list = mutableListOf<Pair<Long, Season>>()
        for (store in this) {
            try {
                require(store.get("storeId") !== null) { throw Exception() }
                require(!store.get("storeId").isNull) { throw Exception() }
                require(store.get("season") !== null) { throw Exception() }
                require(!store.get("season").isNull) { throw Exception() }

                list.add(Pair(store.get("storeId").asLong(), store.get("season").asText().toSeason()))
            } catch (e: Exception) {
                continue
            }
        }
        return list
    }
}