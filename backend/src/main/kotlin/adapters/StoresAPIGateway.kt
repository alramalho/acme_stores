package adapters

import adapters.StoresAPIGateway.GetStoresResult.Failure
import adapters.StoresAPIGateway.GetStoresResult.Success
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

class StoresAPIGateway(private val apiUrl: String, private val apiKey: String) {
    companion object {
        private const val MAX_ATTEMPTS = 5
    }

    private val httpClient: HttpClient = HttpClient.newHttpClient()
    private val objectMapper = ObjectMapper()

    fun getStores(page: Int = 0): List<Store> {
        val list = mutableListOf<Store>()
        for(attempt in 1..MAX_ATTEMPTS) {
            when (val result = requestStores(page)) {
                is Success -> {
                    if (result.stores.isNotEmpty()) {
                        list.addAll(result.stores)
                        list.addAll(getStores(page + 1))
                    }
                    break
                }
                is Failure -> {
                    if (attempt == MAX_ATTEMPTS) {
                        list.addAll(getStores(page + 1))
                        break
                    }
                }
            }
        }
        return list
    }

    private fun requestStores(page: Int = 0): GetStoresResult {
        val request = newBuilder().GET()
            .header("apiKey", apiKey)
            .uri(URI("$apiUrl/v1/stores/?page=$page"))

        httpClient.send(request.build(), ofString()).run {
            if (statusCode() != HttpStatus.OK_200) {
                return Failure
            }
            return Success(objectMapper.readTree(body()).toStores())
        }
    }

    sealed class GetStoresResult {
        class Success(val stores: List<Store>) : GetStoresResult()
        object Failure : GetStoresResult()
    }

    fun getStoresAndSeasons(): List<Pair<Long, Season>> {
        val request = newBuilder().GET()
            .uri(URI("$apiUrl/other/stores_and_seasons"))
            .header("apiKey", apiKey)

        var returnedException: Exception = Exception("Unexpected error at stores and seasons endpoint")
        for (attempt in 1..MAX_ATTEMPTS) {
            try {
                return httpClient.send(request.build(), ofString()).run {
                    require(this.statusCode() == HttpStatus.OK_200)
                    objectMapper.readTree(this.body()).toStoresAndSeasons()
                }
            } catch (e: Exception) {
                returnedException = e
                continue
            }
        }
        throw returnedException
    }

    fun getCSV(): List<Map<String, String>> {
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

    private fun JsonNode.toStores(): List<Store> =
        filter { store ->
            store.get("name") !== null &&
                    !store.get("name").isNull &&
                    store.get("id") !== null &&
                    !store.get("id").isNull
        }.map { store ->
            Store(
                id = store.get("id").asLong(),
                code = store.get("code")?.toTextOrNull(),
                description = store.get("description")?.toTextOrNull(),
                name = store.get("name").textValue(),
                openingDate = store.get("openingDate")?.toDateOrNull(),
                storeType = store.get("storeType")?.toTextOrNull(),
            )
        }

    private fun JsonNode.toDateOrNull(): LocalDate? {
        require(!this.isNull) { return null }
        return LocalDate.parse(this.textValue())
    }

    private fun JsonNode.toTextOrNull(): String? {
        require(!this.isNull) { return null }
        return this.textValue()
    }

    private fun JsonNode.toStoresAndSeasons(): List<Pair<Long, Season>> =
        filter {
            it.get("storeId") !== null &&
                    !it.get("storeId").isNull &&
                    it.get("season") !== null &&
                    !it.get("season").isNull
        }.map {
            Pair(it.get("storeId").asLong(), it.get("season").textValue().toSeason())
        }
}