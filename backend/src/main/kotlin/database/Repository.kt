package database

import entities.Season
import entities.Store

interface Repository {
    fun importStores(stores: List<Store>)
    fun getStores(): List<Store>

    fun importSeasons(seasons: List<Season>)
    fun getSeasons(): List<Season>

    fun getStoreSeasons(): Map<Store, Season>
}