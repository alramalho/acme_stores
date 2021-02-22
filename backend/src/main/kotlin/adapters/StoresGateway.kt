package adapters

import entities.Season
import entities.Store

interface StoresGateway {
    fun getStores(): List<Store>
    fun getStoresAndSeasons(): Map<Long, Season>
    fun getCSV()
}