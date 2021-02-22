package adapters

import entities.toSeason
import com.fasterxml.jackson.databind.JsonNode
import entities.Store
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest.newBuilder
import com.fasterxml.jackson.databind.ObjectMapper
import entities.Season
import java.net.http.HttpResponse.BodyHandlers.ofString
import java.time.LocalDate

// TODO: API UNAVAILABILITY
class StoresAPIGateway(private val apiUrl: String, private val apiKey: String) : StoresGateway {
    private val httpClient: HttpClient = HttpClient.newHttpClient()
    private val objectMapper = ObjectMapper()

    // TODO: PAGINATION
    override fun getStores(): List<Store> {
        val request = newBuilder().GET()
            .header("apiKey", apiKey)
            .uri(URI("$apiUrl/v1/stores"))

        return httpClient.send(request.build(), ofString()).run {
            objectMapper.readTree(this.body()).toStores()
        }
    }

    override fun getStoresAndSeasons(): Map<Long, Season> {
        val request = newBuilder().GET()
            .uri(URI("$apiUrl/other/stores_and_seasons"))
            .header("apiKey", apiKey)

        return httpClient.send(request.build(), ofString()).run {
            objectMapper.readTree(this.body()).toStoresAndSeasons()
        }
    }

    override fun getCSV() {
        TODO("Not yet implemented")
    }

    private fun JsonNode.toStores(): List<Store> {
        val list = mutableListOf<Store>()
        for (store in this) {
            try {
                list.add(
                    Store(
                        id = store.get("id").asLong(),
                        code = store.get("code").toTextOrNull(),
                        description = store.get("description").toTextOrNull(),
                        name = store.get("name").asText(),
                        openingDate = store.get("openingDate").toDateOrNull(),
                        storeType = store.get("storeType").toTextOrNull(),
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

    private fun JsonNode.toStoresAndSeasons(): Map<Long, Season> {
        val map = mutableMapOf<Long, Season>()
        for (store in this) {
            try {
                require(store.get("storeId") !== null) { throw Exception() }
                require(!store.get("storeId").isNull) { throw Exception() }
                require(store.get("season") !== null) { throw Exception() }
                require(!store.get("season").isNull) { throw Exception() }

                map[store.get("storeId").asLong()] = store.get("season").asText().toSeason()
            } catch (e: Exception) {
                continue
            }
        }
        return map
    }
}