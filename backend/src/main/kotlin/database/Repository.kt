package database

import entities.Season
import entities.Store

interface Repository {
    fun importStores(stores: List<Store>)
    fun getStores(): List<Store>
    fun updateStores(stores: List<Store>)
    fun updateStoreName(storeId: Long, newName: String)

    fun importSeasons(seasons: List<Season>)
    fun getSeasons(): List<Season>
    fun updateSeasons(seasons: List<Season>)

    fun importStoreSeasons(map: List<Pair<Long, Season>>)
    fun getStoreSeasons(): List<Pair<Long, Season>>

    fun deleteAll()
}