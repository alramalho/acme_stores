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


class StoresAPIGateway(private val apiUrl: String, private val apiKey: String) : StoresGateway {
    private val httpClient: HttpClient = HttpClient.newHttpClient()
    private val objectMapper = ObjectMapper()

    override fun getStores(): List<Store> {
        val request = newBuilder().GET()
            .uri(URI("$apiUrl/v1/stores"))
            .header("apiKey", apiKey)

        val response = httpClient.send(request.build(), ofString())

        return objectMapper.readTree(response.body()).toStores()

    }

    override fun getStoresAndSeasons(): Map<Int, Season> {
        val request = newBuilder().GET()
            .uri(URI("$apiUrl/other/stores_and_seasons"))
            .header("apiKey", apiKey)

        val response = httpClient.send(request.build(), ofString())

        return objectMapper.readTree(response.body()).toStoresAndSeasons()
    }

    override fun getCSV() {
        TODO("Not yet implemented")
    }

    private fun JsonNode.toStores(): List<Store> {
        val list = mutableListOf<Store>()
        for (store in this) {
            list.add(
                Store(
                    id = store.get("id").asInt(),
                    code = store.get("code").asText(),
                    description = store.get("description").asText(),
                    name = store.get("name").asText(),
                    openingDate = LocalDate.parse(store.get("openingDate").asText()),
                    storeType = store.get("storeType").asText(),
                )
            )
        }
        return list
    }

    private fun JsonNode.toStoresAndSeasons(): Map<Int, Season> {
        val map = mutableMapOf<Int, Season>()
        for (store in this) {
            try {
                map[store.get("storeId").asInt()] = store.get("season").asText().toSeason()
            } catch (e: Exception) {
                continue
            }
        }
        return map
    }
}