package api.usecases

import adapters.StoresAPIGateway
import database.Repository
import entities.Season
import entities.Store

class ImportData(
    private val gateway: StoresAPIGateway,
    private val repo: Repository
) {
    operator fun invoke() {
        println("Importing data...")

        val storesFromApi: List<Store> = gateway.getStores()
        val existingStores: List<Store> = repo.getStores()
        val storesAndSeasonsFromApi: List<Pair<Long, Season>> = gateway.getStoresAndSeasons()
        val seasonsFromApi: List<Season> = storesAndSeasonsFromApi.map { it.second }
        val existingStoresAndSeasons: List<Pair<Long, Season>> = repo.getStoreSeasons()
        val existingSeasons: List<Season> = repo.getSeasons()

        val storesToUpdate: MutableList<Store> = storesFromApi
            .toSet()
            .filter { it in existingStores }
            .toMutableList()

        val mappedStores: MutableMap<Long, Store> = mutableMapOf()
        storesToUpdate.map { mappedStores.plus(Pair(it.id, it)) }

        val csvFromApi = gateway.getCSV()
        for (entry in csvFromApi) {
            try {
                val entryId = entry["Store id"]!!.toLong()
                val existentStore = mappedStores[entryId]!!
                storesToUpdate.add(
                    Store(
                        id = entryId,
                        name = existentStore.name,
                        code = existentStore.code,
                        description = existentStore.description,
                        openingDate = existentStore.openingDate,
                        specialField1 = entry["Special field 1"],
                        specialField2 = entry["Special field 2"]
                    )
                )
            } catch (e: Exception) {
                continue
            }
        }

        val storesToImport = storesFromApi
            .toSet()
            .filter { it !in existingStores }
            .toList()
        val seasonsToImport = seasonsFromApi
            .toSet()
            .filter { it !in existingSeasons }
            .toList()
        val storeSeasonsToImport = storesAndSeasonsFromApi
            .toSet()
            .filter { it !in existingStoresAndSeasons }
            .filter { it.first in existingStores.map { store -> store.id } }
            .filter { it.second in existingSeasons }
            .toList()

        repo.importStores(storesToImport)
        repo.updateStores(storesToUpdate)
        repo.importSeasons(seasonsToImport)
        repo.importStoreSeasons(storeSeasonsToImport)

        println("Import finished successfully. \nImported:\n${storesToImport.size} Stores\n${seasonsToImport.size} Seasons\n${storeSeasonsToImport.size} Season-Store relationships\nUpdated:\n${storesToUpdate.size} Stores\n")
    }
}