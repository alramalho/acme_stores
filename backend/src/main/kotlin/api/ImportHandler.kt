package api

import adapters.StoresGateway
import database.Repository
import entities.Season
import io.javalin.http.Context
import io.javalin.http.Handler

class ImportHandler(
    private val gateway: StoresGateway,
    private val repo: Repository
) : Handler {
    override fun handle(ctx: Context) {
        handleStores()
        handleStoresAndSeasons()
    }

    private fun handleStores() {
        val storesFromApi = gateway.getStores()
        val existingStores = repo.getStores()

        repo.importStores(storesFromApi
            .toSet()
            .filter { it !in existingStores }
            .toList()
        )
        repo.updateStores(storesFromApi
            .toSet()
            .filter { it in existingStores }
            .toList())
    }

    private fun handleStoresAndSeasons() {
        val storesAndSeasonsFromApi: List<Pair<Long, Season>> = gateway.getStoresAndSeasons()
        val seasons: List<Season> = storesAndSeasonsFromApi.map { it.second }
        val existingStoresAndSeasons: List<Pair<Long, Season>> = repo.getStoreSeasons()
        val existingSeasons: List<Season> = repo.getSeasons()

        repo.importSeasons(seasons
            .toSet()
            .filter { it !in existingSeasons }
            .toList())
        repo.importStoreSeasons(storesAndSeasonsFromApi
            .toSet()
            .filter { it !in existingStoresAndSeasons }
            .toList()
        )

    }
}