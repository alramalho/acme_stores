package database

import entities.Store

interface Repository {
    fun importStores(stores: List<Store>)

}