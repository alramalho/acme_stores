package api

import adapters.StoresGateway
import database.Repository
import entities.Season
import entities.Store
import io.javalin.http.Context
import io.javalin.http.Handler
import org.eclipse.jetty.http.HttpStatus

class ImportHandler(
    private val gateway: StoresGateway,
    private val repo: Repository
) : Handler {
    override fun handle(ctx: Context) {
        try {
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

            ctx.status(HttpStatus.CREATED_201)
            ctx.result("Process finished successfully. \nImported:\n${storesToImport.size} Stores\n${seasonsToImport.size} Seasons\n${storeSeasonsToImport.size} Season-Store relationships\nUpdated:\n${storesToUpdate.size} Stores\n")
        } catch (e: Exception) {
            ctx.status(HttpStatus.SERVICE_UNAVAILABLE_503)
            ctx.result(e.message.toString())
            return
        }
    }
}