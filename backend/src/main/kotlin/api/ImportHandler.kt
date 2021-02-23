package api

import adapters.StoresGateway
import database.Repository
import entities.Season
import io.javalin.http.Context
import io.javalin.http.Handler
import org.eclipse.jetty.http.HttpStatus

class ImportHandler(
    private val gateway: StoresGateway,
    private val repo: Repository
) : Handler {
    override fun handle(ctx: Context) {
        try {
            val storesFromApi = gateway.getStores()
            val existingStores = repo.getStores()
            val storesAndSeasonsFromApi: List<Pair<Long, Season>> = gateway.getStoresAndSeasons()
            val seasonsFromApi: List<Season> = storesAndSeasonsFromApi.map { it.second }
            val existingStoresAndSeasons: List<Pair<Long, Season>> = repo.getStoreSeasons()
            val existingSeasons: List<Season> = repo.getSeasons()

            repo.importStores(storesFromApi
                .toSet()
                .filter { it !in existingStores }
                .toList()
            )
            repo.updateStores(storesFromApi
                .toSet()
                .filter { it in existingStores }
                .toList())
            repo.importSeasons(seasonsFromApi
                .toSet()
                .filter { it !in existingSeasons }
                .toList())
            repo.importStoreSeasons(storesAndSeasonsFromApi
                .toSet()
                .filter { it !in existingStoresAndSeasons }
                .toList()
            )
        } catch (e: Exception) {
            ctx.status(HttpStatus.SERVICE_UNAVAILABLE_503)
            ctx.result("Could not connect to Stores API.")
            return
        }
    }
}